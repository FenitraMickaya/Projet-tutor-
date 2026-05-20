package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val matricule: String, // EMP-0001
    val name: String,
    val email: String,
    val photoUri: String? = null,
    val poste: String,
    val departement: String,
    val hourlyRate: Double,
    val baseSalary: Double, // Base monthly salary if applicable
    val hireDate: String, // YYYY-MM-DD
    val status: String, // "ACTIF" or "ARCHIVE"
    val paymentMode: String, // "Mobile Money", "Espèce", "Virement Bancaire"
    val balance: Double = 500000.0
)
