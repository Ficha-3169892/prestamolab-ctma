package com.example.prestamolab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [EquipoEntity::class, SolicitudEntity::class], version = 1, exportSchema = false)
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
                    .addCallback(PrestamoDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PrestamoDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.equipoDao())
                    }
                }
            }

            suspend fun populateDatabase(equipoDao: EquipoDao) {
                val equiposIniciales = listOf(
                    EquipoEntity(1, "Multímetro Digital Fluke", "HERRAMIENTA", "DISPONIBLE"),
                    EquipoEntity(2, "Osciloscopio Digital 100MHz", "ELECTRONICA", "DISPONIBLE"),
                    EquipoEntity(3, "Kit de Soldadura Cautín", "HERRAMIENTA", "DISPONIBLE"),
                    EquipoEntity(4, "Laptop Core i7 Laboratorio", "COMPUTO", "DISPONIBLE"),
                    EquipoEntity(5, "Fuente de Poder DC Regulada", "ELECTRONICA", "DISPONIBLE")
                )
                equipoDao.insertEquipos(equiposIniciales)
            }
        }
    }
}
