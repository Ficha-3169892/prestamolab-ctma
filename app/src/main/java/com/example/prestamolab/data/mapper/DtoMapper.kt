package com.example.prestamolab.data.mapper

import com.example.prestamolab.data.remote.dto.EquipoDto
import com.example.prestamolab.data.remote.dto.SolicitudDto
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

fun EquipoDto.toDomain(): Equipo {
    return Equipo(
        id = id,
        nombre = nombre,
        categoria = CategoriaEquipo.valueOf(categoria),
        estado = EstadoEquipo.valueOf(estado)
    )
}

fun SolicitudDto.toDomain(): SolicitudPrestamo {
    return SolicitudPrestamo(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = EstadoSolicitud.valueOf(estado),
        fechaSolicitud = fechaSolicitud,
        latitud = latitud,
        longitud = longitud,
        fotoUri = fotoUri
    )
}

fun SolicitudPrestamo.toDto(): SolicitudDto {
    return SolicitudDto(
        id = id,
        equipoId = equipoId,
        ambienteDestino = ambienteDestino,
        proposito = proposito,
        duracionHoras = duracionHoras,
        estado = estado.name,
        fechaSolicitud = fechaSolicitud,
        latitud = latitud,
        longitud = longitud,
        fotoUri = fotoUri
    )
}
