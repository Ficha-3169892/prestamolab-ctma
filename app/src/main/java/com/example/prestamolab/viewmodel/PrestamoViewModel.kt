package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.repository.InMemoryPrestamoRepository
import com.example.prestamolab.repository.PrestamoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrestamoViewModel(
    private val repository: PrestamoRepository = InMemoryPrestamoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PrestamoUiState()
    )

    val uiState: StateFlow<PrestamoUiState> =
        _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        _uiState.value = _uiState.value.copy(
            equipos = repository.obtenerEquipos(),
            solicitudes = repository.obtenerSolicitudes()
        )
    }

    fun crearSolicitud(
        equipoId: Int,
        ambienteDestino: String,
        proposito: String,
        duracionHoras: Int
    ): Boolean {

        if (ambienteDestino.isBlank()) {
            mostrarMensaje(
                "Debes ingresar el ambiente de destino"
            )
            return false
        }

        if (proposito.length !in 10..180) {
            mostrarMensaje(
                "El propósito debe tener entre 10 y 180 caracteres"
            )
            return false
        }

        if (duracionHoras !in 1..8) {
            mostrarMensaje(
                "La duración debe estar entre 1 y 8 horas"
            )
            return false
        }

        val nuevaSolicitud = SolicitudPrestamo(
            id = generarIdSolicitud(),
            equipoId = equipoId,
            ambienteDestino = ambienteDestino,
            proposito = proposito,
            duracionHoras = duracionHoras,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(
            nuevaSolicitud
        )

        return if (resultado.isSuccess) {

            cargarDatos()

            mostrarMensaje(
                "Solicitud creada correctamente"
            )

            true

        } else {

            mostrarMensaje(
                resultado.exceptionOrNull()?.message
                    ?: "No se pudo crear la solicitud"
            )

            false
        }
    }

    fun cancelarSolicitud(id: Int) {

        val resultado = repository.cancelarSolicitud(id)

        if (resultado.isSuccess) {

            cargarDatos()

            mostrarMensaje(
                "Solicitud cancelada correctamente"
            )

        } else {

            mostrarMensaje(
                resultado.exceptionOrNull()?.message
                    ?: "No se pudo cancelar la solicitud"
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(
            mensaje = null
        )
    }

    private fun mostrarMensaje(mensaje: String) {
        _uiState.value = _uiState.value.copy(
            mensaje = mensaje
        )
    }

    private fun generarIdSolicitud(): Int {
        return (
                repository
                    .obtenerSolicitudes()
                    .maxOfOrNull { it.id }
                    ?: 0
                ) + 1
    }
}