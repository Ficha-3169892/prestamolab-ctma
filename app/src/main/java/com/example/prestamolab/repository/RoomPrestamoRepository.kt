package com.example.prestamolab.repository

import com.example.prestamolab.data.local.dao.EquipoDao
import com.example.prestamolab.data.local.dao.SolicitudDao
import com.example.prestamolab.data.mapper.toDomain
import com.example.prestamolab.data.mapper.toEntity
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPrestamoRepository(
    private val equipoDao: EquipoDao,
    private val solicitudDao: SolicitudDao
) : PrestamoRepository {

    override fun obtenerEquipos(): Flow<List<Equipo>> {
        return equipoDao.getAllEquipos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun obtenerEquipo(id: Int): Equipo? {
        return equipoDao.getEquipoById(id)?.toDomain()
    }

    override suspend fun crearEquipo(equipo: Equipo): Result<Unit> {
        return try {
            equipoDao.insertEquipos(listOf(equipo.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarEquipo(equipo: Equipo): Result<Unit> {
        return try {
            equipoDao.insertEquipos(listOf(equipo.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarEquipo(id: Int): Result<Unit> {
        return try {
            equipoDao.deleteEquipoById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun obtenerSolicitudes(): Flow<List<SolicitudPrestamo>> {
        return solicitudDao.getAllSolicitudes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun obtenerActiveSolicitudes(): Flow<List<SolicitudPrestamo>> {
        return solicitudDao.getActiveSolicitudes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return solicitudDao.getSolicitudById(id)?.toDomain()
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        return try {
            val equipo = equipoDao.getEquipoById(solicitud.equipoId)
                ?: return Result.failure(Exception("Equipo no encontrado"))
            
            if (equipo.estado != EstadoEquipo.DISPONIBLE) {
                return Result.failure(Exception("Equipo no disponible"))
            }

            solicitudDao.insertSolicitud(solicitud.toEntity())
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.RESERVADO)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
        return try {
            val solicitud = solicitudDao.getSolicitudById(id)
                ?: return Result.failure(Exception("Solicitud no encontrada"))
            
            if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
                return Result.failure(Exception("Solo se pueden cancelar solicitudes SOLICITADA"))
            }

            solicitudDao.updateSolicitud(solicitud.copy(estado = EstadoSolicitud.CANCELADA))
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.DISPONIBLE)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registrarDevolucion(solicitudId: Int, fotoUri: String?, latitud: Double?, longitud: Double?): Result<Unit> {
        return try {
            val solicitud = solicitudDao.getSolicitudById(solicitudId)
                ?: return Result.failure(Exception("Solicitud no encontrada"))
            
            solicitudDao.updateSolicitud(solicitud.copy(
                estado = EstadoSolicitud.DEVUELTA,
                fotoUri = fotoUri,
                latitud = latitud,
                longitud = longitud
            ))
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.DISPONIBLE)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
