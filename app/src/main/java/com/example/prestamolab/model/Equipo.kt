package com.example.prestamolab.model

enum class EstadoEquipo { DISPONIBLE, RESERVADO, PRESTADO }

data class Equipo(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val estado: EstadoEquipo
)
