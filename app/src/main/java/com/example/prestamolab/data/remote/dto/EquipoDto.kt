package com.example.prestamolab.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EquipoDto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val estado: String
)
