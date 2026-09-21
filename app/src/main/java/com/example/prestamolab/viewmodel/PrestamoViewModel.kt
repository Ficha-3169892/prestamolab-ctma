package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.repository.InMemoryPrestamoRepository
import com.example.prestamolab.repository.PrestamoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PrestamoEvent {
    data object SolicitudCreada : PrestamoEvent()
    data object DevolucionExitosa : PrestamoEvent()
}

class PrestamoViewModel(
    private val repository: PrestamoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PrestamoEvent>()
    val events: SharedFlow<PrestamoEvent> = _events.asSharedFlow()

    init {
        observarDatos()
    }

    private fun observarDatos() {
        _uiState.update { it.copy(cargando = true) }

        combine(
            repository.obtenerEquipos(),
            repository.obtenerSolicitudes(),
            repository.obtenerActiveSolicitudes()
        ) { equipos, solicitudes, activeSolicitudes ->
            _uiState.update { 
                it.copy(
                    equipos = equipos,
                    solicitudes = solicitudes,
                    activeSolicitudes = activeSolicitudes,
                    cargando = false
                )
            }
        }.catch { e ->
            _uiState.update { it.copy(mensaje = e.message, cargando = false) }
        }.launchIn(viewModelScope)
    }

    suspend fun obtenerEquipo(id: Int): Equipo? {
        return repository.obtenerEquipo(id)
    }

    suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return repository.obtenerSolicitud(id)
    }

    fun crearSolicitud(
        equipoId: Int,
        ambienteDestino: String,
        proposito: String,
        duracionHoras: Int
    ) {
        if (_uiState.value.guardando) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }

            val solicitudesActuales = repository.obtenerSolicitudes().first()
            val nuevoId = if (solicitudesActuales.isEmpty()) 1 else solicitudesActuales.maxOf { it.id } + 1

            val solicitud = SolicitudPrestamo(
                id = nuevoId,
                equipoId = equipoId,
                ambienteDestino = ambienteDestino.trim(),
                proposito = proposito.trim(),
                duracionHoras = duracionHoras,
                estado = EstadoSolicitud.SOLICITADA
            )

            val resultado = repository.crearSolicitud(solicitud)

            if (resultado.isSuccess) {
                _uiState.update { it.copy(mensaje = "Solicitud creada correctamente", guardando = false) }
                _events.emit(PrestamoEvent.SolicitudCreada)
            } else {
                _uiState.update { 
                    it.copy(
                        mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo crear la solicitud",
                        guardando = false
                    )
                }
            }
        }
    }

    fun cancelarSolicitud(id: Int) {
        viewModelScope.launch {
            val resultado = repository.cancelarSolicitud(id)
            if (resultado.isSuccess) {
                _uiState.update { it.copy(mensaje = "Solicitud cancelada correctamente") }
            } else {
                _uiState.update { 
                    it.copy(mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo cancelar la solicitud")
                }
            }
        }
    }

    fun registrarDevolucion(solicitudId: Int, fotoUri: String?, latitud: Double?, longitud: Double?) {
        viewModelScope.launch {
            val resultado = repository.registrarDevolucion(solicitudId, fotoUri, latitud, longitud)
            if (resultado.isSuccess) {
                _uiState.update { it.copy(mensaje = "Devolución registrada correctamente") }
                _events.emit(PrestamoEvent.DevolucionExitosa)
            } else {
                _uiState.update { 
                    it.copy(mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo registrar la devolución")
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
