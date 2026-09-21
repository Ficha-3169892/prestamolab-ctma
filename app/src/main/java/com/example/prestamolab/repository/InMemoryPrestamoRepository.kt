package com.example.prestamolab.repository

import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryPrestamoRepository : PrestamoRepository {

    private val _equipos = MutableStateFlow(listOf(
        Equipo(1, "Multímetro Digital", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Kit de Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Cámara Digital", CategoriaEquipo.AUDIOVISUAL, EstadoEquipo.RESERVADO),
        Equipo(4, "Portátil Lenovo", CategoriaEquipo.COMPUTO, EstadoEquipo.PRESTADO),
        Equipo(5, "Taladro Inalámbrico", CategoriaEquipo.HERRAMIENTAS, EstadoEquipo.DISPONIBLE)
    ))

    private val _solicitudes = MutableStateFlow<List<SolicitudPrestamo>>(emptyList())

    override fun obtenerEquipos(): Flow<List<Equipo>> = _equipos

    override suspend fun obtenerEquipo(id: Int): Equipo? {
        return _equipos.value.find { it.id == id }
    }

    override fun obtenerSolicitudes(): Flow<List<SolicitudPrestamo>> = _solicitudes

    override fun obtenerActiveSolicitudes(): Flow<List<SolicitudPrestamo>> {
        return _solicitudes.map { list ->
            list.filter { it.estado in listOf(EstadoSolicitud.SOLICITADA, EstadoSolicitud.APROBADA, EstadoSolicitud.ENTREGADA) }
        }
    }

    override suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return _solicitudes.value.find { it.id == id }
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        val equipo = obtenerEquipo(solicitud.equipoId) ?: return Result.failure(IllegalArgumentException("El equipo no existe"))
        
        if (_solicitudes.value.any { it.equipoId == solicitud.equipoId && it.estado == EstadoSolicitud.SOLICITADA }) {
            return Result.failure(IllegalStateException("Ya existe una solicitud activa para este equipo"))
        }

        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(IllegalStateException("El equipo no está disponible"))
        }

        _solicitudes.value = _solicitudes.value + solicitud
        actualizarEstadoEquipo(solicitud.equipoId, EstadoEquipo.RESERVADO)
        return Result.success(Unit)
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
        val currentSolicitudes = _solicitudes.value.toMutableList()
        val indice = currentSolicitudes.indexOfFirst { it.id == id }

        if (indice == -1) return Result.failure(IllegalArgumentException("La solicitud no existe"))

        val solicitud = currentSolicitudes[indice]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(IllegalStateException("Solo se pueden cancelar solicitudes SOLICITADA"))
        }

        currentSolicitudes[indice] = solicitud.copy(estado = EstadoSolicitud.CANCELADA)
        _solicitudes.value = currentSolicitudes
        actualizarEstadoEquipo(solicitud.equipoId, EstadoEquipo.DISPONIBLE)
        return Result.success(Unit)
    }

    override suspend fun registrarDevolucion(solicitudId: Int, fotoUri: String?, latitud: Double?, longitud: Double?): Result<Unit> {
        val currentSolicitudes = _solicitudes.value.toMutableList()
        val indice = currentSolicitudes.indexOfFirst { it.id == solicitudId }

        if (indice == -1) return Result.failure(IllegalArgumentException("La solicitud no existe"))

        val solicitud = currentSolicitudes[indice]
        currentSolicitudes[indice] = solicitud.copy(
            estado = EstadoSolicitud.DEVUELTA,
            fotoUri = fotoUri,
            latitud = latitud,
            longitud = longitud
        )
        _solicitudes.value = currentSolicitudes
        actualizarEstadoEquipo(solicitud.equipoId, EstadoEquipo.DISPONIBLE)
        return Result.success(Unit)
    }

    private fun actualizarEstadoEquipo(equipoId: Int, nuevoEstado: EstadoEquipo) {
        val currentEquipos = _equipos.value.toMutableList()
        val indice = currentEquipos.indexOfFirst { it.id == equipoId }
        if (indice != -1) {
            currentEquipos[indice] = currentEquipos[indice].copy(estado = nuevoEstado)
            _equipos.value = currentEquipos
        }
    }
}
