package com.example.prestamolab.ui.devolucion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.location.LocationProvider
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaDevolucion
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.model.Ubicacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevolucionUiState(
    val cargando: Boolean = true,
    val solicitud: SolicitudPrestamo? = null,
    val nombreEquipo: String = "",
    val condicion: CondicionEquipo? = null,
    val observacion: String = "",
    val ubicacion: Ubicacion? = null,
    val capturandoUbicacion: Boolean = false,
    val mensajeUbicacion: String? = null,
    val errorCondicion: String? = null,
    val errorObservacion: String? = null,
    val mensajeError: String? = null,
    val guardando: Boolean = false,
    val completada: Boolean = false
) {
    val puedeDevolverse: Boolean get() = solicitud?.estado == EstadoSolicitud.PRESTADO
}

class DevolucionViewModel(
    private val solicitudId: Int,
    private val repository: PrestamoRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevolucionUiState())
    val uiState: StateFlow<DevolucionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // CA-HU06-05: préstamo, equipo y evidencias en una sola consulta
            val detalle = repository.obtenerDetalle(solicitudId)
            val solicitud = detalle?.solicitud
            val equipo = detalle?.equipo
            _uiState.update {
                it.copy(
                    cargando = false,
                    solicitud = solicitud,
                    nombreEquipo = equipo?.nombre ?: solicitud?.let { s -> "Equipo ${s.equipoId}" }.orEmpty()
                )
            }
        }
    }

    fun onCondicionSeleccionada(condicion: CondicionEquipo) {
        _uiState.update { it.copy(condicion = condicion, errorCondicion = null, errorObservacion = null) }
    }

    fun onObservacionChanged(observacion: String) {
        _uiState.update { it.copy(observacion = observacion, errorObservacion = null) }
    }

    /** Se llama solo cuando la pantalla ya confirmó el permiso de ubicación. */
    fun capturarUbicacion() {
        if (_uiState.value.capturandoUbicacion) return
        _uiState.update { it.copy(capturandoUbicacion = true, mensajeUbicacion = null) }
        viewModelScope.launch {
            val resultado = locationProvider.ubicacionActual()
            _uiState.update { estado ->
                resultado.fold(
                    onSuccess = { ubicacion ->
                        estado.copy(
                            capturandoUbicacion = false,
                            ubicacion = ubicacion,
                            mensajeUbicacion = "Ubicación capturada correctamente"
                        )
                    },
                    onFailure = { error ->
                        estado.copy(
                            capturandoUbicacion = false,
                            mensajeUbicacion = error.message ?: "No se pudo obtener la ubicación."
                        )
                    }
                )
            }
        }
    }

    // CA-HU13-04: negar el permiso no bloquea la devolución
    fun onPermisoUbicacionDenegado() {
        _uiState.update {
            it.copy(mensajeUbicacion = "Permiso de ubicación denegado. La devolución se registrará sin coordenadas.")
        }
    }

    fun registrarDevolucion() {
        val estado = _uiState.value
        if (estado.guardando || !estado.puedeDevolverse) return

        val condicion = estado.condicion
        val observacion = estado.observacion.trim()
        val errorCondicion = if (condicion == null) "Selecciona el estado del equipo." else null
        val errorObservacion = when {
            // CA-HU05-04: un daño debe describirse
            condicion == CondicionEquipo.DANADO && observacion.length < MIN_OBSERVACION_DANO ->
                "Describe el daño (mínimo $MIN_OBSERVACION_DANO caracteres)."
            observacion.length > MAX_OBSERVACION -> "Máximo $MAX_OBSERVACION caracteres."
            else -> null
        }
        if (condicion == null || errorObservacion != null) {
            _uiState.update { it.copy(errorCondicion = errorCondicion, errorObservacion = errorObservacion) }
            return
        }

        _uiState.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            repository.registrarDevolucion(
                NuevaDevolucion(solicitudId, condicion, observacion, estado.ubicacion)
            ).onSuccess {
                _uiState.update { it.copy(guardando = false, completada = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(guardando = false, mensajeError = error.message ?: "No se pudo registrar la devolución")
                }
            }
        }
    }

    companion object {
        const val MIN_OBSERVACION_DANO = 10
        const val MAX_OBSERVACION = 300

        fun factory(solicitudId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                DevolucionViewModel(solicitudId, app.container.prestamoRepository, app.container.locationProvider)
            }
        }
    }
}
