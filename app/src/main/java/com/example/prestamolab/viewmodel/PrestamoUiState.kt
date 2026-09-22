package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val activeSolicitudes: List<SolicitudPrestamo> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val guardando: Boolean = false,
    val isAdminLoggedIn: Boolean = false
)
