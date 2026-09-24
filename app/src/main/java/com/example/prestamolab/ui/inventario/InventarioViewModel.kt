package com.example.prestamolab.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.ErroresEquipo
import com.example.prestamolab.model.ReglasInventario
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
sealed interface EventoInventario {
    data object EquipoGuardado : EventoInventario
}

data class FormularioEquipo(
    /** null: equipo nuevo; si no, el id del equipo que se edita. */
    val equipoId: Int? = null,
    val nombre: String = "",
    val categoria: String = "",
    val errores: ErroresEquipo = ErroresEquipo(),
    val guardando: Boolean = false
)

data class InventarioUiState(
    val equipos: List<Equipo> = emptyList(),
    val formulario: FormularioEquipo = FormularioEquipo(),
    /** La ruta de edición pidió un equipo que no existe. */
    val equipoNoEncontrado: Boolean = false,
    val mensajeError: String? = null
)

/** Inventario de equipos del instructor (HU-12). */
class InventarioViewModel(
    private val repository: PrestamoRepository,
    private val usuario: Usuario
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoInventario>(Channel.BUFFERED)
    val eventos: Flow<EventoInventario> = _eventos.receiveAsFlow()

    // Solo el instructor mantiene el inventario, aunque las rutas ya estén protegidas por rol
    private val puedeGestionar get() = usuario.rol == Rol.INSTRUCTOR

    init {
        viewModelScope.launch {
            repository.equipos.collect { equipos -> _uiState.update { it.copy(equipos = equipos) } }
        }
    }

    /** Carga el equipo en el formulario de edición. */
    fun editarEquipo(id: Int) {
        if (_uiState.value.formulario.equipoId == id) return
        viewModelScope.launch {
            val equipo = repository.obtenerEquipo(id)
            _uiState.update {
                if (equipo == null) it.copy(equipoNoEncontrado = true)
                else it.copy(formulario = FormularioEquipo(equipo.id, equipo.nombre, equipo.categoria))
            }
        }
    }

    fun onNombreChanged(nombre: String) = actualizarFormulario {
        it.copy(nombre = nombre, errores = it.errores.copy(nombre = null))
    }

    fun onCategoriaChanged(categoria: String) = actualizarFormulario {
        it.copy(categoria = categoria, errores = it.errores.copy(categoria = null))
    }

    /** Registra o edita según el formulario; valida primero para mostrar el error de cada campo. */
    fun guardar() {
        val formulario = _uiState.value.formulario
        if (formulario.guardando || !puedeGestionar) return

        val errores = ReglasInventario.validar(formulario.nombre, formulario.categoria)
        if (errores.hayErrores) {
            actualizarFormulario { it.copy(errores = errores) }
            return
        }

        actualizarFormulario { it.copy(guardando = true) }
        _uiState.update { it.copy(mensajeError = null) }
        viewModelScope.launch {
            val resultado = formulario.equipoId
                ?.let { repository.editarEquipo(it, formulario.nombre, formulario.categoria) }
                ?: repository.registrarEquipo(formulario.nombre, formulario.categoria).map { }
            resultado
                .onSuccess {
                    _uiState.update { it.copy(formulario = FormularioEquipo()) }
                    _eventos.send(EventoInventario.EquipoGuardado)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(guardando = false),
                            mensajeError = error.message ?: "No se pudo guardar el equipo"
                        )
                    }
                }
        }
    }

    fun eliminarEquipo(id: Int) {
        if (!puedeGestionar) return
        _uiState.update { it.copy(mensajeError = null) }
        viewModelScope.launch {
            repository.eliminarEquipo(id).onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message) }
            }
        }
    }

    fun limpiarMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    private fun actualizarFormulario(cambio: (FormularioEquipo) -> FormularioEquipo) {
        _uiState.update { it.copy(formulario = cambio(it.formulario)) }
    }

    companion object {
        fun factory(usuario: Usuario): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                InventarioViewModel(app.container.prestamoRepository, usuario)
            }
        }
    }
}
