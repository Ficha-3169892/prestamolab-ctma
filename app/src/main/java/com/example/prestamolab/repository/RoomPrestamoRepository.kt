package com.example.prestamolab.repository

import android.content.Context
import com.example.prestamolab.data.local.PrestamoDatabase
import com.example.prestamolab.data.local.toDomain
import com.example.prestamolab.data.local.toEntity
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.runBlocking

class RoomPrestamoRepository(context: Context) : PrestamoRepository {

    private val db = PrestamoDatabase.getDatabase(context)
    private val equipoDao = db.equipoDao()
    private val solicitudDao = db.solicitudDao()

    override fun obtenerEquipos(): List<Equipo> = runBlocking {
        equipoDao.getAllEquipos().map { it.toDomain() }
    }

    override fun obtenerEquipo(id: Int): Equipo? = runBlocking {
        equipoDao.getEquipoById(id)?.toDomain()
    }

    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = runBlocking {
        solicitudDao.getAllSolicitudes().map { it.toDomain() }
    }

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = runBlocking {
        solicitudDao.getSolicitudById(id)?.toDomain()
    }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> = runBlocking {
        try {
            val equipo = equipoDao.getEquipoById(solicitud.equipoId)
            if (equipo == null) {
                return@runBlocking Result.failure(Exception("El equipo no existe"))
            }
            if (equipo.estado != "DISPONIBLE") {
                return@runBlocking Result.failure(Exception("El equipo no está disponible"))
            }
            solicitudDao.insertSolicitud(solicitud.toEntity())
            equipoDao.updateEstado(solicitud.equipoId, "RESERVADO")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> = runBlocking {
        try {
            val solicitud = solicitudDao.getSolicitudById(id)
            if (solicitud == null) {
                return@runBlocking Result.failure(Exception("La solicitud no existe"))
            }
            if (solicitud.estado == EstadoSolicitud.CANCELADA.name || solicitud.estado == EstadoSolicitud.DEVUELTA.name) {
                return@runBlocking Result.failure(Exception("La solicitud ya está cancelada o finalizada"))
            }
            solicitudDao.updateEstado(id, EstadoSolicitud.CANCELADA.name)
            equipoDao.updateEstado(solicitud.equipoId, "DISPONIBLE")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
