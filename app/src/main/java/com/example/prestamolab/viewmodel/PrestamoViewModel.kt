package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
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
    data object EquipoGestionado : PrestamoEvent()
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

    // --- Gestión de Catálogo ---

    fun crearEquipo(nombre: String, categoria: CategoriaEquipo) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val equipos = _uiState.value.equipos
            val nuevoId = if (equipos.isEmpty()) 1 else equipos.maxOf { it.id } + 1
            val equipo = Equipo(nuevoId, nombre, categoria, EstadoEquipo.DISPONIBLE)
            
            val result = repository.crearEquipo(equipo)
            if (result.isSuccess) {
                _events.emit(PrestamoEvent.EquipoGestionado)
            } else {
                _uiState.update { it.copy(mensaje = "Error al crear equipo") }
            }
            _uiState.update { it.copy(guardando = false) }
        }
    }

    fun actualizarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val result = repository.actualizarEquipo(equipo)
            if (result.isSuccess) {
                _events.emit(PrestamoEvent.EquipoGestionado)
            } else {
                _uiState.update { it.copy(mensaje = "Error al actualizar equipo") }
            }
            _uiState.update { it.copy(guardando = false) }
        }
    }

    fun eliminarEquipo(id: Int) {
        viewModelScope.launch {
            val result = repository.eliminarEquipo(id)
            if (result.isFailure) {
                _uiState.update { it.copy(mensaje = "Error al eliminar equipo") }
            }
        }
    }

    // --- Gestión de Solicitudes ---

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

    fun registrarDevolucion(
        solicitudId: Int,
        fotoUri: String?,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
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

    fun loginAdmin(correo: String, contrasena: String): Boolean {
        val esAdminValido = correo.trim().equals("admin@gmail.com", ignoreCase = true) && contrasena == "admin123"
        if (esAdminValido) {
            _uiState.update { it.copy(isAdminLoggedIn = true) }
        }
        return esAdminValido
    }

    fun logoutAdmin() {
        _uiState.update { it.copy(isAdminLoggedIn = false) }
    }

    fun borrarHistorial() {
        viewModelScope.launch {
            val resultado = repository.borrarHistorial()
            if (resultado.isSuccess) {
                _uiState.update { it.copy(mensaje = "Historial limpiado correctamente") }
            } else {
                _uiState.update { 
                    it.copy(mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo borrar el historial")
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
