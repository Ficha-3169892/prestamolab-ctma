package com.example.prestamolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo

/** Copia local de `public.equipments`; las columnas usan los mismos nombres que Supabase. */
@Entity(tableName = "equipments", indices = [Index(value = ["remote_id"], unique = true)])
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** equipments.id (uuid) en Supabase. */
    @ColumnInfo(name = "remote_id", defaultValue = "''") val remoteId: String,
    val name: String,
    val category: String,
    val status: EstadoEquipo,
    // El catálogo llega de Supabase ya sincronizado; solo cambia al reservar, cancelar o devolver
    @ColumnInfo(name = "sync_status", defaultValue = "'SINCRONIZADO'")
    val syncStatus: EstadoSincronizacion = EstadoSincronizacion.SINCRONIZADO
)

fun EquipmentEntity.aDominio() = Equipo(id, name, category, status)
