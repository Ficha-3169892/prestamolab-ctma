package com.example.prestamolab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "competencias")
data class CompetenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)