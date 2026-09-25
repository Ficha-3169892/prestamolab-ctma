package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

sealed interface UiStatus {
    object Loading : UiStatus
    object Content : UiStatus
    object Empty : UiStatus
    data class Error(val message: String) : UiStatus
}

data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val mensaje: String? = null,
    val uiStatus: UiStatus = UiStatus.Content,
    val operationStatus: String? = null
)
