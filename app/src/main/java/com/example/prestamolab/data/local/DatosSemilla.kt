package com.example.prestamolab.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud

/** Datos de demostración hasta que el catálogo se sincronice desde Supabase (HU-07). */
object DatosSemilla {
    private val equipos = listOf(
        EquipmentEntity(1, "Multímetro Digital", "Herramienta", EstadoEquipo.DISPONIBLE),
        // Reservado por la solicitud semilla #1
        EquipmentEntity(2, "Osciloscopio 100MHz", "Laboratorio", EstadoEquipo.RESERVADO),
        EquipmentEntity(3, "Cautín Estación de Soldadura", "Herramienta", EstadoEquipo.DISPONIBLE),
        EquipmentEntity(4, "Fuente de Poder DC", "Laboratorio", EstadoEquipo.DISPONIBLE),
        // Entregado por el préstamo semilla #2 (permite probar la devolución antes de HU-14)
        EquipmentEntity(5, "Kit Arduino Uno", "Herramienta", EstadoEquipo.PRESTADO)
    )

    private val prestamos = listOf(
        LoanEntity(
            1, 2, "Andrés Vargas", "Laboratorio 302", "Práctica de señales",
            2, "2026-09-02", "2026-09-05", EstadoSolicitud.SOLICITADA
        ),
        LoanEntity(
            2, 5, "Andrés Vargas", "Ambiente de Electrónica", "Prototipo de sensores IoT",
            4, "2026-09-03", "2026-09-03", EstadoSolicitud.PRESTADO
        )
    )

    /** Inserta con SQL directo porque en onCreate los DAO todavía no están disponibles. */
    fun insertar(db: SupportSQLiteDatabase) {
        equipos.forEach {
            db.execSQL(
                "INSERT INTO equipments (id, name, category, status) VALUES (?, ?, ?, ?)",
                arrayOf(it.id, it.name, it.category, it.status.name)
            )
        }
        prestamos.forEach {
            db.execSQL(
                "INSERT INTO loans (id, equipment_id, requester_name, environment, purpose, duration_hours, " +
                    "request_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    it.id, it.equipmentId, it.requesterName, it.environment, it.purpose,
                    it.durationHours, it.requestDate, it.returnDate, it.status.name
                )
            )
        }
    }
}
