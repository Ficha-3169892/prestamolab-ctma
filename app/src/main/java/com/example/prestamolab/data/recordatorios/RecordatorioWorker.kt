package com.example.prestamolab.data.recordatorios

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.Recordatorio
import kotlinx.coroutines.flow.first

/** Muestra el aviso de devolución (CA-HU09-02). Nunca falla: un aviso perdido no debe reintentarse. */
class RecordatorioWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PrestamoLabApp).container
        val solicitudId = inputData.getInt(SOLICITUD_ID, -1)

        // Si el préstamo se devolvió justo antes de avisar, ya no hay nada que recordar
        val sigueEntregado = container.prestamoRepository.solicitudes.first()
            .any { it.id == solicitudId && it.estado == EstadoSolicitud.PRESTADO }
        // CA-HU09-04: sin permiso de notificaciones el aviso se omite sin error
        if (!sigueEntregado || !container.notificador.puedeNotificar()) return Result.success()

        container.notificador.mostrar(
            Recordatorio(
                solicitudId = solicitudId,
                nombreEquipo = inputData.getString(EQUIPO).orEmpty(),
                horaLimite = inputData.getString(HORA_LIMITE).orEmpty(),
                instante = inputData.getLong(INSTANTE, 0)
            )
        )
        return Result.success()
    }

    companion object {
        const val SOLICITUD_ID = "solicitudId"
        const val EQUIPO = "equipo"
        const val HORA_LIMITE = "horaLimite"
        const val INSTANTE = "instante"
    }
}
