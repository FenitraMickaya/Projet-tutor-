package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Admin
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins WHERE username = :username LIMIT 1")
    suspend fun getAdminByUsername(username: String): Admin?

    @Query("SELECT * FROM admins")
    fun getAllAdmins(): Flow<List<Admin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: Admin)

    @Update
    suspend fun updateAdmin(admin: Admin)

    @Delete
    suspend fun deleteAdmin(admin: Admin)

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminCount(): Int
}
