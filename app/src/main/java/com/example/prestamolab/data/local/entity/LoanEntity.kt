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
    indices = [Index("equipment_id")]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "equipment_id") val equipmentId: Int,
    // Solo local: en Supabase el solicitante se obtiene de loans.user_id
    @ColumnInfo(name = "requester_name") val requesterName: String,
    val environment: String,
    val purpose: String,
    @ColumnInfo(name = "duration_hours") val durationHours: Int,
    @ColumnInfo(name = "request_date") val requestDate: String,
    /** Fecha límite pactada para devolver el equipo; la entrega real queda en returns.return_date. */
    @ColumnInfo(name = "return_date") val returnDate: String,
    val status: EstadoSolicitud
)

fun LoanEntity.aDominio() = SolicitudPrestamo(
    id = id,
    equipoId = equipmentId,
    solicitante = requesterName,
    ambiente = environment,
    proposito = purpose,
    duracionHoras = durationHours,
    fechaInicio = requestDate,
    fechaFin = returnDate,
    estado = status
)

fun SolicitudPrestamo.aEntidad() = LoanEntity(
    id = id,
    equipmentId = equipoId,
    requesterName = solicitante,
    environment = ambiente,
    purpose = proposito,
    durationHours = duracionHoras,
    requestDate = fechaInicio,
    returnDate = fechaFin,
    status = estado
)
