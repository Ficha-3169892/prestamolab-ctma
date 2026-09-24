package com.example.prestamolab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prestamolab.data.local.dao.EquipmentDao
import com.example.prestamolab.data.local.dao.LoanDao
import com.example.prestamolab.data.local.dao.ReturnDao
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.data.local.entity.ReturnEntity

@Database(
    entities = [EquipmentEntity::class, LoanEntity::class, ReturnEntity::class],
    version = 3,
    exportSchema = true
)
abstract class PrestamoLabDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun loanDao(): LoanDao
    abstract fun returnDao(): ReturnDao

    companion object {
        const val NOMBRE = "prestamolab.db"

        /** La base nace vacía: el catálogo y los préstamos llegan de Supabase al sincronizar (HU-07). */
        fun construir(context: Context, enMemoria: Boolean = false): PrestamoLabDatabase {
            val builder = if (enMemoria) {
                Room.inMemoryDatabaseBuilder(context, PrestamoLabDatabase::class.java)
            } else {
                Room.databaseBuilder(context, PrestamoLabDatabase::class.java, NOMBRE)
            }
            return builder.addMigrations(MIGRACION_1_2, MIGRACION_2_3).build()
        }
    }
}
