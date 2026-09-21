package com.example.prestamolab

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.prestamolab.data.local.database.PrestamoDatabase
import com.example.prestamolab.data.worker.SyncWorker
import com.example.prestamolab.data.remote.api.PrestamoApiService
import com.example.prestamolab.repository.PrestamoRepository
import com.example.prestamolab.repository.RoomPrestamoRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PrestamoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupSyncWorker()
    }

    private fun setupSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    val database by lazy { PrestamoDatabase.getDatabase(this) }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val apiService: PrestamoApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.prestamolab.example.com/") // URL ficticia para la guía
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PrestamoApiService::class.java)
    }

    val repository: PrestamoRepository by lazy { 
        com.example.prestamolab.repository.OfflineFirstPrestamoRepository(
            database.equipoDao(), 
            database.solicitudDao(),
            apiService
        ) 
    }
}
