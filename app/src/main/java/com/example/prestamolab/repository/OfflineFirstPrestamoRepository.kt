package com.example.prestamolab.repository

import android.util.Log
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

    companion object {
        private const val TAG = "OfflineFirstRepo"
    }

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
                Log.e(TAG, "Error al crear equipo en Supabase: ${e.message}", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al crear equipo: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun actualizarEquipo(equipo: Equipo): Result<Unit> {
        return try {
            equipoDao.insertEquipos(listOf(equipo.toEntity()))
            try {
                apiService.updateEquipo("eq.${equipo.id}", equipo.toDto())
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar equipo en Supabase: ${e.message}", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al actualizar equipo: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun eliminarEquipo(id: Int): Result<Unit> {
        return try {
            equipoDao.deleteEquipoById(id)
            try {
                apiService.deleteEquipo("eq.$id")
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar equipo en Supabase: ${e.message}", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al eliminar equipo: ${e.message}", e)
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
                val createdDtos = apiService.createSolicitud(solicitud.toDto())
                val entity = solicitudDao.getSolicitudById(solicitud.id)
                if (entity != null) {
                    val remoteId = createdDtos.firstOrNull()?.id
                    solicitudDao.updateSolicitud(
                        entity.copy(
                            id = if (remoteId != null && remoteId > 0) remoteId else entity.id,
                            estadoSincronizacion = "SINCRONIZADA"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear solicitud en Supabase: ${e.message}", e)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al crear solicitud: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
        return try {
            val solicitud = solicitudDao.getSolicitudById(id) ?: return Result.failure(Exception("No encontrada"))
            solicitudDao.updateSolicitud(solicitud.copy(estado = EstadoSolicitud.CANCELADA))
            equipoDao.updateEstado(solicitud.equipoId, EstadoEquipo.DISPONIBLE)

            try {
                apiService.updateSolicitud("eq.$id", mapOf("estado" to EstadoSolicitud.CANCELADA.name))
            } catch (e: Exception) {
                Log.e(TAG, "Error al cancelar solicitud en Supabase: ${e.message}", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al cancelar solicitud: ${e.message}", e)
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
                val updates = mutableMapOf<String, String?>(
                    "estado" to EstadoSolicitud.DEVUELTA.name,
                    "foto_uri" to fotoUri
                )
                if (latitud != null) updates["latitud"] = latitud.toString()
                if (longitud != null) updates["longitud"] = longitud.toString()

                apiService.updateSolicitud("eq.$solicitudId", updates)
            } catch (e: Exception) {
                Log.e(TAG, "Error al registrar devolución en Supabase: ${e.message}", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al registrar devolución: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun borrarHistorial(): Result<Unit> {
        return try {
            solicitudDao.deleteHistorialCompletado()
            try {
                apiService.deleteSolicitudes("in.(DEVUELTA,CANCELADA,RECHAZADA)")
            } catch (e: Exception) {
                Log.e(TAG, "Error al borrar historial en Supabase: ${e.message}", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error local al borrar historial: ${e.message}", e)
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
            Log.d(TAG, "Equipos recibidos de Supabase: ${remoteEquipos.size}")
            if (remoteEquipos.isNotEmpty()) {
                equipoDao.insertEquipos(remoteEquipos.map { it.toDomain().toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar equipos desde Supabase: ${e.message}", e)
        }
    }

    private suspend fun syncSolicitudes() {
        try {
            val remoteSolicitudes = apiService.getSolicitudes()
            Log.d(TAG, "Solicitudes recibidas de Supabase: ${remoteSolicitudes.size}")
            if (remoteSolicitudes.isNotEmpty()) {
                remoteSolicitudes.forEach { dto ->
                    val domain = dto.toDomain()
                    solicitudDao.insertSolicitud(domain.toEntity().copy(estadoSincronizacion = "SINCRONIZADA"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar solicitudes desde Supabase: ${e.message}", e)
        }
    }
}
