package com.example.prestamolab.data.local

import androidx.room.TypeConverter
import com.example.prestamolab.model.EstadoSolicitud

class Converters {
    @TypeConverter
    fun fromEstadoSolicitud(value: EstadoSolicitud): String = value.name

    @TypeConverter
    fun toEstadoSolicitud(value: String): EstadoSolicitud = enumValueOf<EstadoSolicitud>(value)
}
