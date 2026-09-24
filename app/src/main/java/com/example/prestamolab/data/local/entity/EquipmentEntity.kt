package com.example.prestamolab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo

/** Copia local de `public.equipments`; las columnas usan los mismos nombres que Supabase. */
@Entity(tableName = "equipments")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val status: EstadoEquipo
)

fun EquipmentEntity.aDominio() = Equipo(id, name, category, status)

fun Equipo.aEntidad() = EquipmentEntity(id, nombre, categoria, estado)
