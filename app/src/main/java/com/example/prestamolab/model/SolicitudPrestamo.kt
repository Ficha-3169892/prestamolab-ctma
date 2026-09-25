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
    val motivoRechazo: String? = null,
    /** CA-HU13-03: dónde se solicitó; null si el estudiante no ha concedido la ubicación. */
    val latitud: Double? = null,
    val longitud: Double? = null,
    val precisionMetros: Float? = null
)

/** CA-HU06-05: un préstamo con su equipo y sus evidencias, obtenidos en una sola consulta. */
data class PrestamoDetalle(
    val solicitud: SolicitudPrestamo,
    val equipo: Equipo?,
    val evidencias: List<Evidencia>
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
