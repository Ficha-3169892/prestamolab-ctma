package com.example.prestamolab.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Aviso de devolución de un préstamo (HU-09). */
data class Recordatorio(
    /** Id local del préstamo: la notificación abre su pantalla. */
    val solicitudId: Int,
    val nombreEquipo: String,
    /** Fecha límite pactada, "yyyy-MM-dd HH:mm". */
    val horaLimite: String,
    /** Momento en que debe mostrarse (milisegundos). */
    val instante: Long
)

object PlanRecordatorios {
    /** CA-HU09-01: el recordatorio llega 30 minutos antes de la fecha de fin. */
    val ANTICIPACION_MS = TimeUnit.MINUTES.toMillis(30)

    /**
     * Recordatorios que deben estar programados: uno por cada préstamo PRESTADO del usuario cuya fecha
     * límite aún no pasa. Un préstamo devuelto o cancelado no aparece, así su recordatorio se cancela.
     */
    fun calcular(
        solicitudes: List<SolicitudPrestamo>,
        equipos: List<Equipo>,
        usuarioId: String,
        ahora: Long
    ): List<Recordatorio> = solicitudes
        .filter { it.estado == EstadoSolicitud.PRESTADO && it.usuarioId == usuarioId }
        .mapNotNull { solicitud ->
            val limite = aInstante(solicitud.fechaFin) ?: return@mapNotNull null
            if (limite <= ahora) return@mapNotNull null
            Recordatorio(
                solicitudId = solicitud.id,
                nombreEquipo = equipos.find { it.id == solicitud.equipoId }?.nombre ?: "Equipo #${solicitud.equipoId}",
                horaLimite = solicitud.fechaFin,
                instante = limite - ANTICIPACION_MS
            )
        }

    /** Si ya se pasó el momento del aviso pero no la fecha límite, se avisa de inmediato. */
    fun retraso(recordatorio: Recordatorio, ahora: Long): Long = (recordatorio.instante - ahora).coerceAtLeast(0)

    fun titulo(recordatorio: Recordatorio) = "Devuelve ${recordatorio.nombreEquipo}"

    fun texto(recordatorio: Recordatorio) =
        "Hora límite: ${recordatorio.horaLimite}. Toca para registrar la devolución."

    private fun aInstante(fecha: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { isLenient = false }.parse(fecha)?.time
    } catch (e: ParseException) {
        null
    }
}
