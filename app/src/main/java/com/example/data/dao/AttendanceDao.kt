package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE matricule = :matricule ORDER BY date DESC")
    fun getAttendanceByEmployee(matricule: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE matricule = :matricule AND date = :date LIMIT 1")
    suspend fun getAttendanceForEmployeeOnDate(matricule: String, date: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("SELECT COUNT(DISTINCT matricule) FROM attendance WHERE date = :date AND checkInMs IS NOT NULL")
    fun getPresentCountForDateFlow(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND isLate = 1")
    fun getLateCountForDateFlow(date: String): Flow<Int>
}
