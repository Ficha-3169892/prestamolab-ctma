package com.example.prestamolab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SeccionApp {
    CATALOGO, DETALLE_EQUIPO, FORMULARIO, MIS_SOLICITUDES
}

data class PrestamoUiState(
    val seccionActual: SeccionApp = SeccionApp.CATALOGO,
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val equipoSeleccionado: Equipo? = null,
    val ambiente: String = "",
    val proposito: String = "",
    val duracionHoras: String = "1",
    val errorAmbiente: String? = null,
    val errorProposito: String? = null,
    val errorDuracion: String? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null
)

class PrestamoViewModel(
    private val repository: PrestamoRepository,
    // Temporal hasta implementar el inicio de sesión (HU-10)
    private val solicitante: String = SOLICITANTE_DEMO
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            repository.equipos.collect { equipos ->
                _uiState.update { state ->
                    // Mantiene el detalle sincronizado con el estado actual del equipo
                    val seleccionado = state.equipoSeleccionado?.let { sel ->
                        equipos.find { it.id == sel.id } ?: sel
                    }
                    state.copy(equipos = equipos, equipoSeleccionado = seleccionado)
                }
            }
        }
        viewModelScope.launch {
            repository.solicitudes.collect { solicitudes ->
                _uiState.update { it.copy(solicitudes = solicitudes) }
            }
        }
    }

    fun navegarA(seccion: SeccionApp) {
        _uiState.update { it.copy(seccionActual = seccion) }
    }

    fun seleccionarEquipoParaDetalle(equipo: Equipo) {
        _uiState.update {
            it.copy(
                equipoSeleccionado = equipo,
                seccionActual = SeccionApp.DETALLE_EQUIPO
            )
        }
    }

    fun seleccionarEquipoPorId(id: Int) {
        viewModelScope.launch {
            val equipo = repository.obtenerEquipo(id)
            _uiState.update {
                it.copy(
                    equipoSeleccionado = equipo,
                    seccionActual = SeccionApp.DETALLE_EQUIPO
                )
            }
        }
    }

    fun irAFormulario() {
        _uiState.update { it.copy(seccionActual = SeccionApp.FORMULARIO, mensajeError = null) }
    }

    fun onAmbienteChanged(nuevoAmbiente: String) {
        _uiState.update { it.copy(ambiente = nuevoAmbiente, errorAmbiente = null) }
    }

    fun onPropositoChanged(nuevoProposito: String) {
        _uiState.update { it.copy(proposito = nuevoProposito, errorProposito = null) }
    }

    fun onDuracionChanged(nuevaDuracion: String) {
        _uiState.update { it.copy(duracionHoras = nuevaDuracion, errorDuracion = null) }
    }

    /**
     * Valida el formulario y, si es válido, envía la solicitud al repositorio.
     * Retorna true cuando la solicitud pasó la validación y se despachó.
     */
    fun guardarSolicitud(): Boolean {
        if (_uiState.value.guardando) return false

        val estadoActual = _uiState.value
        val equipo = estadoActual.equipoSeleccionado ?: return false

        // Evitar solicitud sobre equipo no disponible (TC-12)
        if (equipo.estado != EstadoEquipo.DISPONIBLE) return false

        var hayError = false
        var errAmbiente: String? = null
        var errProposito: String? = null
        var errDuracion: String? = null

        if (estadoActual.ambiente.isBlank()) {
            errAmbiente = "El ambiente o destino es obligatorio."
            hayError = true
        }

        // TC-04 al TC-07
        if (estadoActual.proposito.length < 10) {
            errProposito = "Propósito debe tener mínimo 10 caracteres"
            hayError = true
        } else if (estadoActual.proposito.length > 180) {
            errProposito = "Máximo 180 caracteres"
            hayError = true
        }

        // TC-08 al TC-11
        val duracion = estadoActual.duracionHoras.toIntOrNull() ?: 0
        if (duracion < 1 || duracion > 8) {
            errDuracion = "Duración entre 1 y 8 horas"
            hayError = true
        }

        if (hayError) {
            _uiState.update {
                it.copy(
                    errorAmbiente = errAmbiente,
                    errorProposito = errProposito,
                    errorDuracion = errDuracion
                )
            }
            return false
        }

        // Bloqueo de doble pulsación (TC-13)
        _uiState.update { it.copy(guardando = true, mensajeError = null) }

        val nueva = NuevaSolicitud(
            equipoId = equipo.id,
            solicitante = solicitante,
            ambiente = estadoActual.ambiente.trim(),
            proposito = estadoActual.proposito,
            duracionHoras = duracion
        )

        viewModelScope.launch {
            repository.crearSolicitud(nueva)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            seccionActual = SeccionApp.MIS_SOLICITUDES,
                            equipoSeleccionado = null,
                            ambiente = "",
                            proposito = "",
                            duracionHoras = "1",
                            errorAmbiente = null,
                            errorProposito = null,
                            errorDuracion = null,
                            guardando = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(guardando = false, mensajeError = error.message ?: "No se pudo registrar la solicitud")
                    }
                }
        }
        return true
    }

    fun cancelarSolicitud(idSolicitud: Int) {
        viewModelScope.launch {
            repository.cancelarSolicitud(idSolicitud).onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message) }
            }
        }
    }

    companion object {
        const val SOLICITANTE_DEMO = "Andrés Vargas"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                PrestamoViewModel(app.container.prestamoRepository)
            }
        }
    }
}
