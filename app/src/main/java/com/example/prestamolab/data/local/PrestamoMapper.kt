package com.example.prestamolab.data.local

import com.example.prestamolab.model.SolicitudPrestamo

fun PrestamoEntity.toDomain(): SolicitudPrestamo {
    return SolicitudPrestamo(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = estado
    )
}

fun SolicitudPrestamo.toEntity(): PrestamoEntity {
    return PrestamoEntity(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = estado
    )
}
