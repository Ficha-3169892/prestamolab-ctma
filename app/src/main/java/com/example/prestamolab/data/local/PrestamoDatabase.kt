package com.example.prestamolab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EquipmentEntity::class, LoanEntity::class], version = 1, exportSchema = false)
abstract class PrestamoDatabase : RoomDatabase() {

    abstract fun prestamoDao(): PrestamoDao

    companion object {
        @Volatile
        private var INSTANCE: PrestamoDatabase? = null

        fun getDatabase(context: Context): PrestamoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrestamoDatabase::class.java,
                    "prestamolab_database"
                )
                    // Permitimos consultas en el hilo principal SOLO para la semana 6.
                    // En la semana 7 (Fase 3) refactorizaremos esto con Corrutinas.
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
