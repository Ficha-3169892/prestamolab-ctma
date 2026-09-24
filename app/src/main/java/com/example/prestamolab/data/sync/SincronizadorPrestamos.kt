package com.example.prestamolab.data.sync

import androidx.room.withTransaction
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.data.local.entity.ReturnEntity
import com.example.prestamolab.data.remote.DevolucionRemota
import com.example.prestamolab.data.remote.EquipoRemoto
import com.example.prestamolab.data.remote.FechasSupabase
import com.example.prestamolab.data.remote.PrestamoRemoto
import com.example.prestamolab.data.remote.PrestamosRemoteDataSource
import com.example.prestamolab.data.remote.SupabaseHttpException
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.CancellationException
import java.io.IOException

sealed interface ResultadoSincronizacion {
    data class Exito(val enviados: Int, val recibidos: Int) : ResultadoSincronizacion
    /** 401: la credencial ya no es válida; hay que volver al login. */
    data object NoAutorizado : ResultadoSincronizacion
    /** 404: la tabla o recurso no existe en Supabase; se conservan los datos locales. */
    data class RecursoNoEncontrado(val detalle: String) : ResultadoSincronizacion
    /** Otro 4xx sobre la consulta completa: reintentar no lo arreglaría. */
    data class Rechazado(val codigo: Int, val detalle: String) : ResultadoSincronizacion
    /** 5xx, 408, 429, sin red o tiempo agotado: se reintenta con espera exponencial. */
    data class ErrorTemporal(val causa: Exception) : ResultadoSincronizacion
}

interface Sincronizador {
    suspend fun sincronizar(usuario: Usuario): ResultadoSincronizacion
}

/**
 * Sincroniza Room con Supabase: primero envía lo PENDIENTE y luego recibe lo remoto.
 * Los envíos son upsert por uuid, así que repetir una sincronización interrumpida es seguro.
 */
