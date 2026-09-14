package com.example.prestamolab.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actividades",
    foreignKeys = [
        ForeignKey(
            entity = CompetenciaEntity::class,
            parentColumns = ["id"],
            childColumns = ["competenciaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["competenciaId"])]
)
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val fechaLimite: String,
    val prioridad: Int,
    val competenciaId: Long,
    val completada: Boolean = false
)