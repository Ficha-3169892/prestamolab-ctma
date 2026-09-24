package com.example.prestamolab.ui.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.repository.ActividadRepository
import com.example.prestamolab.model.Actividad
import com.example.prestamolab.model.DatosActividad
import com.example.prestamolab.model.ErroresActividad
import com.example.prestamolab.model.ReglasActividad
import com.example.prestamolab.model.Rol
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
sealed interface EventoActividad {
    data object ActividadGuardada : EventoActividad
}

data class FormularioActividad(
    /** null: actividad nueva; si no, el id de la que se edita. */
    val actividadId: Int? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val ambiente: String = "",
    val fecha: String = "",
    val errores: ErroresActividad = ErroresActividad(),
    val guardando: Boolean = false
) {
    fun datos() = DatosActividad(titulo, descripcion, ambiente, fecha)
}

data class ActividadesUiState(
    val actividades: List<Actividad> = emptyList(),
    /** CA-HU11-05: el estudiante las ve en modo solo lectura. */
    val puedeGestionar: Boolean = false,
    val formulario: FormularioActividad = FormularioActividad(),
    val actividadNoEncontrada: Boolean = false,
    val mensajeError: String? = null
)

/** Actividades formativas (HU-11). */
class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val usuario: Usuario,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val puedeGestionar = usuario.rol == Rol.INSTRUCTOR

    private val _uiState = MutableStateFlow(ActividadesUiState(puedeGestionar = puedeGestionar))
    val uiState: StateFlow<ActividadesUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoActividad>(Channel.BUFFERED)
    val eventos: Flow<EventoActividad> = _eventos.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.actividades.collect { lista -> _uiState.update { it.copy(actividades = lista) } }
        }
    }

    /** Carga la actividad en el formulario de edición. */
    fun editarActividad(id: Int) {
        if (_uiState.value.formulario.actividadId == id) return
        viewModelScope.launch {
            val actividad = repository.obtener(id)
            _uiState.update {
                if (actividad == null) it.copy(actividadNoEncontrada = true)
                else it.copy(
                    formulario = FormularioActividad(
                        actividad.id, actividad.titulo, actividad.descripcion, actividad.ambiente, actividad.fecha
                    )
                )
            }
        }
    }

    fun onTituloChanged(valor: String) = actualizarFormulario {
        it.copy(titulo = valor, errores = it.errores.copy(titulo = null))
    }

    fun onDescripcionChanged(valor: String) = actualizarFormulario {
        it.copy(descripcion = valor, errores = it.errores.copy(descripcion = null))
    }

    fun onAmbienteChanged(valor: String) = actualizarFormulario {
        it.copy(ambiente = valor, errores = it.errores.copy(ambiente = null))
    }

    fun onFechaChanged(valor: String) = actualizarFormulario {
        it.copy(fecha = valor, errores = it.errores.copy(fecha = null))
    }

    /** Crea o edita según el formulario; valida primero para mostrar el error de cada campo. */
    fun guardar() {
        val formulario = _uiState.value.formulario
        if (formulario.guardando || !puedeGestionar) return

        val errores = ReglasActividad.validar(formulario.datos(), reloj())
        if (errores.hayErrores) {
            actualizarFormulario { it.copy(errores = errores) }
            return
        }

        actualizarFormulario { it.copy(guardando = true) }
        _uiState.update { it.copy(mensajeError = null) }
        viewModelScope.launch {
            val resultado = formulario.actividadId
                ?.let { repository.editar(it, formulario.datos()) }
                ?: repository.crear(formulario.datos(), usuario.id).map { }
            resultado
                .onSuccess {
                    _uiState.update { it.copy(formulario = FormularioActividad()) }
                    _eventos.send(EventoActividad.ActividadGuardada)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(guardando = false),
                            mensajeError = error.message ?: "No se pudo guardar la actividad"
                        )
                    }
                }
        }
    }

    fun eliminarActividad(id: Int) {
        if (!puedeGestionar) return
        viewModelScope.launch {
            repository.eliminar(id).onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message) }
            }
        }
    }

    private fun actualizarFormulario(cambio: (FormularioActividad) -> FormularioActividad) {
        _uiState.update { it.copy(formulario = cambio(it.formulario)) }
    }

    companion object {
        fun factory(usuario: Usuario): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                ActividadesViewModel(app.container.actividadRepository, usuario)
            }
        }
    }
}
