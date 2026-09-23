package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.PrestamoDao
import com.example.prestamolab.data.local.toDomain
import com.example.prestamolab.data.local.toEntity
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomPrestamoRepository(
    private val prestamoDao: PrestamoDao
) : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(1, "Multímetro Digital", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Kit Raspberry Pi 4", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Osciloscopio Portátil", CategoriaEquipo.MEDICION, EstadoEquipo.PRESTADO),
        Equipo(4, "Cautín Regulable", CategoriaEquipo.HERRAMIENTAS, EstadoEquipo.DISPONIBLE)
    )

    override fun obtenerEquipos(): List<Equipo> = equipos.toList()

    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }

    override fun obtenerSolicitudes(): Flow<List<SolicitudPrestamo>> {
        return prestamoDao.obtenerTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun buscarSolicitudes(query: String): Flow<List<SolicitudPrestamo>> {
        return prestamoDao.buscarPorAmbienteOProposito(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return null
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
            if (equipoIndex == -1) {
                return@withContext Result.failure(IllegalArgumentException("El equipo especificado no existe."))
            }

            val equipo = equipos[equipoIndex]
            if (equipo.estado != EstadoEquipo.DISPONIBLE) {
                return@withContext Result.failure(IllegalStateException("El equipo no está disponible para préstamo."))
            }

            equipos[equipoIndex] = equipo.copy(estado = EstadoEquipo.RESERVADO)
            prestamoDao.insertar(solicitud.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val prestamoEntity = prestamoDao.obtenerPorId(id.toInt())
                ?: return@withContext Result.failure(IllegalArgumentException("La solicitud no existe."))

            if (prestamoEntity.estado != EstadoSolicitud.SOLICITADA) {
                return@withContext Result.failure(IllegalStateException("Solo se pueden cancelar solicitudes en estado SOLICITADA."))
            }

            val entidadActualizada = prestamoEntity.copy(estado = EstadoSolicitud.CANCELADA)
            prestamoDao.actualizar(entidadActualizada)

            val equipoIndex = equipos.indexOfFirst { it.id == prestamoEntity.equipoId }
            if (equipoIndex != -1) {
                equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
        return cancelarSolicitud(id.toLong())
    }
}
