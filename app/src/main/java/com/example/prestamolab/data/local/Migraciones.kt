package com.example.prestamolab.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

/**
 * uuid en Supabase de los 5 equipos que la versión 1 sembraba con ids 1 a 5.
 * docs/supabase/004_sincronizacion.sql inserta los mismos, así el catálogo no se duplica.
 */
object CatalogoInicial {
    val remoteIdPorIdLocal = mapOf(
        1 to "0b1e0000-0000-4000-8000-000000000001",
        2 to "0b1e0000-0000-4000-8000-000000000002",
        3 to "0b1e0000-0000-4000-8000-000000000003",
        4 to "0b1e0000-0000-4000-8000-000000000004",
        5 to "0b1e0000-0000-4000-8000-000000000005"
    )
}

/** v1 → v2: columnas de sincronización (remote_id, sync_status) y dueño del préstamo (user_id). */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE equipments ADD COLUMN remote_id TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE equipments ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'SINCRONIZADO'")
        db.execSQL("ALTER TABLE loans ADD COLUMN remote_id TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE loans ADD COLUMN user_id TEXT")
        db.execSQL("ALTER TABLE loans ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDIENTE'")
        db.execSQL("ALTER TABLE returns ADD COLUMN remote_id TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE returns ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDIENTE'")

        // Cada fila necesita su propio uuid antes de crear los índices únicos
        asignarRemoteIds(db, "equipments") { id -> CatalogoInicial.remoteIdPorIdLocal[id] }
        asignarRemoteIds(db, "loans") { null }
        asignarRemoteIds(db, "returns") { null }

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_equipments_remote_id ON equipments (remote_id)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_loans_remote_id ON loans (remote_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_loans_user_id ON loans (user_id)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_returns_remote_id ON returns (remote_id)")
    }

    private fun asignarRemoteIds(db: SupportSQLiteDatabase, tabla: String, fijo: (Int) -> String?) {
        val ids = db.query("SELECT id FROM $tabla").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getInt(0)) }
        }
        ids.forEach { id ->
            val remoteId = fijo(id) ?: UUID.randomUUID().toString()
            db.execSQL("UPDATE $tabla SET remote_id = ? WHERE id = ?", arrayOf(remoteId, id))
        }
    }
}

/** v2 → v3: revisión del instructor (HU-14), mismas columnas que public.loans. */
val MIGRACION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE loans ADD COLUMN reviewed_by TEXT")
        db.execSQL("ALTER TABLE loans ADD COLUMN rejection_reason TEXT")
    }
}

/** v3 → v4: marca local de equipos eliminados por el instructor (HU-12) hasta enviar el DELETE. */
val MIGRACION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE equipments ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
    }
}

/** v4 → v5: actividades formativas (HU-11), copia local de public.activities. */
val MIGRACION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `activities` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`remote_id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, " +
                "`location` TEXT NOT NULL, `scheduled_at` TEXT NOT NULL, `instructor_id` TEXT NOT NULL, " +
                "`sync_status` TEXT NOT NULL, `deleted` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_activities_remote_id` ON `activities` (`remote_id`)")
    }
}

/** v5 → v6: evidencias fotográficas de los préstamos (HU-08), copia local de public.evidences. */
val MIGRACION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `evidences` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`remote_id` TEXT NOT NULL, `loan_id` INTEGER NOT NULL, `stage` TEXT NOT NULL, " +
                "`local_uri` TEXT NOT NULL, `photo_url` TEXT, `taken_at` TEXT NOT NULL, `sync_status` TEXT NOT NULL, " +
                "FOREIGN KEY(`loan_id`) REFERENCES `loans`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidences_loan_id` ON `evidences` (`loan_id`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_evidences_remote_id` ON `evidences` (`remote_id`)")
    }
}
