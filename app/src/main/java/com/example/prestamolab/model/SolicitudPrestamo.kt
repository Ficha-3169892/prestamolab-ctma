package com.example.prestamolab.model

enum class EstadoSolicitud { SOLICITADA, CANCELADA }

data class SolicitudPrestamo(
    val id: Int,
    val equipoId: Int,
    val solicitante: String,
    val ambiente: String,
    val proposito: String,
    val duracionHoras: Int,
    val fechaInicio: String,
    val fechaFin: String,
    val estado: EstadoSolicitud
)

/** Datos que captura el formulario; el repositorio asigna id, fechas y estado. */
data class NuevaSolicitud(
    val equipoId: Int,
    val solicitante: String,
    val ambiente: String,
    val proposito: String,
    val duracionHoras: Int
)
