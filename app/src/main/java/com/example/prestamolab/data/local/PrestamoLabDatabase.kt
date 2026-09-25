package com.example.prestamolab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prestamolab.data.local.dao.ActivityDao
import com.example.prestamolab.data.local.dao.EquipmentDao
import com.example.prestamolab.data.local.dao.EvidenceDao
import com.example.prestamolab.data.local.dao.LoanDao
import com.example.prestamolab.data.local.dao.ReturnDao
import com.example.prestamolab.data.local.entity.ActivityEntity
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.EvidenceEntity
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.data.local.entity.ReturnEntity

@Database(
    entities = [EquipmentEntity::class, LoanEntity::class, ReturnEntity::class, ActivityEntity::class, EvidenceEntity::class],
    version = 8,
    exportSchema = true
)
abstract class PrestamoLabDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun loanDao(): LoanDao
    abstract fun returnDao(): ReturnDao
    abstract fun activityDao(): ActivityDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        const val NOMBRE = "prestamolab.db"

        /** La base nace vacía: el catálogo y los préstamos llegan de Supabase al sincronizar (HU-07). */
        /** [nombre] distinto solo en pruebas que necesitan un archivo propio (reinicio sin conexión). */
        fun construir(context: Context, enMemoria: Boolean = false, nombre: String = NOMBRE): PrestamoLabDatabase {
            val builder = if (enMemoria) {
                Room.inMemoryDatabaseBuilder(context, PrestamoLabDatabase::class.java)
            } else {
                Room.databaseBuilder(context, PrestamoLabDatabase::class.java, nombre)
            }
            return builder.addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5, MIGRACION_5_6, MIGRACION_6_7, MIGRACION_7_8).build()
        }
    }
}
