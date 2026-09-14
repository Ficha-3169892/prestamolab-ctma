package com.example.prestamolab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ActividadEntity::class, CompetenciaEntity::class, PrestamoEntity::class],
    version = 3,
    exportSchema = true
)
abstract class FormacionDatabase : RoomDatabase() {
    abstract fun actividadDao(): ActividadDao
    abstract fun prestamoDao(): PrestamoDao
}