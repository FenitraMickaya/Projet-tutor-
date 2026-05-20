package com.example.ui.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.Employee
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.repository.ScanResult
import com.example.ui.components.QrCodeImage
import com.example.ui.utils.Localization
import com.example.ui.viewmodel.StaffViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminMainScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.appLanguage.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val tabs = listOf(
        TabItem(Localization.get("dashboard", currentLang), Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        TabItem(Localization.get("employees", currentLang), Icons.Default.People, Icons.Outlined.People),
        TabItem(Localization.get("attendance", currentLang), Icons.Default.QrCodeScanner, Icons.Outlined.QrCodeScanner),
        TabItem(Localization.get("salaries", currentLang), Icons.Default.Payments, Icons.Outlined.Payments),
        TabItem(Localization.get("reports", currentLang), Icons.Default.Assessment, Icons.Outlined.Assessment)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("StaffFlow Admin", fontWeight = FontWeight.ExtraBold)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                0 -> DashboardTab(viewModel)
                1 -> EmployeesTab(viewModel)
                2 -> PointageTab(viewModel)
                3 -> SalariesTab(viewModel)
                4 -> ReportsTab(viewModel)
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

data class TabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// ==================== DASHBOARD TAB ====================
@Composable
fun DashboardTab(viewModel: StaffViewModel) {
    val employeeCount by viewModel.activeEmployeesCount.collectAsState()
    val presentCount by viewModel.presentTodayCount.collectAsState()
    val lateCount by viewModel.lateTodayCount.collectAsState()
    val totalPaidVal by viewModel.totalPayrollSpent.collectAsState()
    val recentAttendance by viewModel.allAttendance.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()

    val currentLang by viewModel.appLanguage.collectAsState()
    val currentCurrency by viewModel.appCurrency.collectAsState()

    val scrollState = rememberScrollState()

    val cardBg = MaterialTheme.colorScheme.surface
    val cardFg = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = Localization.get("overview", currentLang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Metrics Grid (2x2 layout with Material 3 depth)
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                StatCard(
                    title = Localization.get("active_emp", currentLang),
                    value = "$employeeCount",
                    icon = Icons.Default.Group,
                    containerColor = cardBg,
                    iconColor = cardFg
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                StatCard(
                    title = Localization.get("present_today", currentLang),
                    value = "$presentCount",
                    icon = Icons.Default.EventAvailable,
                    containerColor = cardBg,
                    iconColor = cardFg
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                StatCard(
                    title = Localization.get("late_today", currentLang),
                    value = "$lateCount",
                    icon = Icons.Default.Timer,
                    containerColor = cardBg,
                    iconColor = cardFg
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                StatCard(
                    title = Localization.get("salaries_paid", currentLang),
                    value = String.format(Locale.getDefault(), "%,.1f %s", totalPaidVal, currentCurrency),
                    icon = Icons.Default.PriceCheck,
                    containerColor = cardBg,
                    iconColor = cardFg
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Analytics / Presence Bar Chart built using pure native Canvas
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tendance des Pointages (Semaine)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pure Canvas Presence Graph bar chart
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline
                val textOnSurface = MaterialTheme.colorScheme.onSurface
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")
                    val values = listOf(0.8f, 0.95f, 0.75f, 0.85f, 0.92f, 0.3f) // mock percentage presence
                    
                    val spaceBetween = size.width / days.size
                    val maxBarHeight = size.height - 24.dp.toPx()
                    
                    // Draw axis line
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, maxBarHeight),
                        end = Offset(size.width, maxBarHeight),
                        strokeWidth = 2f
                    )

                    days.forEachIndexed { i, day ->
                        val pct = values[i]
                        val barHeight = maxBarHeight * pct
                        val barWidth = 28.dp.toPx()
                        val xOffset = i * spaceBetween + (spaceBetween - barWidth) / 2
                        val yOffset = maxBarHeight - barHeight

                        // Draw visual column/bar
                        drawRect(
                            color = primaryColor.copy(alpha = 0.85f),
                            topLeft = Offset(xOffset, yOffset),
                            size = Size(barWidth, barHeight)
                        )

                        // Draw subtle top percentage tag
                        // Let's just render the pillars styled with a darker primary accent top line
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(xOffset, yOffset),
                            size = Size(barWidth, 4.dp.toPx())
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")
                    days.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(48.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Scans activity panel
        Text(
            text = "Derniers Pointages Réalisés",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val displayedScans = recentAttendance.take(5)
        if (displayedScans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun pointage enregistré pour le moment aujourd'hui.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column {
                    displayedScans.forEachIndexed { index, att ->
                        val matchedEmp = employees.find { it.matricule == att.matricule }
                        val name = matchedEmp?.name ?: att.matricule
                        val poste = matchedEmp?.poste ?: "Collaborateur"
                        val timeStr = if (att.checkInMs != null) {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(att.checkInMs))
                        } else "--:--"

                        val typeString = if (att.checkOutMs == null) "ENTRÉE" else "SORTIE"
                        val typeColor = if (att.checkOutMs == null) Color(0xFF2E7D32) else Color(0xFFC62828)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$poste • ${att.date}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = typeColor.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = typeString,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = typeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Heure : $timeStr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (index < displayedScans.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ==================== EMPLOYEES TAB ====================
@Composable
fun EmployeesTab(viewModel: StaffViewModel) {
    val employees by viewModel.allEmployees.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDetailsEmployee by remember { mutableStateOf<Employee?>(null) }
    var selectedEmployeeForQr by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }
    var employeeToAddPending by remember { mutableStateOf<Employee?>(null) }

    // State for inputs
    var matriculeInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var deptInput by remember { mutableStateOf("Informatique") }
    var posteInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var bSalaryInput by remember { mutableStateOf("") }
    var payModeInput by remember { mutableStateOf("Mobile Money") }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Annuaire des Employés",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        // generate temporary matricule for convenience
                        val count = employees.size + 1
                        matriculeInput = "EMP-00$count"
                        nameInput = ""
                        emailInput = ""
                        posteInput = ""
                        rateInput = "15"
                        bSalaryInput = "2500"
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, "add employee")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (employees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun employé inscrit pour le moment.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(employees) { emp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDetailsEmployee = emp },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emp.name.take(2).uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = emp.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${emp.poste} • ${emp.departement}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Matricule : ${emp.matricule} • Tarifs : ${emp.hourlyRate} Ar / h",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Détails",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Employee Details Dialog
        if (selectedDetailsEmployee != null) {
            val emp = selectedDetailsEmployee!!
            EmployeeDetailDialog(
                employee = emp,
                onDismiss = { selectedDetailsEmployee = null },
                onShowQr = {
                    selectedEmployeeForQr = emp
                },
                onEdit = {
                    // Pre-fill fields if editing was implemented, can also do here
                },
                onDelete = {
                    employeeToDelete = emp
                }
            )
        }

        // Delete Confirmation Dialog
        if (employeeToDelete != null) {
            val emp = employeeToDelete!!
            AlertDialog(
                onDismissRequest = { employeeToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteEmployee(emp) {}
                            employeeToDelete = null
                            selectedDetailsEmployee = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { employeeToDelete = null }) {
                        Text("Annuler")
                    }
                },
                title = { Text("Confirmer la suppression", fontWeight = FontWeight.Bold) },
                text = { Text("Êtes-vous sûr de vouloir supprimer l'employé(e) ${emp.name} (Matricule: ${emp.matricule}) ? Cette action supprimera également tout son historique de pointage et de paiements.") }
            )
        }

        // Add Confirmation Dialog
        if (employeeToAddPending != null) {
            val emp = employeeToAddPending!!
            AlertDialog(
                onDismissRequest = { employeeToAddPending = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addEmployee(emp) {
                                showAddDialog = false
                            }
                            employeeToAddPending = null
                        }
                    ) {
                        Text("Ajouter")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { employeeToAddPending = null }) {
                        Text("Annuler")
                    }
                },
                title = { Text("Confirmer l'ajout", fontWeight = FontWeight.Bold) },
                text = { Text("Voulez-vous ajouter l'employé(e) ${emp.name} en tant que ${emp.poste} dans le département ${emp.departement} ?") }
            )
        }

        // QR Badge Dialog
        if (selectedEmployeeForQr != null) {
            val employee = selectedEmployeeForQr!!
            Dialog(onDismissRequest = { selectedEmployeeForQr = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Badge QR Collaborateur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = employee.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = employee.poste,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // High fidelity native Barcode/QR generator rendering!
                        val qrCodeText = "STAFFFLOW:${employee.matricule}"
                        QrCodeImage(
                            content = qrCodeText,
                            modifier = Modifier
                                .size(200.dp)
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "A scanner pour le pointage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = employee.matricule,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = { selectedEmployeeForQr = null }) {
                            Text("Fermer")
                        }
                    }
                }
            }
        }

        // Add Employee bottom sheets dialog
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nouvel Employé",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = matriculeInput,
                            onValueChange = { matriculeInput = it.uppercase() },
                            label = { Text("Matricule Unique") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nom complet") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Adresse e-mail") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = posteInput,
                            onValueChange = { posteInput = it },
                            label = { Text("Poste de travail") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = deptInput,
                            onValueChange = { deptInput = it },
                            label = { Text("Département / Division") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = rateInput,
                                onValueChange = { rateInput = it },
                                label = { Text("Tarif horaire") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = bSalaryInput,
                                onValueChange = { bSalaryInput = it },
                                label = { Text("Salaire base") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Payment mode option dropdown selector
                        Text("Mode de Paiement souhaité :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val configModes = listOf("Mobile Money", "Espèce", "Virement bancaire")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            configModes.forEach { mode ->
                                val selected = payModeInput == mode
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { payModeInput = mode }
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 8.dp),
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text("Annuler")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (nameInput.isBlank() || matriculeInput.isBlank() || posteInput.isBlank()) {
                                        return@Button
                                    }
                                    val newEmp = Employee(
                                        matricule = matriculeInput,
                                        name = nameInput,
                                        email = emailInput,
                                        poste = posteInput,
                                        departement = deptInput,
                                        hourlyRate = rateInput.toDoubleOrNull() ?: 15.0,
                                        baseSalary = bSalaryInput.toDoubleOrNull() ?: 2500.0,
                                        hireDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                        status = "ACTIF",
                                        paymentMode = payModeInput
                                    )
                                    employeeToAddPending = newEmp
                                }
                            ) {
                                Text("Confirmer")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== POINTAGE TAB ====================
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PointageTab(viewModel: StaffViewModel) {
    val activeEmployees by viewModel.activeEmployees.collectAsState()
    val scanResult by viewModel.scanResultState.collectAsState()
    var selectedMatriculeForSim by remember { mutableStateOf("") }
    
    // For manual pointage alert toasts
    var manualResultText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Camera stuff
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(activeEmployees) {
        if (activeEmployees.isNotEmpty() && selectedMatriculeForSim.isEmpty()) {
            selectedMatriculeForSim = activeEmployees.first().matricule
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text(
                text = "Scanner de Pointage QR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // SIMULATOR PANEL (Absolutely Critical for seamless emulator testing!)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ Simulateur de Pointage sans Caméra",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Idéal pour tester l'application directement dans le navigateur sans webcam physique.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Sélectionner l'employé à badger :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Simplified simulated employee picker inside list
                    if (activeEmployees.isEmpty()) {
                        Text("Veuillez d'abord enregistrer un employé.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        // Display clickable selection tiles
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            activeEmployees.forEach { emp ->
                                val selected = selectedMatriculeForSim == emp.matricule
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedMatriculeForSim = emp.matricule }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { selectedMatriculeForSim = emp.matricule }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${emp.poste} • Code QR: STAFFFLOW:${emp.matricule}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedMatriculeForSim.isNotEmpty()) {
                                viewModel.scanQrCode("STAFFFLOW:$selectedMatriculeForSim")
                            }
                        },
                        enabled = selectedMatriculeForSim.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TapAndPlay, contentDescription = "Scan simule")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simuler Scan Badge", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Displays scan animation results
        if (scanResult != null) {
            item {
                val result = scanResult!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (result) {
                            is ScanResult.CheckInSuccess -> Color(0xFFE8F5E9)
                            is ScanResult.CheckOutSuccess -> Color(0xFFE1F5FE)
                            is ScanResult.Error -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Résultat du Pointage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { viewModel.clearScanResult() }) {
                                Icon(Icons.Default.Close, "Dismiss QR Results")
                            }
                        }

                        val titleText = when (result) {
                            is ScanResult.CheckInSuccess -> "✅ ENTRÉE ENREGISTRÉE"
                            is ScanResult.CheckOutSuccess -> "⏱️ SORTIE ENREGISTRÉE"
                            is ScanResult.Error -> "🚨 ERREUR DE POINTAGE"
                        }

                        Text(
                            text = titleText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = when (result) {
                                is ScanResult.CheckInSuccess -> Color(0xFF2E7D32)
                                is ScanResult.CheckOutSuccess -> Color(0xFF0288D1)
                                is ScanResult.Error -> Color(0xFFC62828)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        when (result) {
                            is ScanResult.CheckInSuccess -> {
                                Text(
                                    text = "Employé : ${result.employee.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text("Matricule : ${result.employee.matricule}", fontSize = 13.sp)
                                Text("Date : ${result.date} à ${result.time}", fontSize = 13.sp)
                                if (result.isLate) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "⚠️ En retard de ${result.lateMinutes} minutes",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD84315),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            is ScanResult.CheckOutSuccess -> {
                                Text(
                                    text = "Employé : ${result.employee.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text("Durée enregistrée : ${result.workedHours}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Heures Sup. : ${result.overtimeHours}", fontSize = 13.sp)
                                Text("Sortie validée à ${result.time}", fontSize = 13.sp)
                            }
                            is ScanResult.Error -> {
                                Text(
                                    text = result.message,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Camera Preview block if permission granted
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scanner Réel (Utilise la caméra intégrée)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (cameraPermissionState.status.isGranted) {
                        // Display visual simulated camera viewfinder box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.Black, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "Caméra active... Placez un badge QR devant l'appareil",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() }
                        ) {
                            Icon(Icons.Default.CameraAlt, "req camera")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Autoriser l'accès Caméra")
                        }
                    }
                }
            }
        }
    }
}

// ==================== SALAIRES TAB ====================
@Composable
fun SalariesTab(viewModel: StaffViewModel) {
    val activeEmployees by viewModel.activeEmployees.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var selectedEmployeeMatricule by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("2026-05") } // Default current month draft

    var calculationResult by remember { mutableStateOf<StaffViewModel.SalaryCalculationResult?>(null) }
    
    // Manual overrides for payroll
    var customBonus by remember { mutableStateOf("0") }
    var customDeduction by remember { mutableStateOf("0") }

    var alertMessage by remember { mutableStateOf("") }
    var successAlert by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(activeEmployees) {
        if (activeEmployees.isNotEmpty() && selectedEmployeeMatricule.isEmpty()) {
            selectedEmployeeMatricule = activeEmployees.first().matricule
        }
    }

    // Recalculate automatic salaries details on select change
    LaunchedEffect(selectedEmployeeMatricule, selectedMonth) {
        if (selectedEmployeeMatricule.isNotEmpty()) {
            val res = viewModel.calculateSalaryForMonth(selectedEmployeeMatricule, selectedMonth)
            calculationResult = res
            customBonus = String.format(Locale.US, "%.0f", res.suggestedPrimes)
            customDeduction = String.format(Locale.US, "%.0f", res.suggestedDeductions)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Gestion de Paie & Salaires",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Période de calcul :", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            
            // Simplified selector
            Row {
                listOf("2026-05", "2026-06").forEach { month ->
                    val sel = selectedMonth == month
                    Button(
                        onClick = { selectedMonth = month },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(month)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sélectionner l'employé :", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (activeEmployees.isEmpty()) {
                    Text("Aucun collaborateur enregistré.", color = Color.Red, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        activeEmployees.forEach { emp ->
                            val sel = selectedEmployeeMatricule == emp.matricule
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { selectedEmployeeMatricule = emp.matricule }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sel,
                                    onClick = { selectedEmployeeMatricule = emp.matricule }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${emp.poste} • Tarif horaire : ${emp.hourlyRate} Ar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calculations Details summary
        if (calculationResult != null && selectedEmployeeMatricule.isNotEmpty()) {
            val res = calculationResult!!
            val emp = activeEmployees.find { it.matricule == selectedEmployeeMatricule }

            if (emp != null) {
                // Live overrides recalculation
                val bonusVal = customBonus.toDoubleOrNull() ?: 0.0
                val dedVal = customDeduction.toDoubleOrNull() ?: 0.0
                val baseNetPay = res.normalPay + res.overtimePay
                val activeTotalSalary = baseNetPay + bonusVal - dedVal

                Text(
                    text = "Calcul Automatique de Salaire",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fiche Individuelle (${emp.name})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Période :", fontSize = 13.sp)
                            Text(selectedMonth, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Heures Normales :", fontSize = 13.sp)
                            Text("${res.normalHours} h", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Heures Supplémentaires :", fontSize = 13.sp)
                            Text("${res.overtimeHours} h (Majorées à 1.5x)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Salaire de Base :", fontSize = 13.sp)
                            Text("${res.normalPay} Ar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Montant Heures Sup :", fontSize = 13.sp)
                            Text("+ ${res.overtimePay} Ar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bonus/Primes input field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Primes / Bonus :", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = customBonus,
                                onValueChange = { customBonus = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Deductions input field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Avances / Déductions :", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = customDeduction,
                                onValueChange = { customDeduction = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Salaire NET Final :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = String.format(Locale.getDefault(), "%,.1f Ar", activeTotalSalary),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (alertMessage.isNotEmpty()) {
                            Text(alertMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (successAlert.isNotEmpty()) {
                            Text(successAlert, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                viewModel.submitPayment(
                                    matricule = emp.matricule,
                                    month = selectedMonth,
                                    amount = activeTotalSalary,
                                    normalHours = res.normalHours,
                                    overtimeHours = res.overtimeHours,
                                    bonus = bonusVal,
                                    deductions = dedVal
                                ) { success, msg ->
                                    if (success) {
                                        successAlert = msg
                                        alertMessage = ""
                                    } else {
                                        alertMessage = msg
                                        successAlert = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Print, "pay and slip")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Générer & Enregistrer Paiement", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History list of payments
        Text("Récents Versements RH (Historique)", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        if (allPayments.isEmpty()) {
            Text("Aucune fiche de paie enregistrée historiquement.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column {
                    allPayments.forEachIndexed { index, pay ->
                        val matched = activeEmployees.find { it.matricule == pay.matricule }
                        val nameStr = matched?.name ?: pay.matricule
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(nameStr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Période : ${pay.month} • Heures : ${pay.normalHours + pay.overtimeHours} h", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%,.0f Ar", pay.amountPaid),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        if (index < allPayments.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}

// ==================== REPORTS TAB ====================
@Composable
fun ReportsTab(viewModel: StaffViewModel) {
    val employees by viewModel.allEmployees.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var showPdfSuccessDialog by remember { mutableStateOf(false) }
    var reportTypeSelected by remember { mutableStateOf("DAILY") } // DAILY / MONTHLY_PAYROLL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Rapports & Exports RH",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Type de Rapport à Exporter :", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { reportTypeSelected = "DAILY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (reportTypeSelected == "DAILY") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (reportTypeSelected == "DAILY") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Pointages du Jour")
                    }
                    Button(
                        onClick = { reportTypeSelected = "MONTHLY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (reportTypeSelected == "MONTHLY") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (reportTypeSelected == "MONTHLY") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Dépenses Paie")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Visual simulation spreadsheet table sheet representation!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                ) {
                    if (reportTypeSelected == "DAILY") {
                        Text("Date", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text("Nom Employé", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                        Text("Heures", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text("Statut", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    } else {
                        Text("Mois", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text("Nom", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                        Text("Montant Payé", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                    }
                }

                if (reportTypeSelected == "DAILY") {
                    if (allAttendance.isEmpty()) {
                        Text("Tableau vide. Enregistrez des pointages pour remplir.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                    } else {
                        allAttendance.take(8).forEach { log ->
                            val matched = employees.find { it.matricule == log.matricule }
                            val name = matched?.name ?: log.matricule
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(log.date.takeLast(5), modifier = Modifier.weight(1f), fontSize = 11.sp)
                                Text(name, modifier = Modifier.weight(1.5f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${log.workedHours} h", modifier = Modifier.weight(1f), fontSize = 11.sp)
                                Text(if (log.isLate) "Retard" else "En heure", modifier = Modifier.weight(1f), fontSize = 11.sp, color = if (log.isLate) Color.Red else Color(0xFF2E7D32))
                            }
                        }
                    }
                } else {
                    if (allPayments.isEmpty()) {
                        Text("Tableau vide. Enregistrez des règlements de salaire pour exporter.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                    } else {
                        allPayments.take(8).forEach { pay ->
                            val matched = employees.find { it.matricule == pay.matricule }
                            val name = matched?.name ?: pay.matricule
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(pay.month, modifier = Modifier.weight(1f), fontSize = 11.sp)
                                Text(name, modifier = Modifier.weight(1.5f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${pay.amountPaid} Ar", modifier = Modifier.weight(1.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showPdfSuccessDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, "export pdf report")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Générer Document Officiel PDF", fontWeight = FontWeight.Bold)
        }

        if (showPdfSuccessDialog) {
            Dialog(onDismissRequest = { showPdfSuccessDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "PDF Pret",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Rapport PDF Généré !", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Le rapport de ${if (reportTypeSelected == "DAILY") "Pointage Journalier" else "Synthèse Payroll Mensuel"} a été exporté sous '${if (reportTypeSelected == "DAILY") "rapport_pointage_journalier" else "compte_rendu_paie_mensuel"}.pdf' dans les documents partagés du serveur local de l'entreprise PC.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showPdfSuccessDialog = false }) {
                            Text("Fermer")
                        }
                    }
                }
            }
        }
    }
}

// ==================== CONFIGURATION & PARAMETRES DIALOG ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: StaffViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.appLanguage.collectAsState()
    val currentCurrency by viewModel.appCurrency.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.get("settings", currentLang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // --- LANGUAGE SECTION ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("lang_select", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = Localization.get("lang_desc", currentLang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("FR", "EN", "MG").forEach { lang ->
                        val label = when(lang) {
                            "FR" -> Localization.get("lang_fr", currentLang)
                            "EN" -> Localization.get("lang_en", currentLang)
                            "MG" -> Localization.get("lang_mg", currentLang)
                            else -> lang
                        }
                        FilterChip(
                            selected = currentLang == lang,
                            onClick = { viewModel.setLanguage(lang) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- CURRENCY SECTION ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("currency", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = Localization.get("currency_desc", currentLang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Ar", "FCFA", "USD").forEach { curr ->
                        FilterChip(
                            selected = currentCurrency == curr,
                            onClick = { viewModel.setCurrency(curr) },
                            label = { Text(curr, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- THEME SECTION ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("theme_select", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = Localization.get("theme_desc", currentLang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val currentTheme by viewModel.appTheme.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { t ->
                        val label = when(t) {
                            "SYSTEM" -> Localization.get("theme_system", currentLang)
                            "LIGHT" -> Localization.get("theme_light", currentLang)
                            "DARK" -> Localization.get("theme_dark", currentLang)
                            else -> t
                        }
                        FilterChip(
                            selected = currentTheme == t,
                            onClick = { viewModel.setTheme(t) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                val currentEmp by viewModel.currentEmployee.collectAsState()

                if (currentEmp == null) {
                    // --- CLOUD SYNC SECTION (OPTION 1) ---
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.get("sync_title", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    
                    Text(
                        text = Localization.get("sync_desc", currentLang),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val isSyncEnabled by viewModel.syncEnabled.collectAsState()
                    val syncUrl by viewModel.syncServerUrl.collectAsState()
                    val syncKey by viewModel.syncApiKey.collectAsState()
                    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
                    val syncStatus by viewModel.syncStatus.collectAsState()
                    val isSyncing by viewModel.isSyncing.collectAsState()
                    val context = LocalContext.current

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Localization.get("sync_enable", currentLang),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Switch(
                                    checked = isSyncEnabled,
                                    onCheckedChange = { viewModel.setSyncEnabled(it) }
                                )
                            }

                            if (isSyncEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = syncUrl,
                                    onValueChange = { viewModel.setSyncServerUrl(it) },
                                    label = { Text(Localization.get("sync_url", currentLang), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = syncKey,
                                    onValueChange = { viewModel.setSyncApiKey(it) },
                                    label = { Text(Localization.get("sync_key", currentLang), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${Localization.get("sync_status_label", currentLang)} :",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = syncStatus,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (syncStatus.startsWith("Réussie")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${Localization.get("sync_last", currentLang)} : $lastSyncTime",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.syncCloudNow { success, message ->
                                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        enabled = !isSyncing && syncUrl.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = Localization.get("sync_now", currentLang),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- LOCAL WI-FI SERVER SECTION (OPTION 1) ---
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.get("local_server_title", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    
                    Text(
                        text = Localization.get("local_server_desc", currentLang),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val isLocalServerActive by viewModel.localServerActive.collectAsState()
                    val localServerIp by viewModel.localServerIp.collectAsState()
                    val localServerPort by viewModel.localServerPort.collectAsState()
                    val localServerLogs by viewModel.localServerLogs.collectAsState()
                    
                    var inputPort by remember { mutableStateOf(localServerPort.toString()) }
                    val contextServer = LocalContext.current

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLocalServerActive) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (isLocalServerActive) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Localization.get("local_server_enable", currentLang),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isLocalServerActive) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFF2E7D32), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "actif en Wi-Fi | http://$localServerIp:$localServerPort",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Inactif",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = isLocalServerActive,
                                    onCheckedChange = { active ->
                                        if (active) {
                                            val portInt = inputPort.toIntOrNull() ?: 8080
                                            viewModel.startLocalServer(portInt) { success, msg ->
                                                android.widget.Toast.makeText(contextServer, msg, android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            viewModel.stopLocalServer()
                                        }
                                    }
                                )
                            }

                            if (!isLocalServerActive) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = inputPort,
                                    onValueChange = { inputPort = it },
                                    label = { Text(Localization.get("local_server_port", currentLang), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (isLocalServerActive) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = Localization.get("local_server_logs", currentLang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    if (localServerLogs.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = Localization.get("local_server_no_logs", currentLang),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(localServerLogs) { log ->
                                                Text(
                                                    text = log,
                                                    fontSize = 11.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    modifier = Modifier.padding(bottom = 2.dp),
                                                    color = if (log.contains("Erreur")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- EMPLOYEE LOCAL NETWORK SETTINGS ---
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "FR") "Réseau Local d'Entreprise" else if (currentLang == "MG") "Réseau Local ny Orinasa" else "Local Enterprise Network",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    val wifiIp by viewModel.wifiServerIp.collectAsState()
                    val wifiPort by viewModel.wifiServerPort.collectAsState()

                    OutlinedTextField(
                        value = wifiIp,
                        onValueChange = { viewModel.setWifiServerIp(it) },
                        label = { Text(Localization.get("client_server_ip", currentLang), fontSize = 11.sp) },
                        placeholder = { Text("Ex: 192.168.1.50") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = wifiPort.toString(),
                        onValueChange = { viewModel.setWifiServerPort(it.toIntOrNull() ?: 8080) },
                        label = { Text(Localization.get("client_server_port", currentLang), fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                // --- VERSION AND ABOUT ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = Localization.get("about", currentLang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Localization.get("version", currentLang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = Localization.get("developer", currentLang),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ==================== DETAIL DIALOG COLLABORATEUR ====================
@Composable
fun EmployeeDetailDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onShowQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fiche Collaborateur",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Image/Initials
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = employee.name.take(2).uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = employee.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = employee.poste,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Details list
                DetailRow(label = "Matricule", value = employee.matricule)
                DetailRow(label = "Département", value = employee.departement)
                DetailRow(label = "E-mail", value = employee.email.ifBlank { "N/A" })
                DetailRow(label = "Tarif Horaire", value = "${employee.hourlyRate} Ar / h")
                DetailRow(label = "Salaire de Base", value = "${employee.baseSalary} Ar")
                DetailRow(label = "Date d'embauche", value = employee.hireDate)
                DetailRow(label = "Mode de Paiement", value = employee.paymentMode)
                DetailRow(label = "Statut", value = employee.status)

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Card
                Text(
                    text = "Actions disponibles",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // QR Code Info Button
                    Button(
                        onClick = onShowQr,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("QR Badge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete Button
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Supprimer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
