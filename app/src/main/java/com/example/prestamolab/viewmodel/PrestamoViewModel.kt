package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.prestamolab.model.Equipo
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
            solicitudes = repository.obtenerSolicitudes(),
            mensaje = null
        )
    }

    fun obtenerEquipo(id: Int): Equipo? {
        return repository.obtenerEquipo(id)
    }

    fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return repository.obtenerSolicitud(id)
    }

    fun crearSolicitud(
        equipoId: Int,
        ambienteDestino: String,
        proposito: String,
        duracionHoras: Int
    ): Boolean {

        if (_uiState.value.guardando) {
            return false
        }

        _uiState.value = _uiState.value.copy(
            guardando = true,
            mensaje = null
        )

        val solicitud = SolicitudPrestamo(
            id = generarIdSolicitud(),
            equipoId = equipoId,
            ambienteDestino = ambienteDestino.trim(),
            proposito = proposito.trim(),
            duracionHoras = duracionHoras,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(solicitud)

        if (resultado.isSuccess) {

            _uiState.value = _uiState.value.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes(),
                mensaje = "Solicitud creada correctamente",
                guardando = false
            )

            return true

        } else {

            _uiState.value = _uiState.value.copy(
                mensaje = resultado.exceptionOrNull()?.message
                    ?: "No se pudo crear la solicitud",
                guardando = false
            )

            return false
        }
    }

    fun cancelarSolicitud(id: Int) {

        val resultado = repository.cancelarSolicitud(id)

        if (resultado.isSuccess) {
            _uiState.value = _uiState.value.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes(),
                mensaje = "Solicitud cancelada correctamente"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                mensaje = resultado.exceptionOrNull()?.message
                    ?: "No se pudo cancelar la solicitud"
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(
            mensaje = null
        )
    }

    private fun generarIdSolicitud(): Int {
        val solicitudes = repository.obtenerSolicitudes()

        return if (solicitudes.isEmpty()) {
            1
        } else {
            solicitudes.maxOf { it.id } + 1
        }
    }
}
