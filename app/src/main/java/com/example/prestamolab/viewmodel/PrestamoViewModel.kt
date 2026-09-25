package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.model.ambienteValido
import com.example.prestamolab.model.duracionValida
import com.example.prestamolab.model.propositoValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrestamoViewModel(private val repository: PrestamoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow < PrestamoUiState > = _uiState.asStateFlow()

    init {
        // Nos suscribimos de forma reactiva al catálogo
        viewModelScope.launch {
            repository.obtenerEquipos().collect { listaEquipos ->
                _uiState.update { it.copy(equipos = listaEquipos) }
            }
        }

        // Nos suscribimos de forma reactiva a las solicitudes
        viewModelScope.launch {
            repository.obtenerSolicitudes().collect { listaSolicitudes ->
                _uiState.update { it.copy(solicitudes = listaSolicitudes) }
            }
        }
    }

    fun seleccionarEquipo(equipoId: Int) {
        viewModelScope.launch {
            repository.obtenerEquipo(equipoId).collect { equipo ->
                _uiState.update { it.copy(equipoSeleccionado = equipo) }
            }
        }
    }

    fun seleccionarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            repository.obtenerSolicitud(solicitudId).collect { solicitud ->
                _uiState.update { it.copy(solicitudSeleccionada = solicitud) }
            }
        }
    }

    fun guardarSolicitud(equipoId: Int, ambiente: String, proposito: String, duracion: Int) {
        if (_uiState.value.guardando) return

        val errores = mutableListOf < String > ()
        if (!ambienteValido(ambiente)) errores.add("El ambiente o destino es obligatorio.")
        if (!propositoValido(proposito)) errores.add("El propósito debe tener entre 10 y 180 caracteres.")
        if (!duracionValida(duracion)) errores.add("La duración debe estar entre 1 y 8 horas.")

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(mensajeError = errores.joinToString("\n")) }
            return
        }

        _uiState.update { it.copy(guardando = true, mensajeError = null) }

        val nuevaSolicitud = SolicitudPrestamo(
            id = 0, equipoId = equipoId, ambienteDestino = ambiente,
            proposito = proposito, duracionHoras = duracion, estado = EstadoSolicitud.SOLICITADA
        )

        // Las operaciones de escritura ahora son asíncronas
        viewModelScope.launch {
            val resultado = repository.crearSolicitud(nuevaSolicitud)
            resultado.onSuccess {
                // No necesitamos recargar datos, el Flow lo hace solo
                _uiState.update { it.copy(guardando = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(guardando = false, mensajeError = err.message) }
            }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            val resultado = repository.cancelarSolicitud(solicitudId)
            resultado.onFailure { err ->
                _uiState.update { it.copy(mensajeError = err.message) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}
