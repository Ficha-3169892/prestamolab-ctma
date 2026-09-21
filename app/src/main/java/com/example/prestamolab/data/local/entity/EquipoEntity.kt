package com.example.prestamolab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo

@Entity(tableName = "equipos")
data class EquipoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val categoria: CategoriaEquipo,
    val estado: EstadoEquipo
)
