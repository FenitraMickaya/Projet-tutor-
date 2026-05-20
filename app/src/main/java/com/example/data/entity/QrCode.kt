package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_codes")
data class QrCode(
    @PrimaryKey val matricule: String,
    val qrText: String, // e.g. "STAFFFLOW:EMP-0001"
    val createdDate: String
)
