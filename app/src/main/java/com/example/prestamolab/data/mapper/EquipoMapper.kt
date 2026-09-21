package com.example.prestamolab.data.mapper

import com.example.prestamolab.data.local.entity.EquipoEntity
import com.example.prestamolab.model.Equipo

fun EquipoEntity.toDomain(): Equipo {
    return Equipo(
        id = id,
        nombre = nombre,
        categoria = categoria,
        estado = estado
    )
}

fun Equipo.toEntity(): EquipoEntity {
    return EquipoEntity(
        id = id,
        nombre = nombre,
        categoria = categoria,
        estado = estado
    )
}
