package com.example.prestamolab.model

data class Equipo(
    val id: Int,
    val nombre: String,
    val categoria: CategoriaEquipo,
    val estado: EstadoEquipo
)
