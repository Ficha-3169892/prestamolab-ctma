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
    val errorDuracion: String? = null,
    val guardando: Boolean = false
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

    fun seleccionarEquipoPorId(id: Int) {
        val equipo = repository.obtenerEquipo(id)
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
        if (_uiState.value.guardando) return false

        val estadoActual = _uiState.value
        val equipo = estadoActual.equipoSeleccionado ?: return false

        // Evitar solicitud sobre equipo no disponible (TC-12)
        if (equipo.estado != "DISPONIBLE") return false

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
        _uiState.update { it.copy(guardando = true) }

        val nuevaSolicitud = SolicitudPrestamo(
            id = (100..999).random(),
            equipoId = equipo.id,
            solicitante = "Andrés Vargas",
            fechaInicio = "2026-09-04",
            fechaFin = "2026-09-04",
            estado = "SOLICITADA"
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
                errorProposito = null,
                errorDuracion = null,
                guardando = false
            )
        }
        return true
    }

    fun cancelarSolicitud(idSolicitud: Int) {
        val solicitud = repository.obtenerSolicitudes().find { it.id == idSolicitud }
        // TC-16: Re-cancelar solicitud CANCELADA -> sin cambio
        if (solicitud?.estado == "CANCELADA") return

        repository.cancelarSolicitud(idSolicitud)
        _uiState.update { state ->
            state.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes()
            )
        }
    }
}