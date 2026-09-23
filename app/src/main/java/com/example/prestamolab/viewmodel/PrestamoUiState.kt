package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

sealed interface ListadoUiState {
    object Cargando : ListadoUiState
    object Vacio : ListadoUiState
    data class Contenido(val lista: List<SolicitudPrestamo>) : ListadoUiState
    data class Error(val mensaje: String) : ListadoUiState
}

sealed interface OperacionUiState {
    object Idle : OperacionUiState
    object EnCurso : OperacionUiState
    data class Exitosa(val mensaje: String) : OperacionUiState
    data class Fallida(val mensaje: String) : OperacionUiState
}

data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val mensaje: String? = null,
    val guardando: Boolean = false,
    val busquedaQuery: String = "",
    val listadoState: ListadoUiState = ListadoUiState.Cargando,
    val operacionState: OperacionUiState = OperacionUiState.Idle
)
