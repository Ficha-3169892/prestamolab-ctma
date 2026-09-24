package com.example.prestamolab.model

/** Mismos valores que el CHECK de loans.status en Supabase; RECHAZADA la asigna el instructor (HU-14). */
enum class EstadoSolicitud { SOLICITADA, PRESTADO, DEVUELTO, RECHAZADA, CANCELADA }

data class SolicitudPrestamo(
    val id: Int,
    val equipoId: Int,
    /** users.id del solicitante; null en registros anteriores a la sincronización. */
    val usuarioId: String?,
    val solicitante: String,
    val ambiente: String,
    val proposito: String,
    val duracionHoras: Int,
    val fechaInicio: String,
    val fechaFin: String,
    val estado: EstadoSolicitud,
    /** Lo registra el instructor al rechazar (CA-HU14-03). */
    val motivoRechazo: String? = null
)

/** Datos que captura el formulario; el repositorio asigna id, fechas y estado. */
data class NuevaSolicitud(
    val equipoId: Int,
    val usuarioId: String,
    val solicitante: String,
    val ambiente: String,
    val proposito: String,
    val duracionHoras: Int
)
