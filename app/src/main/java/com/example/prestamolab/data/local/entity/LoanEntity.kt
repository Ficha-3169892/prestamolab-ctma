package com.example.prestamolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

/** Copia local de `public.loans`. */
@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(entity = EquipmentEntity::class, parentColumns = ["id"], childColumns = ["equipment_id"])
    ],
    indices = [Index("equipment_id"), Index(value = ["remote_id"], unique = true), Index("user_id")]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** loans.id (uuid): se genera en el dispositivo, así reenviar el registro no lo duplica. */
    @ColumnInfo(name = "remote_id", defaultValue = "''") val remoteId: String,
    @ColumnInfo(name = "equipment_id") val equipmentId: Int,
    /** users.id del solicitante; null en préstamos creados antes de la versión 2 de la base. */
    @ColumnInfo(name = "user_id") val userId: String?,
    // Solo local: en Supabase el nombre se obtiene de users.full_name
    @ColumnInfo(name = "requester_name") val requesterName: String,
    val environment: String,
    val purpose: String,
    @ColumnInfo(name = "duration_hours") val durationHours: Int,
    @ColumnInfo(name = "request_date") val requestDate: String,
    /** Fecha límite pactada para devolver el equipo; la entrega real queda en returns.return_date. */
    @ColumnInfo(name = "return_date") val returnDate: String,
    val status: EstadoSolicitud,
    /** Revisión del instructor (HU-14): quién revisó (users.id) y por qué rechazó. */
    @ColumnInfo(name = "reviewed_by") val reviewedBy: String? = null,
    @ColumnInfo(name = "rejection_reason") val rejectionReason: String? = null,
    @ColumnInfo(name = "sync_status", defaultValue = "'PENDIENTE'")
    val syncStatus: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE
)

fun LoanEntity.aDominio() = SolicitudPrestamo(
    id = id,
    equipoId = equipmentId,
    usuarioId = userId,
    solicitante = requesterName,
    ambiente = environment,
    proposito = purpose,
    duracionHoras = durationHours,
    fechaInicio = requestDate,
    fechaFin = returnDate,
    estado = status,
    motivoRechazo = rejectionReason
)
