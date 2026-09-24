package com.example.prestamolab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.prestamolab.data.local.dao.EquipmentDao
import com.example.prestamolab.data.local.dao.LoanDao
import com.example.prestamolab.data.local.dao.ReturnDao
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.data.local.entity.ReturnEntity

@Database(
    entities = [EquipmentEntity::class, LoanEntity::class, ReturnEntity::class],
    version = 1,
    exportSchema = true
)
abstract class PrestamoLabDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun loanDao(): LoanDao
    abstract fun returnDao(): ReturnDao

    /** Borra todo y vuelve a cargar la semilla; lo usan las pruebas instrumentadas para aislar cada caso. */
    fun reiniciar() {
        // runInTransaction avisa al InvalidationTracker, así los Flow de los DAO vuelven a emitir
        runInTransaction {
            val db = openHelper.writableDatabase
            db.execSQL("DELETE FROM returns")
            db.execSQL("DELETE FROM loans")
            db.execSQL("DELETE FROM equipments")
            // Reinicia los contadores AUTOINCREMENT para que los ids nuevos sean predecibles
            db.execSQL("DELETE FROM sqlite_sequence")
            DatosSemilla.insertar(db)
        }
    }

    companion object {
        const val NOMBRE = "prestamolab.db"

        fun construir(context: Context, enMemoria: Boolean = false): PrestamoLabDatabase {
            val builder = if (enMemoria) {
                Room.inMemoryDatabaseBuilder(context, PrestamoLabDatabase::class.java)
            } else {
                Room.databaseBuilder(context, PrestamoLabDatabase::class.java, NOMBRE)
            }
            return builder
                .addCallback(object : Callback() {
                    // Solo la primera vez que se crea el archivo; después se conservan los datos del usuario
                    override fun onCreate(db: SupportSQLiteDatabase) = DatosSemilla.insertar(db)
                })
                .build()
        }
    }
}
