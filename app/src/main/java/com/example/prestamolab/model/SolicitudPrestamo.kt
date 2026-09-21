package com.example.prestamolab.model

data class SolicitudPrestamo(
    val id: Int,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: EstadoSolicitud,
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
    val estadoSincronizacion: String = "LOCAL"
)
