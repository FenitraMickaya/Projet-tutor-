package com.example.data.network

import com.example.data.entity.Employee
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface StaffSyncApi {
    @POST("employees/sync")
    suspend fun syncEmployees(
        @Header("Authorization") token: String,
        @Body employees: List<Employee>
    ): Response<Unit>

    @POST("attendance/sync")
    suspend fun syncAttendance(
        @Header("Authorization") token: String,
        @Body attendance: List<Attendance>
    ): Response<Unit>

    @POST("payments/sync")
    suspend fun syncPayments(
        @Header("Authorization") token: String,
        @Body payments: List<Payment>
    ): Response<Unit>
}

object SyncService {
    fun createApi(baseUrl: String): StaffSyncApi {
        // Ensure valid trailing slash for Retrofit baseUrl
        val formattedUrl = when {
            baseUrl.isBlank() -> "https://api.staffflow.io/"
            baseUrl.endsWith("/") -> baseUrl
            else -> "$baseUrl/"
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(StaffSyncApi::class.java)
    }
}
