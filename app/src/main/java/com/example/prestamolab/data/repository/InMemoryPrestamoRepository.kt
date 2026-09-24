package com.example.prestamolab.data.repository

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Implementación en memoria; se reemplaza por Room en el Sprint 6. */
class InMemoryPrestamoRepository : PrestamoRepository {

    private val mutex = Mutex()
    private val _equipos = MutableStateFlow(equiposIniciales())
    private val _solicitudes = MutableStateFlow(solicitudesIniciales())

    override val equipos: StateFlow<List<Equipo>> = _equipos.asStateFlow()
    override val solicitudes: StateFlow<List<SolicitudPrestamo>> = _solicitudes.asStateFlow()

    override suspend fun obtenerEquipo(id: Int): Equipo? = _equipos.value.find { it.id == id }

    override suspend fun crearSolicitud(nueva: NuevaSolicitud): Result<SolicitudPrestamo> = mutex.withLock {
        val equipo = _equipos.value.find { it.id == nueva.equipoId }
            ?: return Result.failure(NoSuchElementException("El equipo ${nueva.equipoId} no existe"))
        // Evitar solicitud sobre equipo no disponible (TC-12)
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(IllegalStateException("El equipo no está disponible"))
        }

        val solicitud = SolicitudPrestamo(
            id = (_solicitudes.value.maxOfOrNull { it.id } ?: 0) + 1,
            equipoId = nueva.equipoId,
            solicitante = nueva.solicitante,
            ambiente = nueva.ambiente,
            proposito = nueva.proposito,
            duracionHoras = nueva.duracionHoras,
            fechaInicio = FECHA_DEMO,
            fechaFin = FECHA_DEMO,
            estado = EstadoSolicitud.SOLICITADA
        )
        _solicitudes.update { it + solicitud }
        // Cambiamos a RESERVADO según TC-14
        cambiarEstadoEquipo(nueva.equipoId, EstadoEquipo.RESERVADO)
        Result.success(solicitud)
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> = mutex.withLock {
        val solicitud = _solicitudes.value.find { it.id == id }
            ?: return Result.failure(NoSuchElementException("Solicitud no encontrada"))
        if (solicitud.estado == EstadoSolicitud.CANCELADA) return Result.success(Unit)

        _solicitudes.update { lista ->
            lista.map { if (it.id == id) it.copy(estado = EstadoSolicitud.CANCELADA) else it }
        }
        // Al cancelar, el equipo vuelve a estar disponible (TC-15)
        cambiarEstadoEquipo(solicitud.equipoId, EstadoEquipo.DISPONIBLE)
        Result.success(Unit)
    }

    /** Restaura los datos semilla; lo usan las pruebas instrumentadas para aislar cada caso. */
    fun reiniciar() {
        _equipos.value = equiposIniciales()
        _solicitudes.value = solicitudesIniciales()
    }

    private fun cambiarEstadoEquipo(equipoId: Int, estado: EstadoEquipo) {
        _equipos.update { lista ->
            lista.map { if (it.id == equipoId) it.copy(estado = estado) else it }
        }
    }

    private companion object {
        const val FECHA_DEMO = "2026-09-04"

        fun equiposIniciales() = listOf(
            Equipo(1, "Multímetro Digital", "Herramienta", EstadoEquipo.DISPONIBLE),
            // Reservado por la solicitud semilla #1
            Equipo(2, "Osciloscopio 100MHz", "Laboratorio", EstadoEquipo.RESERVADO),
            Equipo(3, "Cautín Estación de Soldadura", "Herramienta", EstadoEquipo.DISPONIBLE),
            Equipo(4, "Fuente de Poder DC", "Laboratorio", EstadoEquipo.DISPONIBLE)
        )

        fun solicitudesIniciales() = listOf(
            SolicitudPrestamo(
                1, 2, "Andrés Vargas", "Laboratorio 302", "Práctica de señales",
                2, "2026-09-02", "2026-09-05", EstadoSolicitud.SOLICITADA
            )
        )
    }
}
