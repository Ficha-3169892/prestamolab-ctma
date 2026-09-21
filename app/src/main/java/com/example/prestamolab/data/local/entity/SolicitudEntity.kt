package com.example.prestamolab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolab.model.EstadoSolicitud

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: EstadoSolicitud,
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
    val estadoSincronizacion: String = "LOCAL" // LOCAL, SUBIENDO, SINCRONIZADA, FALLIDA
)
