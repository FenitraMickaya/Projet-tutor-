package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class Admin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val role: String // "SUPER_ADMIN", "ADMIN", "MANAGER"
)
