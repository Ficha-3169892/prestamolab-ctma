package com.example.prestamolab.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.location.LocationProvider
import com.example.prestamolab.data.preferencias.PreferenciasCatalogo
import com.example.prestamolab.data.preferencias.PreferenciasCatalogoEnMemoria
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.data.sync.AvisosSincronizacion
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.FiltroCatalogo
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Eventos de una sola vez que el NavHost convierte en navegación. */
sealed interface EventoPrestamo {
    data class SolicitudRegistrada(val solicitudId: Int) : EventoPrestamo
}

data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    /** Catálogo después de aplicar [filtro] (CA-HU01-03). */
    val equiposCatalogo: List<Equipo> = emptyList(),
    val filtro: FiltroCatalogo = FiltroCatalogo(),
    val categorias: List<String> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val equipoSeleccionado: Equipo? = null,
    /** Id pedido por la ruta que no existe en el repositorio (CA-HU02-02). */
    val equipoNoEncontrado: Int? = null,
    val ambiente: String = "",
    val proposito: String = "",
    val duracionHoras: String = "1",
    val errorAmbiente: String? = null,
    val errorProposito: String? = null,
    val errorDuracion: String? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    /** Problema de sincronización con Supabase que el usuario debe conocer (CA-HU07-04). */
    val avisoSincronizacion: String? = null
)

class PrestamoViewModel(
    private val repository: PrestamoRepository,
    private val usuario: Usuario = USUARIO_DEMO,
    private val avisos: AvisosSincronizacion = AvisosSincronizacion(),
    private val preferencias: PreferenciasCatalogo = PreferenciasCatalogoEnMemoria(),
    private val locationProvider: LocationProvider? = null,
    /** CA-HU13-03: el GPS se agrega solo si el estudiante ya concedió la ubicación (no se pide aquí). */
    private val tienePermisoUbicacion: () -> Boolean = { false }
) : ViewModel() {

    private val rol: Rol get() = usuario.rol

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoPrestamo>(Channel.BUFFERED)
    val eventos: Flow<EventoPrestamo> = _eventos.receiveAsFlow()

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
                    state.copy(equipos = equipos, equipoSeleccionado = seleccionado).conCatalogo()
                }
            }
        }
        viewModelScope.launch {
            // CA-HU01-04: el filtro guardado se restaura al abrir la app
            preferencias.filtro.collect { filtro -> _uiState.update { it.copy(filtro = filtro).conCatalogo() } }
        }
        viewModelScope.launch {
            repository.solicitudes.collect { solicitudes ->
                // El instructor revisa todas; el estudiante solo ve las suyas
                val visibles = if (rol == Rol.INSTRUCTOR) solicitudes else solicitudes.filter { it.usuarioId == usuario.id }
                _uiState.update { it.copy(solicitudes = visibles) }
            }
        }
        viewModelScope.launch {
            avisos.mensaje.collect { aviso -> _uiState.update { it.copy(avisoSincronizacion = aviso) } }
        }
    }

    fun descartarAvisoSincronizacion() = avisos.descartar()

    fun onSoloDisponiblesChanged(soloDisponibles: Boolean) =
        guardarFiltro(_uiState.value.filtro.copy(soloDisponibles = soloDisponibles))

    /** null muestra todas las categorías. */
    fun onCategoriaSeleccionada(categoria: String?) = guardarFiltro(_uiState.value.filtro.copy(categoria = categoria))

    fun quitarFiltros() = guardarFiltro(FiltroCatalogo())

    private fun guardarFiltro(filtro: FiltroCatalogo) {
        // Se muestra al instante; DataStore lo confirma al guardar
        _uiState.update { it.copy(filtro = filtro).conCatalogo() }
        viewModelScope.launch { preferencias.guardar(filtro) }
    }

    private fun PrestamoUiState.conCatalogo() = copy(
        equiposCatalogo = filtro.aplicar(equipos),
        categorias = FiltroCatalogo.categorias(equipos)
    )

    fun seleccionarEquipoParaDetalle(equipo: Equipo) {
        _uiState.update { it.copy(equipoSeleccionado = equipo, equipoNoEncontrado = null) }
    }

    /** Lo usan las rutas `equipo/{id}` y `equipo/{id}/solicitud`, que solo reciben el id. */
    fun seleccionarEquipoPorId(id: Int) {
        if (_uiState.value.equipoSeleccionado?.id == id) return
        viewModelScope.launch {
            val equipo = repository.obtenerEquipo(id)
            _uiState.update {
                it.copy(equipoSeleccionado = equipo, equipoNoEncontrado = if (equipo == null) id else null)
            }
        }
    }

    /** Al abrir el formulario no se arrastra el error de un intento anterior. */
    fun limpiarMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
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
        // Solo el estudiante solicita préstamos (CA-HU03-08), aunque la UI oculte el botón
        if (rol != Rol.ESTUDIANTE) return false

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
        // BUG-13: los espacios y saltos de línea de los extremos no cuentan ni se guardan
        val proposito = estadoActual.proposito.trim()
        if (proposito.length < 10) {
            errProposito = "Propósito debe tener mínimo 10 caracteres"
            hayError = true
        } else if (proposito.length > 180) {
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
            usuarioId = usuario.id,
            solicitante = usuario.nombre,
            ambiente = estadoActual.ambiente.trim(),
            proposito = proposito,
            duracionHoras = duracion
        )

        viewModelScope.launch {
            repository.crearSolicitud(nueva)
                .onSuccess { solicitud ->
                    _uiState.update { state ->
                        state.copy(
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
                    _eventos.send(EventoPrestamo.SolicitudRegistrada(solicitud.id))
                    agregarUbicacion(solicitud.id)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(guardando = false, mensajeError = error.message ?: "No se pudo registrar la solicitud")
                    }
                }
        }
        return true
    }

    /** La solicitud ya está guardada: el GPS puede tardar y, si falla, queda sin ubicación. */
    private suspend fun agregarUbicacion(solicitudId: Int) {
        val proveedor = locationProvider ?: return
        if (!tienePermisoUbicacion()) return
        proveedor.ubicacionActual().onSuccess { repository.agregarUbicacion(solicitudId, it) }
    }

    fun cancelarSolicitud(idSolicitud: Int) {
        viewModelScope.launch {
            repository.cancelarSolicitud(idSolicitud).onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message) }
            }
        }
    }

    /** HU-14: solo el instructor revisa, aunque la ruta ya esté protegida por rol. */
    fun aprobarSolicitud(idSolicitud: Int) {
        if (rol != Rol.INSTRUCTOR) return
        viewModelScope.launch {
            repository.aprobarSolicitud(idSolicitud, usuario.id).onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message) }
            }
        }
    }

    fun rechazarSolicitud(idSolicitud: Int, motivo: String) {
        if (rol != Rol.INSTRUCTOR) return
        viewModelScope.launch {
            repository.rechazarSolicitud(idSolicitud, usuario.id, motivo).onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message) }
            }
        }
    }

    companion object {
        /** Usuario por defecto de las pruebas unitarias. */
        val USUARIO_DEMO = Usuario("u-demo", "Andrés Vargas", "estudiante@sena.edu.co", Rol.ESTUDIANTE)

        fun factory(usuario: Usuario): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                PrestamoViewModel(
                    app.container.prestamoRepository, usuario, app.container.avisosSincronizacion,
                    app.container.preferenciasCatalogo, app.container.locationProvider
                ) {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
                        ContextCompat.checkSelfPermission(app, it) == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
        }
    }
}
