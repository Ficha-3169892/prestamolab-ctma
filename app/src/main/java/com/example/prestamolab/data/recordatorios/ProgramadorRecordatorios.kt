package com.example.prestamolab.data.recordatorios

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.example.prestamolab.model.PlanRecordatorios
import com.example.prestamolab.model.Recordatorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

interface ProgramadorRecordatorios {
    /** Deja programados exactamente [recordatorios]: agrega, reprograma si cambió la hora y cancela el resto. */
    suspend fun sincronizar(recordatorios: List<Recordatorio>)

    /** Al cerrar sesión no deben quedar avisos del usuario anterior. */
    suspend fun cancelarTodos()
}

class WorkManagerRecordatorios(
    private val contexto: Context,
    private val reloj: () -> Long = System::currentTimeMillis
) : ProgramadorRecordatorios {

    private val workManager by lazy { WorkManager.getInstance(contexto) }

    override suspend fun sincronizar(recordatorios: List<Recordatorio>) {
        // Los ya mostrados (SUCCEEDED) cuentan: así un aviso no se repite con cada cambio de datos
        val existentes = withContext(Dispatchers.IO) { workManager.getWorkInfosByTag(ETIQUETA).get() }
            .filter { it.state != WorkInfo.State.CANCELLED && it.state != WorkInfo.State.FAILED }
        val deseados = recordatorios.map(::nombreTrabajo).toSet()

        // CA-HU09-03: el préstamo devuelto ya no está en la lista y su aviso pendiente se cancela
        existentes.filter { it.state == WorkInfo.State.ENQUEUED }
            .flatMap { it.tags }.filter { it.startsWith(PREFIJO_NOMBRE) && it !in deseados }
            .forEach { workManager.cancelUniqueWork(it) }

        for (recordatorio in recordatorios) {
            // Mismo préstamo y misma hora: ya está programado (o ya se mostró) y no se toca
            val etiquetaHora = etiquetaInstante(recordatorio)
            if (existentes.any { etiquetaHora in it.tags && nombreTrabajo(recordatorio) in it.tags }) continue
            workManager.enqueueUniqueWork(nombreTrabajo(recordatorio), ExistingWorkPolicy.REPLACE, solicitud(recordatorio))
        }
    }

    override suspend fun cancelarTodos() {
        workManager.cancelAllWorkByTag(ETIQUETA).await()
    }

    private fun solicitud(recordatorio: Recordatorio) = OneTimeWorkRequestBuilder<RecordatorioWorker>()
        .setInitialDelay(PlanRecordatorios.retraso(recordatorio, reloj()), TimeUnit.MILLISECONDS)
        .setInputData(
            workDataOf(
                RecordatorioWorker.SOLICITUD_ID to recordatorio.solicitudId,
                RecordatorioWorker.EQUIPO to recordatorio.nombreEquipo,
                RecordatorioWorker.HORA_LIMITE to recordatorio.horaLimite,
                RecordatorioWorker.INSTANTE to recordatorio.instante
            )
        )
        .addTag(ETIQUETA)
        .addTag(nombreTrabajo(recordatorio))
        .addTag(etiquetaInstante(recordatorio))
        .build()

    companion object {
        const val ETIQUETA = "recordatorio-devolucion"
        private const val PREFIJO_NOMBRE = "recordatorio-prestamo-"

        /** Nombre único del trabajo: un solo aviso por préstamo. */
        fun nombreTrabajo(recordatorio: Recordatorio) = nombreTrabajo(recordatorio.solicitudId)
        fun nombreTrabajo(solicitudId: Int) = "$PREFIJO_NOMBRE$solicitudId"

        private fun etiquetaInstante(recordatorio: Recordatorio) = "recordatorio-instante-${recordatorio.instante}"
    }
}
