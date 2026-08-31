package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val equipoSeleccionado: Equipo? = null,
    val solicitudSeleccionada: SolicitudPrestamo? = null,
    val mensajeError: String? = null,
    val guardando: Boolean = false
)
