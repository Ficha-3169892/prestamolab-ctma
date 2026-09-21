package com.example.prestamolab.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SolicitudDto(
    val id: Int,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: String,
    val fechaSolicitud: Long,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null
)
