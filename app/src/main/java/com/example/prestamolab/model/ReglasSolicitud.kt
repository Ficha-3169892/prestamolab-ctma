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

    // TDD, fase roja: la especificación existe en ReglasSolicitudTest; la regla aún no valida nada
    fun validar(ambiente: String, proposito: String, duracionHoras: String): ErroresSolicitud = ErroresSolicitud()
}
