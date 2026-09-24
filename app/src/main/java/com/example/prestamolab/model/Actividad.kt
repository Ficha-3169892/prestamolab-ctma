package com.example.prestamolab.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/** Actividad formativa que programa el instructor (HU-11). */
data class Actividad(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val ambiente: String,
    /** "yyyy-MM-dd HH:mm", hora del dispositivo, igual que las fechas de los préstamos. */
    val fecha: String,
    /** users.id del instructor que la creó. */
    val instructorId: String
)

/** Datos que captura el formulario; el repositorio asigna id e instructor. */
data class DatosActividad(
    val titulo: String,
    val descripcion: String,
    val ambiente: String,
    val fecha: String
)

/** Errores por campo; null significa que el campo es válido. */
data class ErroresActividad(
    val titulo: String? = null,
    val descripcion: String? = null,
    val ambiente: String? = null,
    val fecha: String? = null
) {
    val hayErrores: Boolean get() = listOfNotNull(titulo, descripcion, ambiente, fecha).isNotEmpty()
    val primero: String? get() = titulo ?: descripcion ?: ambiente ?: fecha
}

object ReglasActividad {
    const val FORMATO_FECHA = "yyyy-MM-dd HH:mm"
    const val TITULO_MAXIMO = 100
    const val DESCRIPCION_MAXIMA = 500
    const val AMBIENTE_MAXIMO = 80

    /** CA-HU11-02: título obligatorio y fecha válida que no esté en el pasado ([ahora] en milisegundos). */
    fun validar(datos: DatosActividad, ahora: Long) = ErroresActividad(
        titulo = when {
            datos.titulo.isBlank() -> "El título es obligatorio."
            datos.titulo.trim().length > TITULO_MAXIMO -> "Máximo $TITULO_MAXIMO caracteres"
            else -> null
        },
        descripcion = if (datos.descripcion.trim().length > DESCRIPCION_MAXIMA) "Máximo $DESCRIPCION_MAXIMA caracteres" else null,
        ambiente = when {
            datos.ambiente.isBlank() -> "El ambiente es obligatorio."
            datos.ambiente.trim().length > AMBIENTE_MAXIMO -> "Máximo $AMBIENTE_MAXIMO caracteres"
            else -> null
        },
        fecha = validarFecha(datos.fecha.trim(), ahora)
    )

    private fun validarFecha(fecha: String, ahora: Long): String? {
        if (fecha.isEmpty()) return "La fecha es obligatoria."
        val instante = aInstante(fecha) ?: return "Usa el formato AAAA-MM-DD HH:MM"
        return if (instante < ahora) "La fecha no puede estar en el pasado." else null
    }

    /** null si el texto no es una fecha real en [FORMATO_FECHA] (p. ej. 2026-02-30). */
    fun aInstante(fecha: String): Long? {
        if (!Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}""").matches(fecha)) return null
        return try {
            SimpleDateFormat(FORMATO_FECHA, Locale.US).apply { isLenient = false }.parse(fecha)?.time
        } catch (e: ParseException) {
            null
        }
    }
}
