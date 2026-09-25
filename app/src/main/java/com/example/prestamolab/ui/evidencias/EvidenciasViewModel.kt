package com.example.prestamolab.ui.evidencias

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.evidencias.AlmacenFotos
import com.example.prestamolab.data.evidencias.FotoReservada
import com.example.prestamolab.data.location.LocationProvider
import com.example.prestamolab.data.repository.EvidenciaRepository
import com.example.prestamolab.model.EtapaEvidencia
import com.example.prestamolab.model.Evidencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EvidenciasUiState(
    val evidencias: List<Evidencia> = emptyList(),
    /** Aviso para el usuario: permiso negado, cámara no disponible o foto guardada. */
    val mensaje: String? = null,
    val esError: Boolean = false
)

/** Evidencias fotográficas de un préstamo en una etapa (HU-08). */
class EvidenciasViewModel(
    private val repository: EvidenciaRepository,
    private val almacen: AlmacenFotos,
    private val solicitudId: Int,
    private val etapa: EtapaEvidencia,
    private val estadoGuardado: SavedStateHandle = SavedStateHandle(),
    private val locationProvider: LocationProvider? = null,
    /** Mínimo privilegio: el GPS se agrega solo si el estudiante ya concedió la ubicación (p. ej. al devolver). */
    private val tienePermisoUbicacion: () -> Boolean = { false }
) : ViewModel() {

    private val _uiState = MutableStateFlow(EvidenciasUiState())
    val uiState: StateFlow<EvidenciasUiState> = _uiState.asStateFlow()

    /**
     * Archivo que la cámara está llenando. Va en SavedStateHandle: con la cámara abierta, Android
     * puede cerrar la app por memoria y la foto confirmada no debe perderse.
     */
    private var fotoEnCurso: FotoReservada?
        get() {
            val uri = estadoGuardado.get<String>(CLAVE_URI) ?: return null
            return FotoReservada(uri, estadoGuardado.get<String>(CLAVE_RUTA).orEmpty())
        }
        set(valor) {
            estadoGuardado[CLAVE_URI] = valor?.uri
            estadoGuardado[CLAVE_RUTA] = valor?.ruta
        }

    init {
        viewModelScope.launch {
            repository.evidencias(solicitudId).collect { lista -> _uiState.update { it.copy(evidencias = lista) } }
        }
    }

    /** Reserva el archivo y devuelve la URI que recibe la cámara. */
    fun prepararFoto(): String {
        fotoEnCurso?.let(almacen::descartar)
        return almacen.reservar().also { fotoEnCurso = it }.uri
    }

    /** Resultado de TakePicture: true si el usuario confirmó la foto. */
    fun onFotoTomada(confirmada: Boolean) {
        val foto = fotoEnCurso ?: return
        fotoEnCurso = null
        if (!confirmada) {
            // CA-HU08-04: cancelar la cámara no crea evidencia ni deja archivos
            almacen.descartar(foto)
            return
        }
        viewModelScope.launch {
            repository.registrar(solicitudId, etapa, foto.uri)
                .onSuccess { evidencia ->
                    informar("Evidencia guardada. Se subirá al sincronizar.", esError = false)
                    agregarUbicacion(evidencia.id)
                }
                .onFailure { error ->
                    almacen.descartar(foto)
                    informar(error.message ?: "No se pudo guardar la evidencia", esError = true)
                }
        }
    }

    /** La foto ya está guardada: el GPS puede tardar hasta 15 s y, si falla, la evidencia queda sin ubicación. */
    private suspend fun agregarUbicacion(evidenciaId: Int) {
        val proveedor = locationProvider ?: return
        if (!tienePermisoUbicacion()) return
        proveedor.ubicacionActual().onSuccess { repository.agregarUbicacion(evidenciaId, it) }
    }

    /** Miniatura para la lista; se decodifica fuera del hilo principal. */
    suspend fun miniatura(uri: String): Bitmap? = withContext(Dispatchers.IO) { almacen.miniatura(uri) }

    /** CA-HU08-03: sin permiso se explica el motivo y la app sigue funcionando. */
    fun onPermisoCamaraDenegado() = informar(MENSAJE_SIN_PERMISO, esError = true)

    /** El teléfono no tiene una app de cámara que atienda la captura. */
    fun onCamaraNoDisponible() {
        fotoEnCurso?.let(almacen::descartar)
        fotoEnCurso = null
        informar("No se encontró una app de cámara en el teléfono.", esError = true)
    }

    fun descartarMensaje() = _uiState.update { it.copy(mensaje = null) }

    private fun informar(mensaje: String, esError: Boolean) = _uiState.update { it.copy(mensaje = mensaje, esError = esError) }

    companion object {
        private const val CLAVE_URI = "fotoEnCursoUri"
        private const val CLAVE_RUTA = "fotoEnCursoRuta"

        const val MENSAJE_SIN_PERMISO =
            "Sin permiso de cámara no se pueden adjuntar fotos. Puedes concederlo en Ajustes > Aplicaciones > PréstamoLab."

        fun factory(solicitudId: Int, etapa: EtapaEvidencia): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                EvidenciasViewModel(
                    app.container.evidenciaRepository, app.container.almacenFotos, solicitudId, etapa, createSavedStateHandle(),
                    app.container.locationProvider
                ) {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
                        ContextCompat.checkSelfPermission(app, it) == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
        }
    }
}
