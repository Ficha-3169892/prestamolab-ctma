package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.PrestamoDao
import com.example.prestamolab.data.local.PrestamoEntity
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.runBlocking

class RoomPrestamoRepository(
    private val dao: PrestamoDao
) : PrestamoRepository {

    private val inMemoryRepository = InMemoryPrestamoRepository()

    override fun obtenerEquipos(): List<Equipo> {
        return inMemoryRepository.obtenerEquipos()
    }

    override fun obtenerEquipo(id: Int): Equipo? {
        return inMemoryRepository.obtenerEquipo(id)
    }

    override fun obtenerSolicitudes(): List<SolicitudPrestamo> {
        return runBlocking {
            dao.obtenerTodos().map { entityToDomain(it) }
        }
    }

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return runBlocking {
            val entity = dao.obtenerPorId(id) ?: return@runBlocking null
            entityToDomain(entity)
        }
    }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        return try {
            runBlocking {
                val entity = domainToEntity(solicitud)
                dao.insertar(entity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        return try {
            runBlocking {
                val estadoCancelado = EstadoSolicitud.entries.find {
                    it.name.contains("CANCEL", ignoreCase = true)
                } ?: EstadoSolicitud.entries.first()

                dao.actualizarEstado(id, estadoCancelado.name)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun entityToDomain(entity: PrestamoEntity): SolicitudPrestamo {
        val estadoEnum = try {
            EstadoSolicitud.valueOf(entity.estado)
        } catch (e: Exception) {
            EstadoSolicitud.entries.first()
        }

        return SolicitudPrestamo(
            id = entity.id,
            equipoId = entity.equipoId,
            ambienteDestino = entity.ambienteDestino,
            duracionHoras = entity.duracionHoras,
            proposito = entity.proposito,
            estado = estadoEnum
        )
    }

    private fun domainToEntity(solicitud: SolicitudPrestamo): PrestamoEntity {
        return PrestamoEntity(
            id = 0,
            equipoId = solicitud.equipoId,
            ambienteDestino = solicitud.ambienteDestino,
            duracionHoras = solicitud.duracionHoras,
            proposito = solicitud.proposito,
            estado = solicitud.estado.name
        )
    }
}