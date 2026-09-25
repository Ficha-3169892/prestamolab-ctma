package com.example.prestamolab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey val id: Int,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: String
)

fun SolicitudEntity.toDomain(): SolicitudPrestamo {
    return SolicitudPrestamo(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = try { EstadoSolicitud.valueOf(estado) } catch (e: Exception) { EstadoSolicitud.SOLICITADA }
    )
}

fun SolicitudPrestamo.toEntity(): SolicitudEntity {
    return SolicitudEntity(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = estado.name
    )
}
