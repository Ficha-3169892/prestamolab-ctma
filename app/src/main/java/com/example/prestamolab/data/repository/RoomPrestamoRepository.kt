package com.example.prestamolab.data.repository

import androidx.room.withTransaction
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.data.local.entity.ReturnEntity
import com.example.prestamolab.data.local.entity.aDominio
import com.example.prestamolab.model.Devolucion
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaDevolucion
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.PrestamoDetalle
import com.example.prestamolab.model.Ubicacion
import com.example.prestamolab.model.ReglasInventario
import com.example.prestamolab.model.ResultadoRevision
import com.example.prestamolab.model.RevisionSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Fuente de verdad local: la UI solo lee de Room mediante Flow (HU-06). Cada cambio queda
 * PENDIENTE y [alCambiarLocalmente] pide enviarlo a Supabase cuando haya conexión (HU-07).
 */
class RoomPrestamoRepository(
    private val db: PrestamoLabDatabase,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val generarRemoteId: () -> String = { UUID.randomUUID().toString() },
    private val alCambiarLocalmente: () -> Unit = {}
) : PrestamoRepository {

    private val equipmentDao = db.equipmentDao()
    private val loanDao = db.loanDao()
    private val returnDao = db.returnDao()

    override val equipos: Flow<List<Equipo>> =
        equipmentDao.observarTodos().map { lista -> lista.map(EquipmentEntity::aDominio) }
    override val solicitudes: Flow<List<SolicitudPrestamo>> =
        loanDao.observarTodos().map { lista -> lista.map(LoanEntity::aDominio) }
    override val devoluciones: Flow<List<Devolucion>> =
        returnDao.observarTodos().map { lista -> lista.map(ReturnEntity::aDominio) }

    override suspend fun obtenerEquipo(id: Int): Equipo? = equipmentDao.obtener(id)?.aDominio()

    override suspend fun obtenerDetalle(solicitudId: Int): PrestamoDetalle? =
        loanDao.obtenerConDetalle(solicitudId)?.aDominio()

    override suspend fun agregarUbicacion(solicitudId: Int, ubicacion: Ubicacion) {
        loanDao.guardarUbicacion(solicitudId, ubicacion.latitud, ubicacion.longitud, ubicacion.precisionMetros)
        alCambiarLocalmente()
    }

    override suspend fun crearSolicitud(nueva: NuevaSolicitud): Result<SolicitudPrestamo> = db.withTransaction {
        val equipo = equipmentDao.obtener(nueva.equipoId)
            ?: return@withTransaction Result.failure(NoSuchElementException("El equipo ${nueva.equipoId} no existe"))
        // Evitar solicitud sobre equipo no disponible (TC-12)
        if (equipo.status != EstadoEquipo.DISPONIBLE) {
            return@withTransaction Result.failure(IllegalStateException("El equipo no está disponible"))
        }

        val inicio = reloj()
        val prestamo = LoanEntity(
            remoteId = generarRemoteId(),
            equipmentId = nueva.equipoId,
            userId = nueva.usuarioId,
            requesterName = nueva.solicitante,
            environment = nueva.ambiente,
            purpose = nueva.proposito,
            durationHours = nueva.duracionHoras,
            requestDate = formatear(inicio),
            // loans.return_date es la fecha límite pactada, base de los recordatorios de HU-09
            returnDate = formatear(inicio + TimeUnit.HOURS.toMillis(nueva.duracionHoras.toLong())),
            status = EstadoSolicitud.SOLICITADA
        )
        val id = loanDao.insertar(prestamo).toInt()
        // Cambiamos a RESERVADO según TC-14
        equipmentDao.actualizarEstado(nueva.equipoId, EstadoEquipo.RESERVADO)
        Result.success(prestamo.copy(id = id).aDominio())
    }.also { if (it.isSuccess) alCambiarLocalmente() }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> = db.withTransaction {
        val prestamo = loanDao.obtener(id)
            ?: return@withTransaction Result.failure(NoSuchElementException("Solicitud no encontrada"))
        if (prestamo.status == EstadoSolicitud.CANCELADA) return@withTransaction Result.success(Unit)
        // Un equipo ya entregado no se libera cancelando: debe registrarse su devolución
        if (prestamo.status != EstadoSolicitud.SOLICITADA) {
            return@withTransaction Result.failure(
                IllegalStateException("Solo se pueden cancelar solicitudes en estado SOLICITADA")
            )
        }

        loanDao.actualizarEstado(id, EstadoSolicitud.CANCELADA)
        // Al cancelar, el equipo vuelve a estar disponible (TC-15)
        equipmentDao.actualizarEstado(prestamo.equipmentId, EstadoEquipo.DISPONIBLE)
        Result.success(Unit)
    }.also { if (it.isSuccess) alCambiarLocalmente() }

    override suspend fun registrarDevolucion(nueva: NuevaDevolucion): Result<Devolucion> = db.withTransaction {
        val prestamo = loanDao.obtener(nueva.solicitudId)
            ?: return@withTransaction Result.failure(NoSuchElementException("Préstamo no encontrado"))
        // CA-HU05-05: un préstamo ya devuelto (o no entregado) no se puede devolver
        if (prestamo.status != EstadoSolicitud.PRESTADO) {
            return@withTransaction Result.failure(
                IllegalStateException("Solo se pueden devolver préstamos en estado PRESTADO")
            )
        }
        // Otro dispositivo ya la registró y llegó por sincronización: el índice único la rechazaría
        if (returnDao.obtenerPorPrestamo(prestamo.id) != null) {
            return@withTransaction Result.failure(
                IllegalStateException("Este préstamo ya tiene una devolución registrada")
            )
        }

        val devolucion = ReturnEntity(
            remoteId = generarRemoteId(),
            loanId = prestamo.id,
            equipmentCondition = nueva.condicion,
            notes = nueva.observacion.trim(),
            returnDate = formatear(reloj()),
            latitude = nueva.ubicacion?.latitud,
            longitude = nueva.ubicacion?.longitud
        )
        val id = returnDao.insertar(devolucion).toInt()
        loanDao.actualizarEstado(prestamo.id, EstadoSolicitud.DEVUELTO)
        equipmentDao.actualizarEstado(prestamo.equipmentId, EstadoEquipo.DISPONIBLE)
        Result.success(devolucion.copy(id = id).aDominio())
    }.also { if (it.isSuccess) alCambiarLocalmente() }

    override suspend fun aprobarSolicitud(id: Int, instructorId: String): Result<Unit> =
        revisar(id, instructorId) { RevisionSolicitud.aprobar(it) }

    override suspend fun rechazarSolicitud(id: Int, instructorId: String, motivo: String): Result<Unit> =
        revisar(id, instructorId) { RevisionSolicitud.rechazar(it, motivo) }

    /** Aplica la transición de [RevisionSolicitud] al préstamo y a su equipo en una sola transacción. */
    private suspend fun revisar(
        id: Int,
        instructorId: String,
        transicion: (EstadoSolicitud) -> Result<ResultadoRevision>
    ): Result<Unit> = db.withTransaction {
        val prestamo = loanDao.obtener(id)
            ?: return@withTransaction Result.failure(NoSuchElementException("Solicitud no encontrada"))
        transicion(prestamo.status).map { revision ->
            loanDao.registrarRevision(id, revision.estadoSolicitud, instructorId, revision.motivoRechazo)
            equipmentDao.actualizarEstado(prestamo.equipmentId, revision.estadoEquipo)
        }
    }.also { if (it.isSuccess) alCambiarLocalmente() }

    override suspend fun registrarEquipo(nombre: String, categoria: String): Result<Equipo> {
        errorDeDatos(nombre, categoria)?.let { return Result.failure(it) }
        val equipo = EquipmentEntity(
            remoteId = generarRemoteId(),
            name = nombre.trim(),
            category = categoria.trim(),
            status = EstadoEquipo.DISPONIBLE,
            syncStatus = EstadoSincronizacion.PENDIENTE
        )
        val id = equipmentDao.insertar(equipo).toInt()
        alCambiarLocalmente()
        return Result.success(equipo.copy(id = id).aDominio())
    }

    override suspend fun editarEquipo(id: Int, nombre: String, categoria: String): Result<Unit> {
        errorDeDatos(nombre, categoria)?.let { return Result.failure(it) }
        return db.withTransaction {
            equipmentDao.obtener(id)
                ?: return@withTransaction Result.failure(NoSuchElementException("Equipo no encontrado"))
            equipmentDao.editar(id, nombre.trim(), categoria.trim())
            Result.success(Unit)
        }.also { if (it.isSuccess) alCambiarLocalmente() }
    }

    override suspend fun eliminarEquipo(id: Int): Result<Unit> = db.withTransaction {
        equipmentDao.obtener(id)
            ?: return@withTransaction Result.failure(NoSuchElementException("Equipo no encontrado"))
        ReglasInventario.validarEliminacion(loanDao.estadosPorEquipo(id)).map {
            equipmentDao.marcarEliminado(id)
        }
    }.also { if (it.isSuccess) alCambiarLocalmente() }

    private fun errorDeDatos(nombre: String, categoria: String): IllegalArgumentException? {
        val errores = ReglasInventario.validar(nombre, categoria)
        return if (errores.hayErrores) IllegalArgumentException(errores.nombre ?: errores.categoria) else null
    }

    private fun formatear(instante: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(instante))
}
