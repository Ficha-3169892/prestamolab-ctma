package com.example.prestamolab.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prestamolab.PrestamoLabApp

class SincronizacionWorker(contexto: Context, parametros: WorkerParameters) :
    CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PrestamoLabApp).container
        return when (container.coordinadorSincronizacion.ejecutar()) {
            AccionTrasSincronizar.EXITO -> Result.success()
            AccionTrasSincronizar.REINTENTAR -> Result.retry()
            AccionTrasSincronizar.FALLO -> Result.failure()
        }
    }
}
