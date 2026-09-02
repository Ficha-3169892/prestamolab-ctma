package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val mensaje: String? = null,
    val guardando: Boolean = false
)