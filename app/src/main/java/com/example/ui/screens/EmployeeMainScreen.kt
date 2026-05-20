package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import com.example.data.entity.Employee
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.ui.components.QrCodeImage
import com.example.ui.utils.Localization
import com.example.ui.viewmodel.StaffViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmployeeMainScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val employee by viewModel.currentEmployee.collectAsState()
    val currentLang by viewModel.appLanguage.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (employee == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Veuillez d'abord vous connecter")
        }
        return
    }

    val emp = employee!!
    val tabs = listOf(
        TabItem(Localization.get("my_qr", currentLang), Icons.Default.QrCode, Icons.Outlined.QrCode),
        TabItem(Localization.get("pointages_tab", currentLang), Icons.Default.Fingerprint, Icons.Outlined.Fingerprint),
        TabItem(Localization.get("my_salary_tab", currentLang), Icons.Default.RequestQuote, Icons.Outlined.RequestQuote)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emp.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Espace Collaborateur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Déconnexion",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MonQrTab(viewModel, emp)
                1 -> PointagesTab(viewModel, emp)
                2 -> MesSalairesTab(viewModel, emp)
            }
            
            if (showSettingsDialog) {
                SettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}

// ==================== TAB 0: MON QR BADGE ====================
@Composable
fun MonQrTab(viewModel: StaffViewModel, employee: Employee) {
    val scrollState = rememberScrollState()
    val currentLang by viewModel.appLanguage.collectAsState()
    var isCheckingInWifi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Professional graphic badges layout with beautiful depth styling!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Company Info
                Text(
                    text = "BADGE COLLABORATEUR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "StaffFlow Enterprise",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Profile photo uploader/picker
                val context = LocalContext.current
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { sourceUri ->
                        try {
                            val contentResolver = context.contentResolver
                            val directory = java.io.File(context.filesDir, "profile_photos")
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }
                            val file = java.io.File(directory, "profile_${employee.matricule}.jpg")
                            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                                file.outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            val savedUri = Uri.fromFile(file).toString()
                            val updatedEmployee = employee.copy(photoUri = savedUri)
                            viewModel.updateEmployee(updatedEmployee) {}
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Safe fallback
                            val updatedEmployee = employee.copy(photoUri = sourceUri.toString())
                            viewModel.updateEmployee(updatedEmployee) {}
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (employee.photoUri != null) {
                        AsyncImage(
                            model = employee.photoUri,
                            contentDescription = "Photo profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Uploader photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Ajouter Photo",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Icon(
                        imageVector = if (employee.photoUri != null) Icons.Default.Edit else Icons.Default.Upload,
                        contentDescription = "Upload/Edit photo inline button",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (employee.photoUri != null) "Changer de photo" else "Uploader ma photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // High fidelity QR canvas
                val qrText = "STAFFFLOW:${employee.matricule}"
                QrCodeImage(
                    content = qrText,
                    modifier = Modifier
                        .size(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Employee meta descriptions details
                Text(
                    text = employee.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = employee.poste,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Matricule: ${employee.matricule}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 1 Wi-Fi Pointage Button
                Button(
                    onClick = {
                        isCheckingInWifi = true
                        viewModel.triggerLocalWifiPointage(employee.matricule) { success, message ->
                            isCheckingInWifi = false
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isCheckingInWifi,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCheckingInWifi) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.get("client_wifi_pointage", currentLang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    RoundedCornerShape(12.dp)
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Présentez ce code QR devant la caméra d'accueil de la tablette PC de l'entreprise à votre arrivée et départ.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ==================== TAB 1: POINTAGES HISTORIQUE ====================
@Composable
fun PointagesTab(viewModel: StaffViewModel, employee: Employee) {
    val attendanceList by viewModel.getAttendanceForEmployee(employee.matricule).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mon Historique de Présence",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (attendanceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun pointage enregistré historiquement.", fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attendanceList) { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date : ${att.date}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                val statusColor = if (att.isLate) Color(0xFFD84315) else Color(0xFF2E7D32)
                                val statusText = if (att.isLate) "Retard" else "Présent"
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val checkInTimeStr = if (att.checkInMs != null) {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(att.checkInMs))
                                } else "--:--"

                                val checkOutTimeStr = if (att.checkOutMs != null) {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(att.checkOutMs))
                                } else "--:--"

                                Column {
                                    Text("Entrée", fontSize = 11.sp, color = Color.Gray)
                                    Text(checkInTimeStr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column {
                                    Text("Sortie", fontSize = 11.sp, color = Color.Gray)
                                    Text(checkOutTimeStr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Heures Totales", fontSize = 11.sp, color = Color.Gray)
                                    Text("${att.workedHours} h", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 2: PRIVILEGIED PAYROLL SLIPS ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MesSalairesTab(viewModel: StaffViewModel, employee: Employee) {
    val paymentsList by viewModel.getPaymentsForEmployee(employee.matricule).collectAsState(initial = emptyList())
    val currentCurrency by viewModel.appCurrency.collectAsState()
    val currentLang by viewModel.appLanguage.collectAsState()

    var showWithdrawDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.get("balance_available", currentLang).ifBlank { "Solde disponible" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f %s", employee.balance, currentCurrency),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showWithdrawDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("make_withdrawal", currentLang).ifBlank { "Faire un retrait" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Text(
            text = "Mon Registre de Heures & Paies",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (paymentsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune action de paie ou de retrait enregistrée pour le moment.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(paymentsList) { pay ->
                    val isWithdrawalLog = pay.month.startsWith("Retrait")
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isWithdrawalLog) Icons.Default.AccountBalanceWallet else Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = if (isWithdrawalLog) Color(0xFFC62828) else Color(0xFF2E7D32)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isWithdrawalLog) pay.month else "Fiche de Paie : ${pay.month}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isWithdrawalLog) Color(0xFFC62828).copy(alpha = 0.08f) else Color(0xFF2E7D32).copy(alpha = 0.08f)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isWithdrawalLog) "Débité" else "Payé",
                                        color = if (isWithdrawalLog) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            if (isWithdrawalLog) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Type de transaction :", fontSize = 12.sp, color = Color.Gray)
                                    Text("Retrait de fonds", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Date de valeur :", fontSize = 12.sp, color = Color.Gray)
                                    val dateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(pay.paymentDateMs))
                                    Text(dateStr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Heures Normales :", fontSize = 12.sp, color = Color.Gray)
                                    Text("${pay.normalHours} h", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Heures Supplémentaires :", fontSize = 12.sp, color = Color.Gray)
                                    Text("${pay.overtimeHours} h (Maj. 1.5x)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (pay.bonus > 0.1) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Primes supplémentaires :", fontSize = 12.sp, color = Color.Gray)
                                        Text(String.format(Locale.getDefault(), "+ %,.0f %s", pay.bonus, currentCurrency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }
                                }
                                if (pay.deductions > 0.1) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Déductions (Retards/Avances) :", fontSize = 12.sp, color = Color.Gray)
                                        Text(String.format(Locale.getDefault(), "- %,.0f %s", pay.deductions, currentCurrency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isWithdrawalLog) "Montant Débité :" else "Versement Net Reçu :", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.getDefault(), "%,.0f %s", pay.amountPaid, currentCurrency),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWithdrawalLog) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            viewModel = viewModel,
            employee = employee,
            onDismiss = { showWithdrawDialog = false }
        )
    }
}

// ==================== DIALOG DE RETRAIT COMPACT & INTUITIF ====================
@Composable
fun WithdrawDialog(
    viewModel: StaffViewModel,
    employee: Employee,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.appLanguage.collectAsState()
    val currentCurrency by viewModel.appCurrency.collectAsState()

    var withdrawMethod by remember { mutableStateOf("Liquide") }
    var amountInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faire un retrait",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Current balance display
                Text(
                    text = "Solde disponible :",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = String.format(Locale.getDefault(), "%,.0f %s", employee.balance, currentCurrency),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Select withdraw method
                Text(
                    text = "Mode de retrait",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Liquide", "Mobile", "Banque").forEach { method ->
                        FilterChip(
                            selected = withdrawMethod == method,
                            onClick = { withdrawMethod = method },
                            label = { Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Number Input field
                Text(
                    text = when(withdrawMethod) {
                        "Mobile" -> "Numéro Mobile Money (ex: Mvola, Orange Money)"
                        "Banque" -> "Numéro de compte principal (IBAN / RIB)"
                        else -> "Référence ou nom du bénéficiaire"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = numberInput,
                    onValueChange = { numberInput = it },
                    placeholder = {
                        Text(
                            when(withdrawMethod) {
                                "Mobile" -> "034XXXXXXX"
                                "Banque" -> "30004 00001 XXXXXXX"
                                else -> "Nom complet ou référence"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount field
                Text(
                    text = "Montant à retirer ($currentCurrency)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { 
                        amountInput = it
                        errorMessage = null 
                    },
                    placeholder = { Text("0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMessage = "Veuillez entrer un montant valide"
                            } else if (amt > employee.balance) {
                                errorMessage = "Solde disponible insuffisant !"
                            } else if (numberInput.isBlank()) {
                                errorMessage = "Veuillez entrer les coordonnées de destination"
                            } else {
                                errorMessage = null
                                showConfirmationDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continuer")
                    }
                }
            }
        }
    }

    // Confirmation Alert
    if (showConfirmationDialog) {
        val amt = amountInput.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.withdrawAmount(
                            matricule = employee.matricule,
                            amount = amt,
                            mode = withdrawMethod,
                            number = numberInput
                        ) { success, msg ->
                            if (success) {
                                showConfirmationDialog = false
                                onDismiss()
                            } else {
                                errorMessage = msg
                                showConfirmationDialog = false
                            }
                        }
                    }
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Annuler")
                }
            },
            title = {
                Text(
                    text = "Confirmer le retrait",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Êtes-vous sûr de vouloir effectuer ce retrait de votre compte ?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Destination : $withdrawMethod ($numberInput)", fontWeight = FontWeight.Bold)
                    Text("• Montant : ${String.format(Locale.getDefault(), "%,.0f %s", amt, currentCurrency)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("• Nouveau solde : ${String.format(Locale.getDefault(), "%,.0f %s", employee.balance - amt, currentCurrency)}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        )
    }
}
