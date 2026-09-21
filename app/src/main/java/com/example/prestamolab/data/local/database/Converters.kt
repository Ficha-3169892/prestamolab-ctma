package com.example.prestamolab.data.local.database

import androidx.room.TypeConverter
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud

class Converters {
    @TypeConverter
    fun fromCategoria(value: CategoriaEquipo): String = value.name

    @TypeConverter
    fun toCategoria(value: String): CategoriaEquipo = CategoriaEquipo.valueOf(value)

    @TypeConverter
    fun fromEstadoEquipo(value: EstadoEquipo): String = value.name

    @TypeConverter
    fun toEstadoEquipo(value: String): EstadoEquipo = EstadoEquipo.valueOf(value)

    @TypeConverter
    fun fromEstadoSolicitud(value: EstadoSolicitud): String = value.name

    @TypeConverter
    fun toEstadoSolicitud(value: String): EstadoSolicitud = EstadoSolicitud.valueOf(value)
}
