package com.example.prestamolab.data.remote

import com.google.gson.annotations.SerializedName

data class EquipoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("estado") val estado: String
)
