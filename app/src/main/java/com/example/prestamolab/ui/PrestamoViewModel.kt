package com.example.prestamolab.ui

import androidx.lifecycle.ViewModel
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val errorDuracion: String? = null
)

class PrestamoViewModel : ViewModel() {

    private val repository = InMemoryPrestamoRepository()

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        _uiState.update { state ->
            state.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes()
            )
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

    fun irAFormulario() {
        _uiState.update { it.copy(seccionActual = SeccionApp.FORMULARIO) }
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

    fun guardarSolicitud(): Boolean {
        val estadoActual = _uiState.value
        val equipo = estadoActual.equipoSeleccionado ?: return false

        var hayError = false
        var errAmbiente: String? = null
        var errProposito: String? = null

        if (estadoActual.ambiente.isBlank()) {
            errAmbiente = "El ambiente o destino es obligatorio."
            hayError = true
        }

        if (estadoActual.proposito.length < 10) {
            errProposito = "El propósito debe tener al menos 10 caracteres."
            hayError = true
        }

        if (hayError) {
            _uiState.update {
                it.copy(
                    errorAmbiente = errAmbiente,
                    errorProposito = errProposito
                )
            }
            return false
        }

        val nuevaSolicitud = SolicitudPrestamo(
            id = (100..999).random(),
            equipoId = equipo.id,
            solicitante = "Andrés Vargas",
            fechaInicio = "2026-09-04",
            fechaFin = "2026-09-04",
            estado = "PENDIENTE"
        )

        repository.crearSolicitud(nuevaSolicitud)

        _uiState.update { state ->
            state.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes(),
                seccionActual = SeccionApp.MIS_SOLICITUDES,
                equipoSeleccionado = null,
                ambiente = "",
                proposito = "",
                duracionHoras = "1",
                errorAmbiente = null,
                errorProposito = null
            )
        }
        return true
    }

    fun cancelarSolicitud(idSolicitud: Int) {
        repository.cancelarSolicitud(idSolicitud)
        _uiState.update { state ->
            val listaSinCancelada = repository.obtenerSolicitudes().filter { it.id != idSolicitud && it.estado != "CANCELADA" }
            state.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = listaSinCancelada
            )
        }
    }
}