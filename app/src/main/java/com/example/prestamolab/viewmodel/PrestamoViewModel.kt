package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
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

    fun sincronizarEquipos() {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }
            try {
                repository.sincronizarEquipos()
                if (repository is RoomPrestamoRepository) {
                    repository.sincronizarSolicitudes()
                }
                _uiState.update { it.copy(guardando = false, mensajeExito = "Sincronización completada con éxito.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensajeError = "Error de sincronización: ${e.localizedMessage}") }
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

    fun guardarSolicitud(
        equipoId: Int,
        ambiente: String,
        proposito: String,
        duracion: Int,
        onSuccess: () -> Unit
    ) {
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

        viewModelScope.launch {
            val resultado = repository.crearSolicitud(nuevaSolicitud)
            resultado.onSuccess {
                _uiState.update { it.copy(guardando = false) }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(guardando = false, mensajeError = err.message) }
            }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            val resultado = repository.cancelarSolicitud(solicitudId)
            resultado.onSuccess {
                _uiState.update { state ->
                    val actualizadas = state.solicitudes.map { if (it.id == solicitudId) it.copy(estado = EstadoSolicitud.CANCELADA) else it }
                    val seleccionada = if (state.solicitudSeleccionada?.id == solicitudId) state.solicitudSeleccionada.copy(estado = EstadoSolicitud.CANCELADA) else state.solicitudSeleccionada
                    state.copy(solicitudes = actualizadas, solicitudSeleccionada = seleccionada)
                }
            }.onFailure { err ->
                _uiState.update { it.copy(mensajeError = err.message) }
            }
        }
    }

    // Funciones Admin CRUD de Equipos
    fun agregarEquipo(
        nombre: String,
        categoria: CategoriaEquipo,
        estado: EstadoEquipo,
        userRole: String,
        onSuccess: () -> Unit
    ) {
        if (userRole != "admin") {
            _uiState.update { it.copy(mensajeError = "Acceso denegado: Requiere rol de Administrador") }
            return
        }
        if (nombre.isBlank()) {
            _uiState.update { it.copy(mensajeError = "El nombre del equipo es obligatorio.") }
            return
        }
        viewModelScope.launch {
            val nuevo = Equipo(id = 0, nombre = nombre, categoria = categoria, estado = estado)
            val res = repository.agregarEquipo(nuevo)
            res.onSuccess { onSuccess() }
                .onFailure { err -> _uiState.update { it.copy(mensajeError = err.message) } }
        }
    }

    fun editarEquipo(
        id: Int,
        nombre: String,
        categoria: CategoriaEquipo,
        estado: EstadoEquipo,
        userRole: String,
        onSuccess: () -> Unit
    ) {
        if (userRole != "admin") {
            _uiState.update { it.copy(mensajeError = "Acceso denegado: Requiere rol de Administrador") }
            return
        }
        if (nombre.isBlank()) {
            _uiState.update { it.copy(mensajeError = "El nombre del equipo es obligatorio.") }
            return
        }
        viewModelScope.launch {
            val editado = Equipo(id = id, nombre = nombre, categoria = categoria, estado = estado)
            val res = repository.editarEquipo(editado)
            res.onSuccess { onSuccess() }
                .onFailure { err -> _uiState.update { it.copy(mensajeError = err.message) } }
        }
    }

    fun eliminarEquipo(id: Int, userRole: String, onSuccess: () -> Unit) {
        if (userRole != "admin") {
            _uiState.update { it.copy(mensajeError = "Acceso denegado: Requiere rol de Administrador") }
            return
        }
        viewModelScope.launch {
            val res = repository.eliminarEquipo(id)
            res.onSuccess { onSuccess() }
                .onFailure { err -> _uiState.update { it.copy(mensajeError = err.message) } }
        }
    }

    fun adjuntarEvidencia(
        solicitudId: Int,
        imagenBytes: ByteArray,
        extension: String,
        onSuccess: () -> Unit
    ) {
        _uiState.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            val res = repository.adjuntarEvidencia(solicitudId, imagenBytes, extension)
            res.onSuccess {
                _uiState.update { state -> state.copy(guardando = false) }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { state -> state.copy(guardando = false, mensajeError = err.message) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeError = null, mensajeExito = null) }
    }
}
