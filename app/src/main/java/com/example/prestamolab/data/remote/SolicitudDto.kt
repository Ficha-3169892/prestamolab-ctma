package com.example.prestamolab.data.remote

import com.google.gson.annotations.SerializedName

data class SolicitudDto(
    @SerializedName("id") val id: Int,
    @SerializedName("equipoId") val equipoId: Int,
    @SerializedName("ambienteDestino") val ambienteDestino: String,
    @SerializedName("proposito") val proposito: String,
    @SerializedName("duracionHoras") val duracionHoras: Int,
    @SerializedName("estado") val estado: String
)
