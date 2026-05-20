package com.example.data.dao

import androidx.room.*
import com.example.data.entity.QrCode

@Dao
interface QrCodeDao {
    @Query("SELECT * FROM qr_codes WHERE matricule = :matricule LIMIT 1")
    suspend fun getQrByMatricule(matricule: String): QrCode?

    @Query("SELECT * FROM qr_codes WHERE qrText = :qrText LIMIT 1")
    suspend fun getQrByText(qrText: String): QrCode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCode(qrCode: QrCode)

    @Query("DELETE FROM qr_codes WHERE matricule = :matricule")
    suspend fun deleteQrByMatricule(matricule: String)
}
