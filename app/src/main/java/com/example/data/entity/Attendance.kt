package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matricule: String,
    val date: String, // YYYY-MM-DD
    val checkInMs: Long?,
    val checkOutMs: Long?,
    val workedHours: Double = 0.0,
    val isLate: Boolean = false,
    val lateMinutes: Int = 0,
    val overtimeHours: Double = 0.0
)
