package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Employee
import com.example.ui.viewmodel.StaffViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: StaffViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToEmployee: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEmployeeTab by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Admin login inputs
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Admin registration inputs
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regAdminRole by remember { mutableStateOf("ADMIN") } // ADMIN, MANAGER, SUPER_ADMIN
    var showRegPassword by remember { mutableStateOf(false) }

    // Employee login inputs
    var matricule by remember { mutableStateOf("") }

    // Employee registration inputs
    var regMatricule by remember { mutableStateOf("") }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPoste by remember { mutableStateOf("") }
    var regDepartement by remember { mutableStateOf("") }
    var regHourlyRate by remember { mutableStateOf("") }
    var regBaseSalary by remember { mutableStateOf("") }
    var regPaymentMode by remember { mutableStateOf("Mobile Money") } // "Mobile Money", "Espèce", "Virement Bancaire"

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Reset fields on tab/mode change
    LaunchedEffect(isEmployeeTab, isRegisterMode) {
        errorMessage = ""
        successMessage = ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative top brush blob
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Main Branding Section
            Icon(
                imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Lock,
                contentDescription = "Auth Screen Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isRegisterMode) "Créer un Compte" else "Connexion StaffFlow",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isRegisterMode) 
                    "Remplissez les informations ci-dessous" 
                    else "Veuillez vous identifier pour continuer",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Switch tabs ONLY when not registering, or optionally allow tabs in both (so they choose which type of account to create)
            TabRow(
                selectedTabIndex = if (isEmployeeTab) 1 else 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (isEmployeeTab) 1 else 0]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = !isEmployeeTab,
                    onClick = { isEmployeeTab = false },
                    text = { Text("Espace Admin", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isEmployeeTab,
                    onClick = { isEmployeeTab = true },
                    text = { Text("Collaborateur", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // Message alerts (Error / Success)
            if (errorMessage.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Erreur",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer Alerte",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (successMessage.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Succès",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = successMessage,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Authentication Forms Layout
            if (!isEmployeeTab) {
                // ADMIN CONTEXT
                if (!isRegisterMode) {
                    // Admin Log-in Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nom d'utilisateur") },
                            placeholder = { Text("Ex: admin") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Person, "ic_person") },
                            modifier = Modifier.fillMaxWidth().testTag("username_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.VpnKey, "ic_key") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher mot de passe"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Veuillez remplir tous les champs"
                                    return@Button
                                }
                                viewModel.adminLogin(username, password) { success, msg ->
                                    if (success) {
                                        successMessage = "Connexion d'administrateur établie"
                                        errorMessage = ""
                                        onNavigateToAdmin()
                                    } else {
                                        errorMessage = msg
                                        successMessage = ""
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_login_submit")
                        ) {
                            Text("Se Connecter (Admin)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { isRegisterMode = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Nouveau administrateur ? Créer un compte")
                        }
                    }
                } else {
                    // Admin Registration Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Nom d'utilisateur Admin") },
                            placeholder = { Text("Ex: super_rh") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Person, "ic_person") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Mot de passe") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.VpnKey, "ic_key") },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher mot de passe"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Confirmer le mot de passe") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.VpnKey, "ic_key_confirm") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role selection with chips
                        Text(
                            text = "Sélectionner le Rôle Administrateur :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("ADMIN", "MANAGER", "SUPER_ADMIN").forEach { role ->
                                FilterChip(
                                    selected = (regAdminRole == role),
                                    onClick = { regAdminRole = role },
                                    label = { Text(role, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (regUsername.isBlank() || regPassword.isBlank() || regConfirmPassword.isBlank()) {
                                    errorMessage = "Veuillez remplir tous les champs"
                                    return@Button
                                }
                                if (regPassword != regConfirmPassword) {
                                    errorMessage = "Les mots de passe ne correspondent pas"
                                    return@Button
                                }
                                viewModel.registerAdmin(regUsername, regPassword, regAdminRole) { success, msg ->
                                    if (success) {
                                        successMessage = "Administrateur enregistré avec succès ! Veuillez vous connecter."
                                        errorMessage = ""
                                        // Auto-populate for login convenience
                                        username = regUsername
                                        password = regPassword
                                        isRegisterMode = false
                                        // Clear registration fields
                                        regUsername = ""
                                        regPassword = ""
                                        regConfirmPassword = ""
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Enregistrer & Ouvrir de session", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { isRegisterMode = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Déjà inscrit ? Retour à la Connexion")
                        }
                    }
                }
            } else {
                // EMPLOYEE CONTEXT
                if (!isRegisterMode) {
                    // Employee Log-in Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = matricule,
                            onValueChange = { matricule = it.uppercase() },
                            label = { Text("Numéro Matricule de l'employé") },
                            placeholder = { Text("Ex: EMP-001") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Badge, "ic_badge") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (matricule.isBlank()) {
                                    errorMessage = "Veuillez saisir votre matricule"
                                }
                            }),
                            modifier = Modifier.fillMaxWidth().testTag("matricule_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (matricule.isBlank()) {
                                    errorMessage = "Veuillez saisir votre numéro matricule"
                                    return@Button
                                }
                                viewModel.employeeLogin(matricule) { success, msg ->
                                    if (success) {
                                        successMessage = "Espace collaborateur connecté"
                                        errorMessage = ""
                                        onNavigateToEmployee()
                                    } else {
                                        errorMessage = msg
                                        successMessage = ""
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("employee_login_submit")
                        ) {
                            Text("Accéder à Mon Espace", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { isRegisterMode = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Nouveau Collaborateur ? S'enregistrer ici")
                        }
                    }
                } else {
                    // Employee Self-Registration Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = regMatricule,
                            onValueChange = { regMatricule = it.uppercase() },
                            label = { Text("Numéro Matricule") },
                            placeholder = { Text("Ex: EMP-003") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Badge, "ic_badge") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Nom Complet") },
                            placeholder = { Text("Ex: Haingo Razafy") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Person, "ic_name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Adresse Email") },
                            placeholder = { Text("Ex: haingo@company.mg") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Email, "ic_email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regPoste,
                            onValueChange = { regPoste = it },
                            label = { Text("Poste de l'employé") },
                            placeholder = { Text("Ex: Comptable") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Work, "ic_work") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regDepartement,
                            onValueChange = { regDepartement = it },
                            label = { Text("Département") },
                            placeholder = { Text("Ex: Administration") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Business, "ic_dept") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = regHourlyRate,
                                onValueChange = { regHourlyRate = it },
                                label = { Text("Taux Horaire (Ar)") },
                                placeholder = { Text("Ex: 15000") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = regBaseSalary,
                                onValueChange = { regBaseSalary = it },
                                label = { Text("Salaire Base (Ar)") },
                                placeholder = { Text("Ex: 2500000") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Payment mode selection chips
                        Text(
                            text = "Mode de Paiement Préféré :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Mobile Money", "Espèce", "Virement").forEach { mode ->
                                val actualModeName = if (mode == "Virement") "Virement Bancaire" else mode
                                FilterChip(
                                    selected = (regPaymentMode == actualModeName),
                                    onClick = { regPaymentMode = actualModeName },
                                    label = { Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (regMatricule.isBlank() || regName.isBlank() || regEmail.isBlank() || regPoste.isBlank() || regHourlyRate.isBlank() || regBaseSalary.isBlank()) {
                                    errorMessage = "Veuillez remplir tous les champs obligatoires"
                                    return@Button
                                }
                                val hourly = regHourlyRate.toDoubleOrNull() ?: 0.0
                                val base = regBaseSalary.toDoubleOrNull() ?: 0.0
                                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                                val newEmp = Employee(
                                    matricule = regMatricule.trim().uppercase(),
                                    name = regName.trim(),
                                    email = regEmail.trim(),
                                    photoUri = null,
                                    poste = regPoste.trim(),
                                    departement = regDepartement.trim(),
                                    hourlyRate = hourly,
                                    baseSalary = base,
                                    hireDate = todayStr,
                                    status = "ACTIF",
                                    paymentMode = regPaymentMode
                                )

                                viewModel.registerEmployee(newEmp) { success, msg ->
                                    if (success) {
                                        successMessage = "Compte créé! badge QR généré automatiquement pour ${newEmp.name}."
                                        errorMessage = ""
                                        // Move back to login tab with fields set
                                        matricule = regMatricule.uppercase()
                                        isRegisterMode = false
                                        // Reset registration data
                                        regMatricule = ""
                                        regName = ""
                                        regEmail = ""
                                        regPoste = ""
                                        regDepartement = ""
                                        regHourlyRate = ""
                                        regBaseSalary = ""
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Enregistrer Mon Compte Collaborateur", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { isRegisterMode = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Déjà inscrit ? Retour à la Connexion")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
