package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.EquipmentEntity
import com.example.prestamolab.data.local.LoanEntity
import com.example.prestamolab.data.local.PrestamoDao
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPrestamoRepository(private val dao: PrestamoDao) : PrestamoRepository {

    override fun obtenerEquipos(): Flow < List < Equipo > > {
        return dao.obtenerTodosLosEquipos().map { entidades ->
            entidades.map { toDominio(it) }
        }
    }

    override fun obtenerEquipo(id: Int): Flow < Equipo? > {
        return dao.obtenerEquipoPorId(id).map { entidad ->
            entidad?.let { toDominio(it) }
        }
    }

    override fun obtenerSolicitudes(): Flow < List < SolicitudPrestamo > > {
        return dao.obtenerTodasLasSolicitudes().map { entidades ->
            entidades.map { toDominio(it) }
        }
    }

    override fun obtenerSolicitud(id: Int): Flow < SolicitudPrestamo? > {
        return dao.obtenerSolicitudPorId(id).map { entidad ->
            entidad?.let { toDominio(it) }
        }
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result < Unit > {
        return try {
            val equipoActual = dao.obtenerEquipoSync(solicitud.equipoId)
            if (equipoActual == null || equipoActual.estado != EstadoEquipo.DISPONIBLE.name) {
                return Result.failure(Exception("Equipo no disponible"))
            }

            dao.insertarSolicitud(toEntity(solicitud))
            dao.actualizarEquipo(equipoActual.copy(estado = EstadoEquipo.RESERVADO.name))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Int): Result < Unit > {
        return try {
            val solicitud = dao.obtenerSolicitudSync(id)
                ?: return Result.failure(Exception("Solicitud no encontrada"))

            if (solicitud.estado != EstadoSolicitud.SOLICITADA.name) {
                return Result.failure(Exception("Solo se pueden cancelar solicitudes en estado SOLICITADA"))
            }

            dao.actualizarSolicitud(solicitud.copy(estado = EstadoSolicitud.CANCELADA.name))

            val equipo = dao.obtenerEquipoSync(solicitud.equipoId)
            if (equipo != null) {
                dao.actualizarEquipo(equipo.copy(estado = EstadoEquipo.DISPONIBLE.name))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun toDominio(entity: EquipmentEntity): Equipo = Equipo(
        id = entity.id, nombre = entity.nombre,
        categoria = CategoriaEquipo.valueOf(entity.categoria), estado = EstadoEquipo.valueOf(entity.estado)
    )
    private fun toDominio(entity: LoanEntity): SolicitudPrestamo = SolicitudPrestamo(
        id = entity.id, equipoId = entity.equipoId, ambienteDestino = entity.ambienteDestino,
        proposito = entity.proposito, duracionHoras = entity.duracionHoras, estado = EstadoSolicitud.valueOf(entity.estado)
    )
    private fun toEntity(domain: SolicitudPrestamo): LoanEntity = LoanEntity(
        id = domain.id, equipoId = domain.equipoId, ambienteDestino = domain.ambienteDestino,
        proposito = domain.proposito, duracionHoras = domain.duracionHoras, estado = domain.estado.name
    )
}
