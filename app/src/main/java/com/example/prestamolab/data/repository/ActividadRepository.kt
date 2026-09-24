package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.ActivityEntity
import com.example.prestamolab.data.local.entity.aDominio
import com.example.prestamolab.model.Actividad
import com.example.prestamolab.model.DatosActividad
import com.example.prestamolab.model.ReglasActividad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/** Actividades formativas (HU-11): todos las consultan; solo el instructor las mantiene. */
interface ActividadRepository {
    val actividades: Flow<List<Actividad>>
    suspend fun obtener(id: Int): Actividad?

    /** Falla si los datos no cumplen [ReglasActividad] (título vacío, fecha pasada...). */
    suspend fun crear(datos: DatosActividad, instructorId: String): Result<Actividad>
    suspend fun editar(id: Int, datos: DatosActividad): Result<Unit>
    suspend fun eliminar(id: Int): Result<Unit>
}

class RoomActividadRepository(
    db: PrestamoLabDatabase,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val generarRemoteId: () -> String = { UUID.randomUUID().toString() },
    private val alCambiarLocalmente: () -> Unit = {}
) : ActividadRepository {

    private val dao = db.activityDao()

    override val actividades: Flow<List<Actividad>> =
        dao.observarTodas().map { lista -> lista.map(ActivityEntity::aDominio) }

    override suspend fun obtener(id: Int): Actividad? = dao.obtener(id)?.aDominio()

    override suspend fun crear(datos: DatosActividad, instructorId: String): Result<Actividad> {
        errorDeDatos(datos)?.let { return Result.failure(it) }
        val actividad = ActivityEntity(
            remoteId = generarRemoteId(),
            title = datos.titulo.trim(),
            description = datos.descripcion.trim(),
            location = datos.ambiente.trim(),
            scheduledAt = datos.fecha.trim(),
            instructorId = instructorId
        )
        val id = dao.insertar(actividad).toInt()
        alCambiarLocalmente()
        return Result.success(actividad.copy(id = id).aDominio())
    }

    override suspend fun editar(id: Int, datos: DatosActividad): Result<Unit> {
        errorDeDatos(datos)?.let { return Result.failure(it) }
        dao.obtener(id) ?: return Result.failure(NoSuchElementException("Actividad no encontrada"))
        dao.editar(id, datos.titulo.trim(), datos.descripcion.trim(), datos.ambiente.trim(), datos.fecha.trim())
        alCambiarLocalmente()
        return Result.success(Unit)
    }

    override suspend fun eliminar(id: Int): Result<Unit> {
        dao.obtener(id) ?: return Result.failure(NoSuchElementException("Actividad no encontrada"))
        dao.marcarEliminada(id)
        alCambiarLocalmente()
        return Result.success(Unit)
    }

    private fun errorDeDatos(datos: DatosActividad): IllegalArgumentException? =
        ReglasActividad.validar(datos, reloj()).primero?.let(::IllegalArgumentException)
}
