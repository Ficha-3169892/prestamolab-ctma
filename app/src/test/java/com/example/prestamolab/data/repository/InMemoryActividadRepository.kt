package com.example.prestamolab.data.repository

import com.example.prestamolab.model.Actividad
import com.example.prestamolab.model.DatosActividad
import com.example.prestamolab.model.ReglasActividad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Doble de pruebas unitarias con las mismas reglas que [RoomActividadRepository], sin SQLite. */
class InMemoryActividadRepository(private val reloj: () -> Long) : ActividadRepository {

    private val _actividades = MutableStateFlow(
        listOf(
            Actividad(1, "Práctica de osciloscopio", "Medición de señales", "Laboratorio 302", "2026-10-01 08:00", "u-instructor")
        )
    )
    override val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    override suspend fun obtener(id: Int): Actividad? = _actividades.value.find { it.id == id }

    override suspend fun crear(datos: DatosActividad, instructorId: String): Result<Actividad> {
        errorDeDatos(datos)?.let { return Result.failure(it) }
        val actividad = Actividad(
            id = (_actividades.value.maxOfOrNull { it.id } ?: 0) + 1,
            titulo = datos.titulo.trim(),
            descripcion = datos.descripcion.trim(),
            ambiente = datos.ambiente.trim(),
            fecha = datos.fecha.trim(),
            instructorId = instructorId
        )
        _actividades.update { it + actividad }
        return Result.success(actividad)
    }

    override suspend fun editar(id: Int, datos: DatosActividad): Result<Unit> {
        errorDeDatos(datos)?.let { return Result.failure(it) }
        if (_actividades.value.none { it.id == id }) return Result.failure(NoSuchElementException("Actividad no encontrada"))
        _actividades.update { lista ->
            lista.map {
                if (it.id == id) it.copy(
                    titulo = datos.titulo.trim(), descripcion = datos.descripcion.trim(),
                    ambiente = datos.ambiente.trim(), fecha = datos.fecha.trim()
                ) else it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun eliminar(id: Int): Result<Unit> {
        if (_actividades.value.none { it.id == id }) return Result.failure(NoSuchElementException("Actividad no encontrada"))
        _actividades.update { lista -> lista.filterNot { it.id == id } }
        return Result.success(Unit)
    }

    private fun errorDeDatos(datos: DatosActividad): IllegalArgumentException? =
        ReglasActividad.validar(datos, reloj()).primero?.let(::IllegalArgumentException)
}
