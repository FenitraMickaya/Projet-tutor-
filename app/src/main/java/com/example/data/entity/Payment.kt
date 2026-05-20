package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matricule: String,
    val month: String, // YYYY-MM
    val paymentDateMs: Long,
    val amountPaid: Double,
    val normalHours: Double,
    val overtimeHours: Double,
    val bonus: Double,
    val deductions: Double,
    val status: String // "PAID", "PARTIAL"
)
