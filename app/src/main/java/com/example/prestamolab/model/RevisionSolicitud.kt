package com.example.prestamolab.model

/** Estado en que quedan la solicitud y su equipo tras la revisión del instructor. */
data class ResultadoRevision(
    val estadoSolicitud: EstadoSolicitud,
    val estadoEquipo: EstadoEquipo,
    val motivoRechazo: String? = null
)

/**
 * Transiciones que aplica el instructor a una solicitud (HU-14). Solo se revisa una solicitud
 * SOLICITADA: una CANCELADA, RECHAZADA o ya entregada se rechaza sin cambios (CA-HU14-04).
 */
object RevisionSolicitud {
    const val MOTIVO_MAXIMO = 180

    /** CA-HU14-02: SOLICITADA → PRESTADO; el equipo queda PRESTADO. */
    fun aprobar(actual: EstadoSolicitud): Result<ResultadoRevision> =
        validarPendiente(actual, "aprobar") ?: Result.success(
            ResultadoRevision(EstadoSolicitud.PRESTADO, EstadoEquipo.PRESTADO)
        )

    /** CA-HU14-03: SOLICITADA → RECHAZADA con motivo; el equipo vuelve a DISPONIBLE. */
    fun rechazar(actual: EstadoSolicitud, motivo: String): Result<ResultadoRevision> {
        validarPendiente(actual, "rechazar")?.let { return it }
        val limpio = motivo.trim()
        if (limpio.isEmpty()) return Result.failure(IllegalArgumentException("Indica el motivo del rechazo"))
        if (limpio.length > MOTIVO_MAXIMO) {
            return Result.failure(IllegalArgumentException("El motivo admite máximo $MOTIVO_MAXIMO caracteres"))
        }
        return Result.success(ResultadoRevision(EstadoSolicitud.RECHAZADA, EstadoEquipo.DISPONIBLE, limpio))
    }

    private fun validarPendiente(actual: EstadoSolicitud, accion: String): Result<ResultadoRevision>? =
        if (actual == EstadoSolicitud.SOLICITADA) null
        else Result.failure(IllegalStateException("No se puede $accion una solicitud en estado $actual"))
}
