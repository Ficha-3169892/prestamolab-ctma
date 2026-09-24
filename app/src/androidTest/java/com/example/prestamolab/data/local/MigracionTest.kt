package com.example.prestamolab.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** La migración 1 → 2 conserva los datos y agrega las columnas de sincronización (CA-HU06-04). */
@RunWith(AndroidJUnit4::class)
class MigracionTest {

    private val nombreBase = "migracion-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PrestamoLabDatabase::class.java
    )

    @Test
    fun TC_HU06_04_MigracionDeV1AV2ConservaLosDatos() {
        // Base v1 con los datos que sembraba la app antes de sincronizar con Supabase
        helper.createDatabase(nombreBase, 1).apply {
            execSQL("INSERT INTO equipments (id, name, category, status) VALUES (1, 'Multímetro Digital', 'Herramienta', 'DISPONIBLE')")
            execSQL("INSERT INTO equipments (id, name, category, status) VALUES (5, 'Kit Arduino Uno', 'Herramienta', 'PRESTADO')")
            execSQL("INSERT INTO equipments (id, name, category, status) VALUES (9, 'Equipo creado localmente', 'Otro', 'DISPONIBLE')")
            execSQL(
                "INSERT INTO loans (id, equipment_id, requester_name, environment, purpose, duration_hours, " +
                    "request_date, return_date, status) VALUES " +
                    "(1, 5, 'Andrés Vargas', 'Lab', 'Prototipo IoT', 4, '2026-09-03', '2026-09-03', 'DEVUELTO'), " +
                    "(2, 1, 'Andrés Vargas', 'Lab', 'Mediciones', 1, '2026-09-04', '2026-09-04', 'SOLICITADA')"
            )
            execSQL(
                "INSERT INTO returns (id, loan_id, equipment_condition, notes, return_date, latitude, longitude) " +
                    "VALUES (1, 1, 'BUENO', 'Sin novedad', '2026-09-03 12:00', 6.25, -75.56)"
            )
            close()
        }

        // Valida que el esquema resultante coincide exactamente con el exportado de la v2
        val db = helper.runMigrationsAndValidate(nombreBase, 2, true, MIGRACION_1_2)

        db.query("SELECT id, remote_id, name, sync_status FROM equipments ORDER BY id").use { c ->
            c.moveToNext()
            assertEquals(CatalogoInicial.remoteIdPorIdLocal[1], c.getString(1))
            assertEquals("Multímetro Digital", c.getString(2))
            assertEquals("SINCRONIZADO", c.getString(3))
            c.moveToNext()
            assertEquals(CatalogoInicial.remoteIdPorIdLocal[5], c.getString(1))
            c.moveToNext()
            // Un equipo fuera del catálogo inicial recibe un uuid nuevo
            assertEquals(36, c.getString(1).length)
        }
        db.query("SELECT remote_id, user_id, sync_status, purpose FROM loans ORDER BY id").use { c ->
            val remoteIds = mutableSetOf<String>()
            while (c.moveToNext()) {
                remoteIds += c.getString(0)
                assertTrue(c.isNull(1))
                assertEquals("PENDIENTE", c.getString(2))
            }
            assertEquals("uuid únicos por fila", 2, remoteIds.size)
        }
        db.query("SELECT notes, latitude, sync_status FROM returns").use { c ->
            c.moveToNext()
            assertEquals("Sin novedad", c.getString(0))
            assertEquals(6.25, c.getDouble(1), 0.0)
            assertEquals("PENDIENTE", c.getString(2))
        }
    }
}
