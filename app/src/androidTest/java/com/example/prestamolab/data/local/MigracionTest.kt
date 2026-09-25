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

    @Test
    fun MigracionDeV2AV3AgregaLaRevisionSinPerderPrestamos() {
        helper.createDatabase(nombreBase, 2).apply {
            execSQL(
                "INSERT INTO equipments (id, remote_id, name, category, status, sync_status) " +
                    "VALUES (2, 'e2', 'Osciloscopio 100MHz', 'Laboratorio', 'RESERVADO', 'SINCRONIZADO')"
            )
            execSQL(
                "INSERT INTO loans (id, remote_id, equipment_id, user_id, requester_name, environment, purpose, " +
                    "duration_hours, request_date, return_date, status, sync_status) VALUES " +
                    "(1, 'l1', 2, 'u1', 'Andrés Vargas', 'Lab', 'Señales', 2, '2026-09-02 08:00', " +
                    "'2026-09-02 10:00', 'SOLICITADA', 'SINCRONIZADO')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreBase, 3, true, MIGRACION_2_3)

        db.query("SELECT status, sync_status, reviewed_by, rejection_reason FROM loans").use { c ->
            c.moveToNext()
            assertEquals("SOLICITADA", c.getString(0))
            assertEquals("SINCRONIZADO", c.getString(1))
            assertTrue(c.isNull(2))
            assertTrue(c.isNull(3))
        }
    }

    @Test
    fun MigracionDeV3AV4ConservaLosEquiposVisibles() {
        helper.createDatabase(nombreBase, 3).apply {
            execSQL(
                "INSERT INTO equipments (id, remote_id, name, category, status, sync_status) " +
                    "VALUES (1, 'e1', 'Multímetro Digital', 'Herramienta', 'DISPONIBLE', 'SINCRONIZADO')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreBase, 4, true, MIGRACION_3_4)

        db.query("SELECT name, deleted FROM equipments").use { c ->
            c.moveToNext()
            assertEquals("Multímetro Digital", c.getString(0))
            assertEquals(0, c.getInt(1))
        }
    }

    @Test
    fun MigracionDeV4AV5CreaLaTablaDeActividades() {
        helper.createDatabase(nombreBase, 4).apply {
            execSQL(
                "INSERT INTO equipments (id, remote_id, name, category, status, sync_status, deleted) " +
                    "VALUES (1, 'e1', 'Multímetro Digital', 'Herramienta', 'DISPONIBLE', 'SINCRONIZADO', 0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreBase, 5, true, MIGRACION_4_5)

        db.query("SELECT COUNT(*) FROM activities").use { c ->
            c.moveToNext()
            assertEquals(0, c.getInt(0))
        }
        db.query("SELECT name FROM equipments").use { c ->
            c.moveToNext()
            assertEquals("Multímetro Digital", c.getString(0))
        }
    }

    @Test
    fun MigracionDeV5AV6CreaLaTablaDeEvidencias() {
        helper.createDatabase(nombreBase, 5).apply {
            execSQL(
                "INSERT INTO equipments (id, remote_id, name, category, status, sync_status, deleted) " +
                    "VALUES (5, 'e5', 'Kit Arduino Uno', 'Herramienta', 'PRESTADO', 'SINCRONIZADO', 0)"
            )
            execSQL(
                "INSERT INTO loans (id, remote_id, equipment_id, user_id, requester_name, environment, purpose, " +
                    "duration_hours, request_date, return_date, status, sync_status) VALUES " +
                    "(2, 'l2', 5, 'u1', 'Andrés Vargas', 'Lab', 'IoT', 4, '2026-09-03 08:00', '2026-09-03 12:00', " +
                    "'PRESTADO', 'SINCRONIZADO')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreBase, 6, true, MIGRACION_5_6)

        db.execSQL(
            "INSERT INTO evidences (remote_id, loan_id, stage, local_uri, taken_at, sync_status) " +
                "VALUES ('ev1', 2, 'ENTREGA', 'content://x/1.jpg', '2026-09-25 10:00', 'PENDIENTE')"
        )
        db.query("SELECT loan_id, photo_url FROM evidences").use { c ->
            c.moveToNext()
            assertEquals(2, c.getInt(0))
            assertTrue(c.isNull(1))
        }
    }

    @Test
    fun MigracionDeV6AV7AgregaLaUbicacionDeLasEvidencias() {
        helper.createDatabase(nombreBase, 6).apply {
            execSQL(
                "INSERT INTO equipments (id, remote_id, name, category, status, sync_status, deleted) " +
                    "VALUES (5, 'e5', 'Kit Arduino Uno', 'Herramienta', 'PRESTADO', 'SINCRONIZADO', 0)"
            )
            execSQL(
                "INSERT INTO loans (id, remote_id, equipment_id, user_id, requester_name, environment, purpose, " +
                    "duration_hours, request_date, return_date, status, sync_status) VALUES " +
                    "(2, 'l2', 5, 'u1', 'Andrés Vargas', 'Lab', 'IoT', 4, '2026-09-03 08:00', '2026-09-03 12:00', " +
                    "'PRESTADO', 'SINCRONIZADO')"
            )
            execSQL(
                "INSERT INTO evidences (remote_id, loan_id, stage, local_uri, photo_url, taken_at, sync_status) " +
                    "VALUES ('ev1', 2, 'ENTREGA', 'content://x/1.jpg', 'https://x/1.jpg', '2026-09-25 10:00', 'SINCRONIZADO')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreBase, 7, true, MIGRACION_6_7)

        db.query("SELECT photo_url, sync_status, latitude, longitude FROM evidences").use { c ->
            c.moveToNext()
            assertEquals("https://x/1.jpg", c.getString(0))
            assertEquals("SINCRONIZADO", c.getString(1))
            assertTrue(c.isNull(2))
            assertTrue(c.isNull(3))
        }
    }

    @Test
    fun MigracionDeV7AV8AgregaLaUbicacionDeLaSolicitud() {
        helper.createDatabase(nombreBase, 7).apply {
            execSQL(
                "INSERT INTO equipments (id, remote_id, name, category, status, sync_status, deleted) " +
                    "VALUES (1, 'e1', 'Multímetro Digital', 'Herramienta', 'RESERVADO', 'SINCRONIZADO', 0)"
            )
            execSQL(
                "INSERT INTO loans (id, remote_id, equipment_id, user_id, requester_name, environment, purpose, " +
                    "duration_hours, request_date, return_date, status, sync_status) VALUES " +
                    "(1, 'l1', 1, 'u1', 'Andrés Vargas', 'Lab', 'Mediciones', 2, '2026-09-24 08:00', " +
                    "'2026-09-24 10:00', 'SOLICITADA', 'SINCRONIZADO')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreBase, 8, true, MIGRACION_7_8)

        db.query("SELECT status, latitude, longitude, location_accuracy FROM loans").use { c ->
            c.moveToNext()
            assertEquals("SOLICITADA", c.getString(0))
            assertTrue(c.isNull(1))
            assertTrue(c.isNull(2))
            assertTrue(c.isNull(3))
        }
    }
}
