package com.example.prestamolab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolab.model.EstadoSolicitud

@Entity(tableName = "prestamos")
data class PrestamoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: EstadoSolicitud
)
