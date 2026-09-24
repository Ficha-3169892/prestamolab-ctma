package com.example.prestamolab.model

enum class CondicionEquipo(val etiqueta: String) {
    BUENO("Bueno"),
    CON_NOVEDAD("Con novedad"),
    DANADO("Dañado")
}

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    /** Radio de precisión en metros; mayor cuando el usuario solo concede ubicación aproximada. */
    val precisionMetros: Float?
)

data class Devolucion(
    val id: Int,
    val solicitudId: Int,
    val condicion: CondicionEquipo,
    val observacion: String,
    val fechaDevolucion: String,
    // Null cuando no se concedió el permiso o no se pudo obtener la ubicación
    val latitud: Double?,
    val longitud: Double?
)

data class NuevaDevolucion(
    val solicitudId: Int,
    val condicion: CondicionEquipo,
    val observacion: String,
    val ubicacion: Ubicacion?
)
