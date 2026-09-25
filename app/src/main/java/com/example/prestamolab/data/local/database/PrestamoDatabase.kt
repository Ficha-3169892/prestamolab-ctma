package com.example.prestamolab.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.prestamolab.data.local.dao.EquipoDao
import com.example.prestamolab.data.local.dao.SolicitudDao
import com.example.prestamolab.data.local.entity.EquipoEntity
import com.example.prestamolab.data.local.entity.SolicitudEntity

@Database(
    entities = [EquipoEntity::class, SolicitudEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PrestamoDatabase : RoomDatabase() {
    abstract fun equipoDao(): EquipoDao
    abstract fun solicitudDao(): SolicitudDao

    companion object {
        @Volatile
        private var INSTANCE: PrestamoDatabase? = null

        fun getDatabase(context: Context): PrestamoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrestamoDatabase::class.java,
                    "prestamo_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
