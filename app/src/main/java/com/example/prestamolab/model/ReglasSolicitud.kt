package com.example.prestamolab.model

/** Errores por campo del formulario de solicitud; null significa que el campo es válido. */
data class ErroresSolicitud(
    val ambiente: String? = null,
    val proposito: String? = null,
    val duracion: String? = null
) {
    val hayErrores: Boolean get() = listOfNotNull(ambiente, proposito, duracion).isNotEmpty()
}

/** Reglas del formulario de solicitud (HU-03, CA-HU03-02 y CA-HU03-03). */
object ReglasSolicitud {
    const val PROPOSITO_MINIMO = 10
    const val PROPOSITO_MAXIMO = 180
    const val DURACION_MINIMA = 1
    const val DURACION_MAXIMA = 8

    fun validar(ambiente: String, proposito: String, duracionHoras: String): ErroresSolicitud {
        val largoProposito = proposito.trim().length
        val duracion = duracionHoras.trim().toIntOrNull()
        return ErroresSolicitud(
            ambiente = if (ambiente.isBlank()) "El ambiente o destino es obligatorio." else null,
            proposito = when {
                largoProposito < PROPOSITO_MINIMO -> "Propósito debe tener mínimo $PROPOSITO_MINIMO caracteres"
                largoProposito > PROPOSITO_MAXIMO -> "Máximo $PROPOSITO_MAXIMO caracteres"
                else -> null
            },
            duracion = if (duracion == null || duracion !in DURACION_MINIMA..DURACION_MAXIMA) {
                "Duración entre $DURACION_MINIMA y $DURACION_MAXIMA horas"
            } else null
        )
    }
}
