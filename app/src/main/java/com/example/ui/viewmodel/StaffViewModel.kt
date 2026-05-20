package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.ScanResult
import com.example.data.repository.StaffRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Inet4Address
import java.net.URLDecoder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers

class StaffViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StaffRepository

    // Session State
    private val _currentAdmin = MutableStateFlow<Admin?>(null)
    val currentAdmin: StateFlow<Admin?> = _currentAdmin.asStateFlow()

    private val _currentEmployee = MutableStateFlow<Employee?>(null)
    val currentEmployee: StateFlow<Employee?> = _currentEmployee.asStateFlow()

    // Settings Configuration
    private val _appLanguage = MutableStateFlow("FR")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _appCurrency = MutableStateFlow("Ar")
    val appCurrency: StateFlow<String> = _appCurrency.asStateFlow()

    private val _appTheme = MutableStateFlow("SYSTEM")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    // Sync Option 1 States
    private val _syncEnabled = MutableStateFlow(false)
    val syncEnabled: StateFlow<Boolean> = _syncEnabled.asStateFlow()

    private val _syncServerUrl = MutableStateFlow("https://api.staffflow.io/v1")
    val syncServerUrl: StateFlow<String> = _syncServerUrl.asStateFlow()

    private val _syncApiKey = MutableStateFlow("")
    val syncApiKey: StateFlow<String> = _syncApiKey.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Jamais")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _syncStatus = MutableStateFlow("Inactif")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Option 1 Local Network Server States (Admin)
    private val _localServerActive = MutableStateFlow(false)
    val localServerActive: StateFlow<Boolean> = _localServerActive.asStateFlow()

    private val _localServerIp = MutableStateFlow("127.0.0.1")
    val localServerIp: StateFlow<String> = _localServerIp.asStateFlow()

    private val _localServerPort = MutableStateFlow(8080)
    val localServerPort: StateFlow<Int> = _localServerPort.asStateFlow()

    private val _localServerLogs = MutableStateFlow<List<String>>(emptyList())
    val localServerLogs: StateFlow<List<String>> = _localServerLogs.asStateFlow()

    // Option 1 Local Network Client States (Employee)
    private val _wifiServerIp = MutableStateFlow("")
    val wifiServerIp: StateFlow<String> = _wifiServerIp.asStateFlow()

    private val _wifiServerPort = MutableStateFlow(8080)
    val wifiServerPort: StateFlow<Int> = _wifiServerPort.asStateFlow()

    fun setLanguage(lang: String) {
        _appLanguage.value = lang
    }

    fun setCurrency(curr: String) {
        _appCurrency.value = curr
    }

    fun setTheme(theme: String) {
        _appTheme.value = theme
    }

    fun setSyncEnabled(enabled: Boolean) {
        _syncEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("sync_enabled", enabled).apply()
    }

    fun setSyncServerUrl(url: String) {
        _syncServerUrl.value = url
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("sync_server_url", url).apply()
    }

    fun setSyncApiKey(key: String) {
        _syncApiKey.value = key
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("sync_api_key", key).apply()
    }

    fun setLastSyncTime(time: String) {
        _lastSyncTime.value = time
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("last_sync_time", time).apply()
    }

    fun setSyncStatus(status: String) {
        _syncStatus.value = status
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("sync_status", status).apply()
    }

    fun syncCloudNow(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Synchronisation..."
            try {
                val employees = allEmployees.value
                val attendance = allAttendance.value
                val payments = allPayments.value

                // Instantiate clean api sync framework
                val api = com.example.data.network.SyncService.createApi(_syncServerUrl.value)
                val token = if (_syncApiKey.value.isNotBlank()) "Bearer ${_syncApiKey.value}" else "Bearer demo_key_123"

                // Launch parallel/sequential requests to sync all tables
                val empResponse = api.syncEmployees(token, employees)
                val attResponse = api.syncAttendance(token, attendance)
                val payResponse = api.syncPayments(token, payments)

                if (empResponse.isSuccessful && attResponse.isSuccessful && payResponse.isSuccessful) {
                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                    setLastSyncTime(dateStr)
                    setSyncStatus("Réussie ($dateStr)")
                    _isSyncing.value = false
                    onResult(true, "Synchronisation cloud terminée avec succès !")
                } else {
                    val errCode = "${empResponse.code()}/${attResponse.code()}/${payResponse.code()}"
                    setSyncStatus("Erreur Code $errCode")
                    _isSyncing.value = false
                    onResult(false, "Code HTTP d'erreur retourné : $errCode. Veuillez vérifier l'authentification.")
                }
            } catch (e: Exception) {
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                setSyncStatus("Échec de connexion")
                _isSyncing.value = false
                val cleanErrMsg = e.localizedMessage ?: "Hôte ou réseau introuvable"
                onResult(false, "Échec de connexion au serveur : $cleanErrMsg. Veuillez vous assurer que le serveur à l'adresse ${_syncServerUrl.value} est en ligne et accepte les requêtes JSON.")
            }
        }
    }

    // Status messages for scanners/actions
    private val _scanResultState = MutableStateFlow<ScanResult?>(null)
    val scanResultState: StateFlow<ScanResult?> = _scanResultState.asStateFlow()

    // Exposures of database data flows
    val allEmployees: StateFlow<List<Employee>>
    val activeEmployees: StateFlow<List<Employee>>
    val allAttendance: StateFlow<List<Attendance>>
    val allPayments: StateFlow<List<Payment>>

    // Calculated Dashboard stats
    val activeEmployeesCount: StateFlow<Int>
    val presentTodayCount: StateFlow<Int>
    val lateTodayCount: StateFlow<Int>
    val totalPayrollSpent: StateFlow<Double>

    init {
        val prefs = application.getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        _syncEnabled.value = prefs.getBoolean("sync_enabled", false)
        _syncServerUrl.value = prefs.getString("sync_server_url", "https://api.staffflow.io/v1") ?: "https://api.staffflow.io/v1"
        _syncApiKey.value = prefs.getString("sync_api_key", "") ?: ""
        _lastSyncTime.value = prefs.getString("last_sync_time", "Jamais") ?: "Jamais"
        _syncStatus.value = prefs.getString("sync_status", "Inactif") ?: "Inactif"

        _wifiServerIp.value = prefs.getString("wifi_server_ip", "") ?: ""
        _wifiServerPort.value = prefs.getInt("wifi_server_port", 8080)

        val db = AppDatabase.getDatabase(application)
        repository = StaffRepository(
            db.adminDao(),
            db.employeeDao(),
            db.attendanceDao(),
            db.paymentDao(),
            db.qrCodeDao()
        )

        // Seed default admin if none exists
        viewModelScope.launch {
            if (repository.getAdminCount() == 0) {
                repository.registerAdmin(
                    Admin(username = "admin", passwordHash = "admin", role = "SUPER_ADMIN")
                )
                // Seed a couple of example employees for quick demo
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repository.insertEmployee(
                    Employee(
                        matricule = "EMP-001",
                        name = "Jean Dupont",
                        email = "jean.dupont@company.com",
                        poste = "Développeur Senior",
                        departement = "Informatique",
                        hourlyRate = 25.0,
                        baseSalary = 4000.0,
                        hireDate = todayStr,
                        status = "ACTIF",
                        paymentMode = "Mobile Money"
                    )
                )
                repository.insertEmployee(
                    Employee(
                        matricule = "EMP-002",
                        name = "Marie Koné",
                        email = "marie.kone@company.com",
                        poste = "Responsable RH",
                        departement = "Ressources Humaines",
                        hourlyRate = 22.0,
                        baseSalary = 3500.0,
                        hireDate = todayStr,
                        status = "ACTIF",
                        paymentMode = "Virement Bancaire"
                    )
                )
            }
        }

        allEmployees = repository.allEmployees.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        activeEmployees = repository.activeEmployees.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        allAttendance = repository.allAttendance.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        allPayments = repository.allPayments.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        activeEmployeesCount = repository.activeEmployeeCountFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )

        presentTodayCount = repository.getPresentCountTodayFlow().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )

        lateTodayCount = repository.getLateCountTodayFlow().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )

        totalPayrollSpent = repository.totalPaidFlow.map { it ?: 0.0 }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
        )
    }

    // ---------------- AUTH ACTIONS ----------------

    fun adminLogin(username: String, passwordHash: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val admin = repository.findAdminByUsername(username)
            if (admin != null && admin.passwordHash == passwordHash) {
                _currentAdmin.value = admin
                _currentEmployee.value = null
                onResult(true, "Connexion réussie")
            } else {
                onResult(false, "Identifiants incorrects")
            }
        }
    }

    fun registerAdmin(username: String, passwordHash: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.findAdminByUsername(username)
            if (existing != null) {
                onResult(false, "Ce nom d'utilisateur est déjà utilisé")
            } else {
                val newAdmin = Admin(username = username, passwordHash = passwordHash, role = role)
                repository.registerAdmin(newAdmin)
                onResult(true, "Compte administrateur créé avec succès")
            }
        }
    }

    fun registerEmployee(employee: Employee, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getEmployeeByMatricule(employee.matricule)
            if (existing != null) {
                onResult(false, "Ce numéro matricule est déjà utilisé")
            } else {
                repository.insertEmployee(employee)
                onResult(true, "Compte collaborateur créé avec succès")
            }
        }
    }

    fun employeeLogin(matricule: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val employee = repository.getEmployeeByMatricule(matricule)
            if (employee != null) {
                if (employee.status == "ACTIF") {
                    _currentEmployee.value = employee
                    _currentAdmin.value = null
                    onResult(true, "Connexion réussie")
                } else {
                    onResult(false, "Ce compte employé est inactif")
                }
            } else {
                onResult(false, "Matricule introuvable")
            }
        }
    }

    fun logout() {
        _currentAdmin.value = null
        _currentEmployee.value = null
    }

    // ---------------- EMPLOYEE MANAGEMENT ----------------

    fun addEmployee(employee: Employee, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertEmployee(employee)
            onComplete()
        }
    }

    fun updateEmployee(employee: Employee, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
            // If current logged employee is edited, update the session flow
            if (_currentEmployee.value?.matricule == employee.matricule) {
                _currentEmployee.value = employee
            }
            onComplete()
        }
    }

    fun deleteEmployee(employee: Employee, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
            onComplete()
        }
    }

    // ---------------- LOCAL WI-FI SERVER & CLIENT (OPTION 1) ----------------

    fun setWifiServerIp(ip: String) {
        _wifiServerIp.value = ip
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("wifi_server_ip", ip).apply()
    }

    fun setWifiServerPort(port: Int) {
        _wifiServerPort.value = port
        val prefs = getApplication<Application>().getSharedPreferences("staffflow_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putInt("wifi_server_port", port).apply()
    }

    fun getWifiIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                for (address in Collections.list(networkInterface.inetAddresses)) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val hostAddress = address.hostAddress ?: ""
                        if (hostAddress.startsWith("192.168.") || hostAddress.startsWith("10.") || hostAddress.startsWith("172.")) {
                            return hostAddress
                        }
                    }
                }
            }
            // Fallback
            val interfaces2 = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces2)) {
                for (address in Collections.list(networkInterface.inetAddresses)) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "192.168.1.50"
    }

    private var httpServer: HttpServer? = null

    fun startLocalServer(port: Int = 8080, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopLocalServerInternal()

                val ip = getWifiIpAddress()
                _localServerIp.value = ip
                _localServerPort.value = port

                val server = HttpServer.create(InetSocketAddress(port), 0)
                
                // Root status check
                server.createContext("/") { exchange ->
                    val responseText = "StaffFlow Local Server is Running IP: $ip Port: $port"
                    val bytes = responseText.toByteArray(Charsets.UTF_8)
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.write(bytes)
                    exchange.responseBody.close()
                }

                // Pointage endpoint
                server.createContext("/pointage") { exchange ->
                    var responseCode = 400
                    var responseText = "Invalid Request"

                    try {
                        val query = exchange.requestURI.query ?: ""
                        var matricule = ""
                        if (query.isNotBlank()) {
                            val params = query.split("&")
                            for (p in params) {
                                val pair = p.split("=")
                                if (pair.size == 2 && pair[0] == "matricule") {
                                    matricule = URLDecoder.decode(pair[1], "UTF-8")
                                    break
                                }
                            }
                        }

                        if (matricule.isBlank() && exchange.requestMethod == "POST") {
                            val body = exchange.requestBody.bufferedReader().use { it.readText() }
                            if (body.contains("\"matricule\":")) {
                                val regex = Regex("\"matricule\"\\s*:\\s*\"([^\"]+)\"")
                                matricule = regex.find(body)?.groupValues?.get(1) ?: ""
                            } else if (body.startsWith("matricule=")) {
                                matricule = body.substringAfter("matricule=")
                            } else {
                                matricule = body.trim()
                            }
                        }

                        if (matricule.isNotBlank()) {
                            viewModelScope.launch(Dispatchers.Main) {
                                val scanResult = repository.handleQrScan("STAFFFLOW:$matricule")
                                _scanResultState.value = scanResult
                                val resultMessage = when (scanResult) {
                                    is ScanResult.CheckInSuccess -> "Entrée réussie : ${scanResult.employee.name}"
                                    is ScanResult.CheckOutSuccess -> "Sortie réussie : ${scanResult.employee.name}"
                                    is ScanResult.Error -> "Erreur : ${scanResult.message}"
                                }

                                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                addLog("[$currentTime] Pointage de $matricule -> $resultMessage")
                            }

                            responseCode = 200
                            responseText = "SUCCESS:$matricule"
                        } else {
                            responseCode = 400
                            responseText = "ERROR:Missing matricule"
                        }
                    } catch (e: Exception) {
                        responseCode = 500
                        responseText = "ERROR:${e.localizedMessage}"
                    }

                    val bytes = responseText.toByteArray(Charsets.UTF_8)
                    exchange.sendResponseHeaders(responseCode, bytes.size.toLong())
                    exchange.responseBody.write(bytes)
                    exchange.responseBody.close()
                }

                server.executor = null
                server.start()
                httpServer = server

                launch(Dispatchers.Main) {
                    _localServerActive.value = true
                    val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    addLog("[$currentTime] Serveur lancé sur http://$ip:$port")
                    onResult(true, "Serveur Wi-Fi démarré avec succès sur http://$ip:$port")
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    _localServerActive.value = false
                    val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    addLog("[$currentTime] Erreur lancement : ${e.localizedMessage}")
                    onResult(false, "Échec : ${e.localizedMessage}")
                }
            }
        }
    }

    private fun stopLocalServerInternal() {
        httpServer?.let {
            try {
                it.stop(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        httpServer = null
    }

    fun stopLocalServer() {
        stopLocalServerInternal()
        _localServerActive.value = false
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        addLog("[$currentTime] Serveur arrêté.")
    }

    private fun addLog(log: String) {
        val current = _localServerLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 50) current.removeAt(current.size - 1)
        _localServerLogs.value = current
    }

    fun triggerLocalWifiPointage(matricule: String, onResult: (Boolean, String) -> Unit) {
        val serverIp = _wifiServerIp.value
        val serverPort = _wifiServerPort.value
        if (serverIp.isBlank()) {
            onResult(false, "Veuillez d'abord configurer l'IP du serveur Administrateur.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "http://$serverIp:$serverPort/pointage?matricule=$matricule"
                val client = OkHttpClient.Builder()
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .readTimeout(6, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url(url)
                    .post(RequestBody.create(null, ""))
                    .build()
                
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    launch(Dispatchers.Main) {
                        if (response.isSuccessful && responseBody.startsWith("SUCCESS:")) {
                            onResult(true, "Pointage enregistré sur le serveur Administrateur avec succès !")
                        } else {
                            val cleanMsg = responseBody.replace("ERROR:", "")
                            onResult(false, "Le serveur Admin a retourné une erreur : $cleanMsg")
                        }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onResult(false, "Connexion impossible : ${e.localizedMessage}. Vérifiez le Wi-Fi et l'IP du serveur.")
                }
            }
        }
    }

    // ---------------- POINTAGE & SCANNING ----------------

    fun scanQrCode(qrText: String) {
        viewModelScope.launch {
            val result = repository.handleQrScan(qrText)
            _scanResultState.value = result
        }
    }

    fun clearScanResult() {
        _scanResultState.value = null
    }

    // Direct manual pointage helper for quick entry
    fun triggerManualPointage(matricule: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val qrText = "STAFFFLOW:$matricule"
            val result = repository.handleQrScan(qrText)
            _scanResultState.value = result
            when (result) {
                is ScanResult.CheckInSuccess -> onResult("Pointage d'entrée réussi pour ${result.employee.name}")
                is ScanResult.CheckOutSuccess -> onResult("Pointage de sortie réussi pour ${result.employee.name}")
                is ScanResult.Error -> onResult(result.message)
            }
        }
    }

    // Get specific employee logs for Employee view
    fun getAttendanceForEmployee(matricule: String): Flow<List<Attendance>> {
        return repository.getAttendanceByEmployee(matricule)
    }

    fun getPaymentsForEmployee(matricule: String): Flow<List<Payment>> {
        return repository.getPaymentsByEmployee(matricule)
    }

    // ---------------- SALARY COMPUTATION ENGINE ----------------

    data class SalaryCalculationResult(
        val baseHourlyRate: Double,
        val normalHours: Double,
        val overtimeHours: Double,
        val normalPay: Double,
        val overtimePay: Double,
        val suggestedPrimes: Double,
        val suggestedDeductions: Double,
        val totalNetSalary: Double
    )

    suspend fun calculateSalaryForMonth(matricule: String, month: String): SalaryCalculationResult {
        // e.g. month is "2026-05" (matches date string "2026-05-XX")
        val employee = repository.getEmployeeByMatricule(matricule)!!
        val attendances = repository.allAttendance.first().filter {
            it.matricule == matricule && it.date.startsWith(month)
        }

        var totalWorkedHours = 0.0
        var totalOvertimeHours = 0.0
        var lateDaysCount = 0

        attendances.forEach { att ->
            totalWorkedHours += att.workedHours
            totalOvertimeHours += att.overtimeHours
            if (att.isLate) {
                lateDaysCount++
            }
        }

        val normalHours = if (totalWorkedHours > totalOvertimeHours) {
            totalWorkedHours - totalOvertimeHours
        } else {
            totalWorkedHours
        }

        val normalPay = normalHours * employee.hourlyRate
        val overtimePay = totalOvertimeHours * employee.hourlyRate * 1.5 // 1.5x majoration for OT

        // Simple professional formulas for automatic suggested primes and deductions
        // e.g. deduction of 10$ per late check-in
        val suggestedDeductions = lateDaysCount * 10.0
        // e.g. prime of 50$ if worked more than 40 hours inside the month
        val suggestedPrimes = if (totalWorkedHours >= 40.0) 50.0 else 0.0

        val totalNetSalary = normalPay + overtimePay + suggestedPrimes - suggestedDeductions

        return SalaryCalculationResult(
            baseHourlyRate = employee.hourlyRate,
            normalHours = String.format(Locale.US, "%.2f", normalHours).toDouble(),
            overtimeHours = String.format(Locale.US, "%.2f", totalOvertimeHours).toDouble(),
            normalPay = String.format(Locale.US, "%.2f", normalPay).toDouble(),
            overtimePay = String.format(Locale.US, "%.2f", overtimePay).toDouble(),
            suggestedPrimes = suggestedPrimes,
            suggestedDeductions = suggestedDeductions,
            totalNetSalary = String.format(Locale.US, "%.2f", if (totalNetSalary < 0) 0.0 else totalNetSalary).toDouble()
        )
    }

    fun submitPayment(
        matricule: String,
        month: String,
        amount: Double,
        normalHours: Double,
        overtimeHours: Double,
        bonus: Double,
        deductions: Double,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            // Check if already paid
            val existing = repository.getPaymentByEmployeeAndMonth(matricule, month)
            if (existing != null) {
                onComplete(false, "Ce salaire a déjà été payé pour ce mois (${month})")
                return@launch
            }

            val payment = Payment(
                matricule = matricule,
                month = month,
                paymentDateMs = System.currentTimeMillis(),
                amountPaid = amount,
                normalHours = normalHours,
                overtimeHours = overtimeHours,
                bonus = bonus,
                deductions = deductions,
                status = "PAID"
            )
            repository.recordPayment(payment)
            onComplete(true, "Paiement de salaire enregistré avec succès")
        }
    }

    fun withdrawAmount(
        matricule: String,
        amount: Double,
        mode: String,
        number: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val emp = repository.getEmployeeByMatricule(matricule)
            if (emp == null) {
                onResult(false, "Employé introuvable")
                return@launch
            }
            if (amount <= 0) {
                onResult(false, "Montant invalide")
                return@launch
            }
            if (emp.balance < amount) {
                onResult(false, "Solde insuffisant (Disponible: ${emp.balance} Ar)")
                return@launch
            }
            val updatedEmp = emp.copy(balance = emp.balance - amount)
            repository.updateEmployee(updatedEmp)
            // If it is the current logged-in employee, update currentEmployee flow
            if (_currentEmployee.value?.matricule == matricule) {
                _currentEmployee.value = updatedEmp
            }
            
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val payment = Payment(
                matricule = matricule,
                month = "Retrait $mode ($number) - $todayStr",
                paymentDateMs = System.currentTimeMillis(),
                amountPaid = amount,
                normalHours = 0.0,
                overtimeHours = 0.0,
                bonus = 0.0,
                deductions = amount,
                status = "PAID"
            )
            repository.recordPayment(payment)
            onResult(true, "Retrait de ${amount} Ar effectué avec succès via ${mode} (${number})")
        }
    }
}
