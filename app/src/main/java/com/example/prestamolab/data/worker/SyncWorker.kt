package com.example.prestamolab.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prestamolab.PrestamoApplication
import com.example.prestamolab.data.mapper.toDto
import com.example.prestamolab.data.mapper.toDomain
import kotlinx.coroutines.flow.first

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as PrestamoApplication
        val database = application.database
        val apiService = application.apiService
        val solicitudDao = database.solicitudDao()

        return try {
            val localSolicitudes = solicitudDao.getAllSolicitudes().first()
                .filter { it.estadoSincronizacion == "LOCAL" }

            for (entity in localSolicitudes) {
                try {
                    // Mapear y enviar
                    val domain = entity.toDomain()
                    apiService.createSolicitud(domain.toDto())
                    
                    // Actualizar estado local
                    solicitudDao.updateSolicitud(entity.copy(estadoSincronizacion = "SINCRONIZADA"))
                } catch (e: Exception) {
                    // Reintentar en la próxima ejecución si es un error temporal
                    return Result.retry()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
