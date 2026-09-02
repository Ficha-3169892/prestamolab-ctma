package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.util.ambienteValido
import com.example.prestamolab.util.duracionValida
import com.example.prestamolab.util.propositoValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PrestamoViewModel(
    private val repository: PrestamoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        _uiState.update {
            it.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes()
            )
        }
    }

    fun registrarSolicitud(
        equipoId: Int,
        ambiente: String,
        proposito: String,
        duracionHoras: Int,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.guardando) return

        if (!ambienteValido(ambiente)) {
            _uiState.update { it.copy(mensaje = "El ambiente o destino es obligatorio.") }
            return
        }
        if (!propositoValido(proposito)) {
            _uiState.update { it.copy(mensaje = "El propósito debe tener entre 10 y 180 caracteres.") }
            return
        }
        if (!duracionValida(duracionHoras)) {
            _uiState.update { it.copy(mensaje = "La duración debe estar entre 1 y 8 horas.") }
            return
        }

        _uiState.update { it.copy(guardando = true) }

        val nuevaSolicitud = SolicitudPrestamo(
            id = (repository.obtenerSolicitudes().maxOfOrNull { it.id } ?: 0) + 1,
            equipoId = equipoId,
            ambienteDestino = ambiente.trim(),
            proposito = proposito.trim(),
            duracionHoras = duracionHoras,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(nuevaSolicitud)

        resultado.onSuccess {
            cargarDatos()
            _uiState.update { it.copy(guardando = false, mensaje = "Solicitud registrada con éxito.") }
            onSuccess()
        }.onFailure { error ->
            _uiState.update { it.copy(guardando = false, mensaje = error.message ?: "Error al registrar la solicitud.") }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        val resultado = repository.cancelarSolicitud(solicitudId)
        resultado.onSuccess {
            cargarDatos()
            _uiState.update { it.copy(mensaje = "Solicitud cancelada con éxito.") }
        }.onFailure { error ->
            _uiState.update { it.copy(mensaje = error.message ?: "Error al cancelar la solicitud.") }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}