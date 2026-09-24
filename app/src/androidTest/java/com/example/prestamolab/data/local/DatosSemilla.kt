package com.example.prestamolab.data.local

import com.example.prestamolab.data.local.entity.ActivityEntity
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.testutil.FakeUsuariosDataSource

/**
 * Datos de las pruebas instrumentadas. Los equipos usan los uuid de CatalogoInicial y los
 * préstamos pertenecen al estudiante de FakeUsuariosDataSource.
 */
object DatosSemilla {
    private val ESTUDIANTE = FakeUsuariosDataSource.ESTUDIANTE.usuario.id
    private val INSTRUCTOR = FakeUsuariosDataSource.INSTRUCTOR.usuario.id

    val equipos = listOf(
        equipo(1, "Multímetro Digital", "Herramienta", EstadoEquipo.DISPONIBLE),
        // Reservado por la solicitud semilla #1
        equipo(2, "Osciloscopio 100MHz", "Laboratorio", EstadoEquipo.RESERVADO),
        equipo(3, "Cautín Estación de Soldadura", "Herramienta", EstadoEquipo.DISPONIBLE),
        equipo(4, "Fuente de Poder DC", "Laboratorio", EstadoEquipo.DISPONIBLE),
        // Entregado por el préstamo semilla #2 (permite probar la devolución)
        equipo(5, "Kit Arduino Uno", "Herramienta", EstadoEquipo.PRESTADO)
    )

    val prestamos = listOf(
        LoanEntity(
            1, "5eed0000-0000-4000-8000-000000000001", 2, ESTUDIANTE, "Andrés Vargas", "Laboratorio 302",
            "Práctica de señales", 2, "2026-09-02 08:00", "2026-09-02 10:00", EstadoSolicitud.SOLICITADA,
            syncStatus = EstadoSincronizacion.SINCRONIZADO
        ),
        LoanEntity(
            2, "5eed0000-0000-4000-8000-000000000002", 5, ESTUDIANTE, "Andrés Vargas", "Ambiente de Electrónica",
            "Prototipo de sensores IoT", 4, "2026-09-03 08:00", "2026-09-03 12:00", EstadoSolicitud.PRESTADO,
            syncStatus = EstadoSincronizacion.SINCRONIZADO
        )
    )

    /** Fecha lejana: la actividad semilla nunca queda en el pasado al editarla. */
    val actividades = listOf(
        ActivityEntity(
            1, "ac710000-0000-4000-8000-000000000001", "Práctica de osciloscopio", "Medición de señales",
            "Laboratorio 302", "2030-01-15 08:00", INSTRUCTOR, EstadoSincronizacion.SINCRONIZADO
        )
    )

    private fun equipo(id: Int, nombre: String, categoria: String, estado: EstadoEquipo) = EquipmentEntity(
        id, CatalogoInicial.remoteIdPorIdLocal.getValue(id), nombre, categoria, estado, EstadoSincronizacion.SINCRONIZADO
    )

    /** Borra todo y vuelve a cargar la semilla, reiniciando los ids autoincrementales. */
    fun reiniciar(db: PrestamoLabDatabase) {
        // runInTransaction avisa al InvalidationTracker, así los Flow de los DAO vuelven a emitir
        db.runInTransaction {
            val sql = db.openHelper.writableDatabase
            sql.execSQL("DELETE FROM evidences")
            sql.execSQL("DELETE FROM activities")
            sql.execSQL("DELETE FROM returns")
            sql.execSQL("DELETE FROM loans")
            sql.execSQL("DELETE FROM equipments")
            sql.execSQL("DELETE FROM sqlite_sequence")
            equipos.forEach {
                sql.execSQL(
                    "INSERT INTO equipments (id, remote_id, name, category, status, sync_status) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf(it.id, it.remoteId, it.name, it.category, it.status.name, it.syncStatus.name)
                )
            }
            actividades.forEach {
                sql.execSQL(
                    "INSERT INTO activities (id, remote_id, title, description, location, scheduled_at, instructor_id, " +
                        "sync_status, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                    arrayOf(
                        it.id, it.remoteId, it.title, it.description, it.location, it.scheduledAt, it.instructorId,
                        it.syncStatus.name
                    )
                )
            }
            prestamos.forEach {
                sql.execSQL(
                    "INSERT INTO loans (id, remote_id, equipment_id, user_id, requester_name, environment, purpose, " +
                        "duration_hours, request_date, return_date, status, sync_status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        it.id, it.remoteId, it.equipmentId, it.userId, it.requesterName, it.environment, it.purpose,
                        it.durationHours, it.requestDate, it.returnDate, it.status.name, it.syncStatus.name
                    )
                )
            }
        }
    }
}
