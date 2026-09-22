package com.example.prestamolab.repository

import com.example.prestamolab.data.local.dao.EquipoDao
import com.example.prestamolab.data.local.dao.SolicitudDao
import com.example.prestamolab.data.mapper.toDomain
import com.example.prestamolab.data.mapper.toEntity
import com.example.prestamolab.data.mapper.toDto
import com.example.prestamolab.data.remote.api.PrestamoApiService
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class OfflineFirstPrestamoRepository(
    private val equipoDao: EquipoDao,
    private val solicitudDao: SolicitudDao,
    private val apiService: PrestamoApiService
) : PrestamoRepository {

    override fun obtenerEquipos(): Flow<List<Equipo>> {
        return equipoDao.getAllEquipos()
            .map { entities -> entities.map { it.toDomain() } }
            .onStart { 
                seedInitialData()
                syncEquipos() 
            }
    }

    override suspend fun obtenerEquipo(id: Int): Equipo? {
        return equipoDao.getEquipoById(id)?.toDomain()
    }

    override suspend fun crearEquipo(equipo: Equipo): Result<Unit> {
        return try {
            equipoDao.insertEquipos(listOf(equipo.toEntity()))
            try {
                apiService.createEquipo(equipo.toDto())
            } catch (e: Exception) {
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarEquipo(equipo: Equipo): Result<Unit> {
        return try {
            equipoDao.insertEquipos(listOf(equipo.toEntity()))
            try {
                apiService.updateEquipo(equipo.id, equipo.toDto())
            } catch (e: Exception) {
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarEquipo(id: Int): Result<Unit> {
        return try {
            equipoDao.deleteEquipoById(id)
            try {
                apiService.deleteEquipo(id)
            } catch (e: Exception) {
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun obtenerSolicitudes(): Flow<List<SolicitudPrestamo>> {
        return solicitudDao.getAllSolicitudes()
            .map { entities -> entities.map { it.toDomain() } }
            .onStart { syncSolicitudes() }
    }

    override fun obtenerActiveSolicitudes(): Flow<List<SolicitudPrestamo>> {
        return solicitudDao.getActiveSolicitudes()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return solicitudDao.getSolicitudById(id)?.toDomain()
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        return try {
            solicitudDao.insertSolicitud(solicitud.toEntity())
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.RESERVADO)

            try {
                apiService.createSolicitud(solicitud.toDto())
                val entity = solicitudDao.getSolicitudById(solicitud.id)
                if (entity != null) {
                    solicitudDao.updateSolicitud(entity.copy(estadoSincronizacion = "SINCRONIZADA"))
                }
            } catch (e: Exception) {
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
        return try {
            val solicitud = solicitudDao.getSolicitudById(id) ?: return Result.failure(Exception("No encontrada"))
            solicitudDao.updateSolicitud(solicitud.copy(estado = EstadoSolicitud.CANCELADA))
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.DISPONIBLE)

            try {
                apiService.cancelarSolicitud(id)
            } catch (e: Exception) {
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registrarDevolucion(solicitudId: Int, fotoUri: String?, latitud: Double?, longitud: Double?): Result<Unit> {
        return try {
            val solicitud = solicitudDao.getSolicitudById(solicitudId) ?: return Result.failure(Exception("No encontrada"))
            solicitudDao.updateSolicitud(solicitud.copy(
                estado = EstadoSolicitud.DEVUELTA, 
                fotoUri = fotoUri,
                latitud = latitud,
                longitud = longitud
            ))
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.DISPONIBLE)

            try {
                apiService.registrarDevolucion(solicitudId, mapOf(
                    "fotoUri" to fotoUri,
                    "latitud" to latitud?.toString(),
                    "longitud" to longitud?.toString()
                ))
            } catch (e: Exception) {
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun borrarHistorial(): Result<Unit> {
        return try {
            solicitudDao.deleteHistorialCompletado()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedInitialData() {
        val currentEquipos = equipoDao.getAllEquipos().first()
        if (currentEquipos.isEmpty()) {
            val initialEquipos = listOf(
                Equipo(1, "Multímetro Digital", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
                Equipo(2, "Kit de Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
                Equipo(3, "Cámara Digital", CategoriaEquipo.AUDIOVISUAL, EstadoEquipo.DISPONIBLE),
                Equipo(4, "Portátil Lenovo", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE),
                Equipo(5, "Taladro Inalámbrico", CategoriaEquipo.HERRAMIENTAS, EstadoEquipo.DISPONIBLE)
            )
            equipoDao.insertEquipos(initialEquipos.map { it.toEntity() })
        }
    }

    private suspend fun syncEquipos() {
        try {
            val remoteEquipos = apiService.getEquipos()
            equipoDao.insertEquipos(remoteEquipos.map { it.toDomain().toEntity() })
        } catch (e: Exception) {
        }
    }

    private suspend fun syncSolicitudes() {
        try {
            val remoteSolicitudes = apiService.getSolicitudes()
            remoteSolicitudes.forEach { dto ->
                val domain = dto.toDomain()
                solicitudDao.insertSolicitud(domain.toEntity().copy(estadoSincronizacion = "SINCRONIZADA"))
            }
        } catch (e: Exception) {
        }
    }
}
