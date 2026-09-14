package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.ActividadDao
import com.example.prestamolab.data.local.ActividadEntity
import kotlinx.coroutines.flow.Flow

class RoomActividadRepository(
    private val dao: ActividadDao
) {
    val actividades: Flow<List<ActividadEntity>> = dao.obtenerTodas()

    suspend fun guardar(actividad: ActividadEntity): Long {
        return if (actividad.id == 0L) {
            dao.insertar(actividad)
        } else {
            dao.actualizar(actividad)
            actividad.id
        }
    }

    suspend fun obtenerPorId(id: Long): ActividadEntity? = dao.obtenerPorId(id)

    suspend fun eliminar(actividad: ActividadEntity) {
        dao.eliminar(actividad)
    }
}