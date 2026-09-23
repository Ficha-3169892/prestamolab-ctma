package com.example.prestamolab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.util.ambienteValido
import com.example.prestamolab.util.duracionValida
import com.example.prestamolab.util.propositoValido
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PrestamoViewModel(
    private val repository: PrestamoRepository
) : ViewModel() {

    private val _busquedaQuery = MutableStateFlow("")
    private val _operacionState = MutableStateFlow<OperacionUiState>(OperacionUiState.Idle)
    private val _reintentarTrigger = MutableStateFlow(0)

    private val listadoStateFlow: Flow<ListadoUiState> = combine(_busquedaQuery, _reintentarTrigger) { query, _ ->
        query
    }.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.obtenerSolicitudes()
        } else {
            repository.buscarSolicitudes(query)
        }
    }.map { lista ->
        if (lista.isEmpty()) {
            ListadoUiState.Vacio
        } else {
            ListadoUiState.Contenido(lista)
        }
    }.catch { throwable ->
        emit(ListadoUiState.Error(throwable.localizedMessage ?: "Error desconocido"))
    }

    val uiState: StateFlow<PrestamoUiState> = combine(
        listadoStateFlow,
        _operacionState,
        _busquedaQuery
    ) { listadoState, operacionState, query ->
        val solicitudes = (listadoState as? ListadoUiState.Contenido)?.lista ?: emptyList()
        val guardando = operacionState is OperacionUiState.EnCurso
        val mensaje = when (operacionState) {
            is OperacionUiState.Exitosa -> operacionState.mensaje
            is OperacionUiState.Fallida -> operacionState.mensaje
            else -> if (listadoState is ListadoUiState.Error) listadoState.mensaje else null
        }

        PrestamoUiState(
            equipos = repository.obtenerEquipos(),
            solicitudes = solicitudes,
            mensaje = mensaje,
            guardando = guardando,
            busquedaQuery = query,
            listadoState = listadoState,
            operacionState = operacionState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrestamoUiState()
    )

    fun actualizarBusqueda(query: String) {
        _busquedaQuery.value = query
    }

    fun buscarSolicitudes(query: String) {
        actualizarBusqueda(query)
    }

    fun registrarSolicitud(
        equipoId: Int,
        ambiente: String,
        proposito: String,
        duracionHoras: Int,
        onSuccess: () -> Unit = {}
    ) {
        if (_operacionState.value is OperacionUiState.EnCurso) return

        if (!ambienteValido(ambiente)) {
            _operacionState.value = OperacionUiState.Fallida("El ambiente o destino es obligatorio.")
            return
        }
        if (!propositoValido(proposito)) {
            _operacionState.value = OperacionUiState.Fallida("El propósito debe tener entre 10 y 180 caracteres.")
            return
        }
        if (!duracionValida(duracionHoras)) {
            _operacionState.value = OperacionUiState.Fallida("La duración debe estar entre 1 y 8 horas.")
            return
        }

        viewModelScope.launch {
            _operacionState.value = OperacionUiState.EnCurso
            try {
                val solicitudesActuales = uiState.value.solicitudes
                val nuevoId = (solicitudesActuales.maxOfOrNull { it.id } ?: 0) + 1

                val nuevaSolicitud = SolicitudPrestamo(
                    id = nuevoId,
                    equipoId = equipoId,
                    ambienteDestino = ambiente.trim(),
                    proposito = proposito.trim(),
                    duracionHoras = duracionHoras,
                    estado = EstadoSolicitud.SOLICITADA
                )

                val resultado = repository.crearSolicitud(nuevaSolicitud)

                resultado.onSuccess {
                    _operacionState.value = OperacionUiState.Exitosa("Solicitud registrada con éxito.")
                    onSuccess()
                }.onFailure { error ->
                    if (error is CancellationException) throw error
                    _operacionState.value = OperacionUiState.Fallida(error.localizedMessage ?: "Error al registrar la solicitud.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionState.value = OperacionUiState.Fallida(e.localizedMessage ?: "Error al registrar la solicitud.")
            }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _operacionState.value = OperacionUiState.EnCurso
            try {
                val resultado = repository.cancelarSolicitud(solicitudId)
                resultado.onSuccess {
                    _operacionState.value = OperacionUiState.Exitosa("Solicitud cancelada con éxito.")
                }.onFailure { error ->
                    if (error is CancellationException) throw error
                    _operacionState.value = OperacionUiState.Fallida(error.localizedMessage ?: "Error al cancelar la solicitud.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionState.value = OperacionUiState.Fallida(e.localizedMessage ?: "Error al cancelar la solicitud.")
            }
        }
    }

    fun limpiarOperacionState() {
        _operacionState.value = OperacionUiState.Idle
    }

    fun limpiarMensaje() {
        limpiarOperacionState()
    }

    fun reintentarCarga() {
        _reintentarTrigger.value += 1
    }

    fun cargarDatos() {
        reintentarCarga()
    }
}
