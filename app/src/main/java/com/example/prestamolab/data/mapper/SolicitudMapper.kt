package com.example.prestamolab.data.mapper

import com.example.prestamolab.data.local.entity.SolicitudEntity
import com.example.prestamolab.model.SolicitudPrestamo

fun SolicitudEntity.toDomain(): SolicitudPrestamo {
    return SolicitudPrestamo(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = estado,
        fechaSolicitud = fechaSolicitud,
        latitud = latitud,
        longitud = longitud,
        fotoUri = fotoUri,
        estadoSincronizacion = estadoSincronizacion
    )
}

fun SolicitudPrestamo.toEntity(): SolicitudEntity {
    return SolicitudEntity(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = estado,
        fechaSolicitud = fechaSolicitud,
        latitud = latitud,
        longitud = longitud,
        fotoUri = fotoUri,
        estadoSincronizacion = estadoSincronizacion
    )
}