class SincronizadorPrestamos(
    private val db: PrestamoLabDatabase,
    private val remoto: PrestamosRemoteDataSource,
    private val fechas: FechasSupabase = FechasSupabase()
) : Sincronizador {

    private val equipmentDao = db.equipmentDao()
    private val loanDao = db.loanDao()
    private val returnDao = db.returnDao()

    override suspend fun sincronizar(usuario: Usuario): ResultadoSincronizacion = try {
        val enviados = enviarPendientes()
        val recibidos = recibir(usuario)
        ResultadoSincronizacion.Exito(enviados, recibidos)
    } catch (e: CancellationException) {
        throw e
    } catch (e: SupabaseHttpException) {
        when {
            e.codigo == 401 -> ResultadoSincronizacion.NoAutorizado
            e.codigo == 404 -> ResultadoSincronizacion.RecursoNoEncontrado(e.cuerpo)
            esTemporal(e.codigo) -> ResultadoSincronizacion.ErrorTemporal(e)
            else -> ResultadoSincronizacion.Rechazado(e.codigo, e.cuerpo)
        }
    } catch (e: IOException) {
        ResultadoSincronizacion.ErrorTemporal(e)
    }

    // ── Envío ────────────────────────────────────────────────────────────────

    /** En orden de dependencia: un préstamo necesita su equipo y una devolución su préstamo. */
    private suspend fun enviarPendientes(): Int {
        var enviados = 0
        for (equipo in equipmentDao.pendientes()) {
            val resultado = enviar { remoto.guardarEquipo(equipo.aRemoto()) }
            equipmentDao.marcarEnviado(equipo.id, equipo.status, resultado)
            if (resultado == EstadoSincronizacion.SINCRONIZADO) enviados++
        }
        for (prestamo in loanDao.pendientes()) {
            val remotoPrestamo = prestamo.aRemoto()
            // Préstamos de la versión 1 sin dueño: Supabase exige user_id
            val resultado = if (remotoPrestamo == null) {
                EstadoSincronizacion.ERROR
            } else {
                enviar { remoto.guardarPrestamo(remotoPrestamo) }
            }
            loanDao.marcarEnviado(prestamo.id, prestamo.status, resultado)
            if (resultado == EstadoSincronizacion.SINCRONIZADO) enviados++
        }
        for (devolucion in returnDao.pendientes()) {
            val prestamo = loanDao.obtener(devolucion.loanId) ?: continue
            // Si su préstamo aún no está en Supabase, se intenta en la próxima sincronización
            if (prestamo.syncStatus != EstadoSincronizacion.SINCRONIZADO) continue
            val resultado = enviar { remoto.guardarDevolucion(devolucion.aRemoto(prestamo.remoteId)) }
            returnDao.marcarEnviado(devolucion.id, resultado)
            if (resultado == EstadoSincronizacion.SINCRONIZADO) enviados++
        }
        return enviados
    }

    /**
     * Un 4xx propio del registro (datos inválidos, conflicto, permiso) lo marca ERROR y sigue con
     * los demás; 401, 404 y los errores temporales detienen la sincronización completa.
     */
    private suspend fun enviar(accion: suspend () -> Unit): EstadoSincronizacion = try {
        accion()
        EstadoSincronizacion.SINCRONIZADO
    } catch (e: SupabaseHttpException) {
        if (e.codigo == 401 || e.codigo == 404 || esTemporal(e.codigo)) throw e
        EstadoSincronizacion.ERROR
    }

    private fun EquipmentEntity.aRemoto() = EquipoRemoto(remoteId, name, category, status.name)

    private suspend fun LoanEntity.aRemoto(): PrestamoRemoto? {
        val dueno = userId ?: return null
        val equipo = equipmentDao.obtener(equipmentId) ?: return null
        return PrestamoRemoto(
            id = remoteId,
            usuarioId = dueno,
            equipoId = equipo.remoteId,
            estado = status.name,
            fechaSolicitud = fechas.aIso(requestDate),
            fechaLimite = fechas.aIso(returnDate),
            ambiente = environment,
            proposito = purpose,
            duracionHoras = durationHours
        )
    }

    private fun ReturnEntity.aRemoto(prestamoRemoteId: String) = DevolucionRemota(
        id = remoteId,
        prestamoId = prestamoRemoteId,
        condicion = equipmentCondition.name,
        notas = notes,
        fechaDevolucion = fechas.aIso(returnDate),
        latitud = latitude,
        longitud = longitude
    )

    // ── Recepción ────────────────────────────────────────────────────────────

    private suspend fun recibir(usuario: Usuario): Int {
        // El instructor revisa todos los préstamos; el estudiante solo recibe los suyos
        val filtro = if (usuario.rol == Rol.INSTRUCTOR) null else usuario.id
        // Todo se descarga antes de escribir, para no dejar Room a medias si la red falla
        val equipos = remoto.equipos()
        val prestamos = remoto.prestamos(filtro)
        val devoluciones = remoto.devoluciones(filtro)

        return db.withTransaction {
            equipos.sumOf { guardarEquipo(it) } +
                prestamos.sumOf { guardarPrestamo(it) } +
                devoluciones.sumOf { guardarDevolucion(it) }
        }
    }

    /** Devuelve 1 si cambió Room. Un registro local PENDIENTE no se pisa: su cambio aún no se envió. */
    private suspend fun guardarEquipo(r: EquipoRemoto): Int {
        val estado = EstadoEquipo.entries.find { it.name == r.estado } ?: return 0
        val local = equipmentDao.obtenerPorRemoteId(r.id)
        val entidad = EquipmentEntity(
            id = local?.id ?: 0, remoteId = r.id, name = r.nombre, category = r.categoria,
            status = estado, syncStatus = EstadoSincronizacion.SINCRONIZADO
        )
        return when {
            local == null -> { equipmentDao.insertar(entidad); 1 }
            local.syncStatus == EstadoSincronizacion.PENDIENTE || local == entidad -> 0
            else -> { equipmentDao.actualizar(entidad); 1 }
        }
    }

    private suspend fun guardarPrestamo(r: PrestamoRemoto): Int {
        val estado = EstadoSolicitud.entries.find { it.name == r.estado } ?: return 0
        val equipo = equipmentDao.obtenerPorRemoteId(r.equipoId) ?: return 0
        val local = loanDao.obtenerPorRemoteId(r.id)
        val entidad = LoanEntity(
            id = local?.id ?: 0,
            remoteId = r.id,
            equipmentId = equipo.id,
            userId = r.usuarioId,
            requesterName = r.nombreSolicitante ?: local?.requesterName.orEmpty(),
            environment = r.ambiente,
            purpose = r.proposito,
            durationHours = r.duracionHoras,
            requestDate = fechas.desdeIso(r.fechaSolicitud),
            returnDate = fechas.desdeIso(r.fechaLimite),
            status = estado,
            syncStatus = EstadoSincronizacion.SINCRONIZADO
        )
        return when {
            local == null -> { loanDao.insertar(entidad); 1 }
            local.syncStatus == EstadoSincronizacion.PENDIENTE || local == entidad -> 0
            else -> { loanDao.actualizar(entidad); 1 }
        }
    }

    private suspend fun guardarDevolucion(r: DevolucionRemota): Int {
        val condicion = CondicionEquipo.entries.find { it.name == r.condicion } ?: return 0
        val prestamo = loanDao.obtenerPorRemoteId(r.prestamoId) ?: return 0
        val local = returnDao.obtenerPorRemoteId(r.id)
        // Otro dispositivo registró una devolución distinta del mismo préstamo: gana la local
        if (local == null && returnDao.obtenerPorPrestamo(prestamo.id) != null) return 0
        val entidad = ReturnEntity(
            id = local?.id ?: 0,
            remoteId = r.id,
            loanId = prestamo.id,
            equipmentCondition = condicion,
            notes = r.notas,
            returnDate = fechas.desdeIso(r.fechaDevolucion),
            latitude = r.latitud,
            longitude = r.longitud,
            syncStatus = EstadoSincronizacion.SINCRONIZADO
        )
        return when {
            local == null -> { returnDao.insertar(entidad); 1 }
            local.syncStatus == EstadoSincronizacion.PENDIENTE || local == entidad -> 0
            else -> { returnDao.actualizar(entidad); 1 }
        }
    }

    private fun esTemporal(codigo: Int) = codigo >= 500 || codigo == 408 || codigo == 429
}
