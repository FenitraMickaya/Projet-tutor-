package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY matricule DESC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE status = 'ACTIF' ORDER BY name ASC")
    fun getActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE matricule = :matricule LIMIT 1")
    suspend fun getEmployeeByMatricule(matricule: String): Employee?

    @Query("SELECT * FROM employees WHERE name LIKE '%' || :query || '%' OR matricule LIKE '%' || :query || '%'")
    fun searchEmployees(query: String): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("SELECT COUNT(*) FROM employees WHERE status = 'ACTIF'")
    fun getActiveEmployeeCountFlow(): Flow<Int>
}
