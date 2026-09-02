package com.example.prestamolab.model

data class SolicitudPrestamo(
    val id: Int,
    val equipoId: Int,
    val solicitante: String,
    val fechaInicio: String,
    val fechaFin: String,
    val estado: String
)