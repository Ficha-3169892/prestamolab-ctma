package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
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

class PrestamoViewModel(private val repository: PrestamoRepository) : ViewModel() {

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

    fun seleccionarEquipo(equipoId: Int) {
        val equipo = repository.obtenerEquipo(equipoId)
        _uiState.update { it.copy(equipoSeleccionado = equipo) }
    }

    fun seleccionarSolicitud(solicitudId: Int) {
        val solicitud = repository.obtenerSolicitud(solicitudId)
        _uiState.update { it.copy(solicitudSeleccionada = solicitud) }
    }

    fun guardarSolicitud(
        equipoId: Int,
        ambiente: String,
        proposito: String,
        duracion: Int
    ) {
        if (_uiState.value.guardando) return

        if (!ambienteValido(ambiente) || !propositoValido(proposito) || !duracionValida(duracion)) {
            _uiState.update { it.copy(mensajeError = "Datos de formulario no válidos") }
            return
        }

        _uiState.update { it.copy(guardando = true, mensajeError = null) }

        val nuevaSolicitud = SolicitudPrestamo(
            id = 0,
            equipoId = equipoId,
            ambienteDestino = ambiente,
            proposito = proposito,
            duracionHoras = duracion,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(nuevaSolicitud)

        resultado.onSuccess {
            cargarDatos()
            _uiState.update { it.copy(guardando = false) }
        }.onFailure { err ->
            _uiState.update { it.copy(guardando = false, mensajeError = err.message) }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        val resultado = repository.cancelarSolicitud(solicitudId)
        resultado.onSuccess {
            cargarDatos()
            seleccionarSolicitud(solicitudId)
        }.onFailure { err ->
            _uiState.update { it.copy(mensajeError = err.message) }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}
