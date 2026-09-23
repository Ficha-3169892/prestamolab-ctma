package com.example.prestamolab.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolicitudDto(
    val id: Int = 0,
    @SerialName("equipo_id") val equipoId: Int,
    @SerialName("ambiente_destino") val ambienteDestino: String,
    val proposito: String,
    @SerialName("duracion_horas") val duracionHoras: Int,
    val estado: String,
    @SerialName("fecha_solicitud") val fechaSolicitud: Long,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @SerialName("foto_uri") val fotoUri: String? = null
)
