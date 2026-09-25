package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.EquipmentEntity
import com.example.prestamolab.data.local.LoanEntity
import com.example.prestamolab.data.local.PrestamoDao
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

class RoomPrestamoRepository(private val dao: PrestamoDao) : PrestamoRepository {

    override fun obtenerEquipos(): List < Equipo > {
        val entidades = dao.obtenerTodosLosEquipos()
        return entidades.map { toDominio(it) }
    }

    override fun obtenerEquipo(id: Int): Equipo? {
        val entidad = dao.obtenerEquipoPorId(id)
        return entidad?.let { toDominio(it) }
    }

    override fun obtenerSolicitudes(): List < SolicitudPrestamo > {
        val entidades = dao.obtenerTodasLasSolicitudes()
        return entidades.map { toDominio(it) }
    }

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        val entidad = dao.obtenerSolicitudPorId(id)
        return entidad?.let { toDominio(it) }
    }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result < Unit > {
        return try {
            val equipoActual = dao.obtenerEquipoPorId(solicitud.equipoId)
            if (equipoActual == null || equipoActual.estado != EstadoEquipo.DISPONIBLE.name) {
                return Result.failure(Exception("Equipo no disponible"))
            }

            // 1. Insertamos la solicitud
            val nuevaEntidad = toEntity(solicitud)
            dao.insertarSolicitud(nuevaEntidad)

            // 2. Actualizamos el equipo a RESERVADO
            val equipoActualizado = equipoActual.copy(estado = EstadoEquipo.RESERVADO.name)
            dao.actualizarEquipo(equipoActualizado)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun cancelarSolicitud(id: Int): Result < Unit > {
        return try {
            val solicitud = dao.obtenerSolicitudPorId(id)
                ?: return Result.failure(Exception("Solicitud no encontrada"))

            if (solicitud.estado != EstadoSolicitud.SOLICITADA.name) {
                return Result.failure(Exception("Solo se pueden cancelar solicitudes en estado SOLICITADA"))
            }

            // 1. Cancelar la solicitud
            val solicitudCancelada = solicitud.copy(estado = EstadoSolicitud.CANCELADA.name)
            dao.actualizarSolicitud(solicitudCancelada)

            // 2. Liberar el equipo
            val equipo = dao.obtenerEquipoPorId(solicitud.equipoId)
            if (equipo != null) {
                val equipoLiberado = equipo.copy(estado = EstadoEquipo.DISPONIBLE.name)
                dao.actualizarEquipo(equipoLiberado)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Funciones privadas de Mapeo (Traducción entre Room y App) ---

    private fun toDominio(entity: EquipmentEntity): Equipo {
        return Equipo(
            id = entity.id,
            nombre = entity.nombre,
            categoria = CategoriaEquipo.valueOf(entity.categoria),
            estado = EstadoEquipo.valueOf(entity.estado)
        )
    }

    private fun toDominio(entity: LoanEntity): SolicitudPrestamo {
        return SolicitudPrestamo(
            id = entity.id,
            equipoId = entity.equipoId,
            ambienteDestino = entity.ambienteDestino,
            proposito = entity.proposito,
            duracionHoras = entity.duracionHoras,
            estado = EstadoSolicitud.valueOf(entity.estado)
        )
    }

    private fun toEntity(domain: SolicitudPrestamo): LoanEntity {
        return LoanEntity(
            id = domain.id,
            equipoId = domain.equipoId,
            ambienteDestino = domain.ambienteDestino,
            proposito = domain.proposito,
            duracionHoras = domain.duracionHoras,
            estado = domain.estado.name
        )
    }
}
