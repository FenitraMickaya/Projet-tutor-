package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDateMs DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE matricule = :matricule ORDER BY month DESC")
    fun getPaymentsByEmployee(matricule: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE matricule = :matricule AND month = :month LIMIT 1")
    suspend fun getPaymentByEmployeeAndMonth(matricule: String, month: String): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT SUM(amountPaid) FROM payments")
    fun getTotalPaidFlow(): Flow<Double?>
}
