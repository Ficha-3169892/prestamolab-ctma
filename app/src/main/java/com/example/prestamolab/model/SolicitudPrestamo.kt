package com.example.prestamolab.model

data class SolicitudPrestamo(
    val id: Int,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: EstadoSolicitud
)

fun ambienteValido(texto: String): Boolean = texto.trim().isNotBlank()

fun propositoValido(texto: String): Boolean = texto.trim().length in 10..180

fun duracionValida(horas: Int): Boolean = horas in 1..8
