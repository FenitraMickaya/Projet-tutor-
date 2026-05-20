package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entity.*

@Database(
    entities = [
        Admin::class,
        Employee::class,
        Attendance::class,
        Payment::class,
        QrCode::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adminDao(): AdminDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun qrCodeDao(): QrCodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "staffflow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
