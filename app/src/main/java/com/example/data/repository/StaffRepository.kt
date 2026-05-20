package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class StaffRepository(
    private val adminDao: AdminDao,
    private val employeeDao: EmployeeDao,
    private val attendanceDao: AttendanceDao,
    private val paymentDao: PaymentDao,
    private val qrCodeDao: QrCodeDao
) {
    // Flows exposed for UI
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val activeEmployees: Flow<List<Employee>> = employeeDao.getActiveEmployees()
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val totalPaidFlow: Flow<Double?> = paymentDao.getTotalPaidFlow()
    val activeEmployeeCountFlow: Flow<Int> = employeeDao.getActiveEmployeeCountFlow()

    fun getPresentCountTodayFlow(): Flow<Int> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return attendanceDao.getPresentCountForDateFlow(today)
    }

    fun getLateCountTodayFlow(): Flow<Int> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return attendanceDao.getLateCountForDateFlow(today)
    }

    fun getAttendanceByEmployee(matricule: String): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByEmployee(matricule)

    fun getPaymentsByEmployee(matricule: String): Flow<List<Payment>> =
        paymentDao.getPaymentsByEmployee(matricule)

    fun searchEmployees(query: String): Flow<List<Employee>> =
        employeeDao.searchEmployees(query)

    // Admin Auth
    suspend fun findAdminByUsername(username: String): Admin? =
        adminDao.getAdminByUsername(username)

    suspend fun registerAdmin(admin: Admin) =
        adminDao.insertAdmin(admin)

    suspend fun getAdminCount(): Int =
        adminDao.getAdminCount()

    // Employee operations
    suspend fun getEmployeeByMatricule(matricule: String): Employee? =
        employeeDao.getEmployeeByMatricule(matricule)

    suspend fun insertEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
        // Automatically create QR code mapping
        val qrText = "STAFFFLOW:${employee.matricule}"
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        qrCodeDao.insertQrCode(QrCode(employee.matricule, qrText, todayStr))
    }

    suspend fun updateEmployee(employee: Employee) =
        employeeDao.updateEmployee(employee)

    suspend fun deleteEmployee(employee: Employee) {
        employeeDao.deleteEmployee(employee)
        qrCodeDao.deleteQrByMatricule(employee.matricule)
    }

    // QR Operations
    suspend fun getQrByMatricule(matricule: String): QrCode? =
        qrCodeDao.getQrByMatricule(matricule)

    suspend fun getQrByText(qrText: String): QrCode? =
        qrCodeDao.getQrByText(qrText)

    // Attendance Operations
    suspend fun getAttendanceForEmployeeOnDate(matricule: String, date: String): Attendance? =
        attendanceDao.getAttendanceForEmployeeOnDate(matricule, date)

    suspend fun saveAttendance(attendance: Attendance) {
        if (attendance.id == 0) {
            attendanceDao.insertAttendance(attendance)
        } else {
            attendanceDao.updateAttendance(attendance)
        }
    }

    // Attendance Scan Pointage Logic
    suspend fun handleQrScan(qrText: String): ScanResult {
        val qrEntry = qrCodeDao.getQrByText(qrText) ?: return ScanResult.Error("Code QR non valide ou inconnu")
        val matricule = qrEntry.matricule
        
        val employee = employeeDao.getEmployeeByMatricule(matricule)
            ?: return ScanResult.Error("Employé introuvable")
        
        if (employee.status != "ACTIF") {
            return ScanResult.Error("Le compte de cet employé est inactif")
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowMs = System.currentTimeMillis()

        // Get attendance for today
        val existing = attendanceDao.getAttendanceForEmployeeOnDate(matricule, todayStr)

        return if (existing == null) {
            // First Scan: Check IN
            // Standard Check-in time limit set as 09:00 AM for ease of use
            val calendar = Calendar.getInstance().apply {
                timeInMillis = nowMs
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val limitMs = calendar.timeInMillis
            val isLate = nowMs > limitMs
            val lateMinutes = if (isLate) ((nowMs - limitMs) / 60000).toInt() else 0

            val newAttendance = Attendance(
                matricule = matricule,
                date = todayStr,
                checkInMs = nowMs,
                checkOutMs = null,
                workedHours = 0.0,
                isLate = isLate,
                lateMinutes = lateMinutes,
                overtimeHours = 0.0
            )
            attendanceDao.insertAttendance(newAttendance)
            ScanResult.CheckInSuccess(
                employee = employee,
                date = todayStr,
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nowMs)),
                isLate = isLate,
                lateMinutes = lateMinutes
            )
        } else if (existing.checkOutMs == null) {
            // Second Scan: Check OUT
            val checkInVal = existing.checkInMs ?: nowMs
            val durationMs = nowMs - checkInVal
            val hours = durationMs.toDouble() / 3600000.0
            
            // Format to 2 decimal places
            val hoursFormatted = String.format(Locale.US, "%.2f", hours).toDouble()
            val overtime = if (hoursFormatted > 8.0) hoursFormatted - 8.0 else 0.0
            val overtimeFormatted = String.format(Locale.US, "%.2f", overtime).toDouble()

            val updatedAttendance = existing.copy(
                checkOutMs = nowMs,
                workedHours = hoursFormatted,
                overtimeHours = overtimeFormatted
            )
            attendanceDao.updateAttendance(updatedAttendance)
            
            ScanResult.CheckOutSuccess(
                employee = employee,
                date = todayStr,
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nowMs)),
                workedHours = String.format(Locale.getDefault(), "%.1f h", hoursFormatted),
                overtimeHours = String.format(Locale.getDefault(), "%.1f h", overtimeFormatted)
            )
        } else {
            ScanResult.Error("Pointage déjà complété pour aujourd'hui (${employee.name})")
        }
    }

    // Payment processes
    suspend fun recordPayment(payment: Payment) =
        paymentDao.insertPayment(payment)

    suspend fun getPaymentByEmployeeAndMonth(matricule: String, month: String): Payment? {
        return paymentDao.getPaymentByEmployeeAndMonth(matricule, month)
    }
}

sealed class ScanResult {
    data class CheckInSuccess(
        val employee: Employee,
        val date: String,
        val time: String,
        val isLate: Boolean,
        val lateMinutes: Int
    ) : ScanResult()

    data class CheckOutSuccess(
        val employee: Employee,
        val date: String,
        val time: String,
        val workedHours: String,
        val overtimeHours: String
    ) : ScanResult()

    data class Error(val message: String) : ScanResult()
}
