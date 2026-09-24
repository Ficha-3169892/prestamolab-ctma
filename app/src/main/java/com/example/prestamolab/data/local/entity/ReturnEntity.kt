package com.example.prestamolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.Devolucion

/** Copia local de `public.returns`, con la ubicación GPS capturada al devolver (HU-13). */
@Entity(
    tableName = "returns",
    foreignKeys = [
        ForeignKey(entity = LoanEntity::class, parentColumns = ["id"], childColumns = ["loan_id"])
    ],
    // Un préstamo se devuelve una sola vez (CA-HU05-05)
    indices = [Index(value = ["loan_id"], unique = true), Index(value = ["remote_id"], unique = true)]
)
data class ReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** returns.id (uuid) generado en el dispositivo. */
    @ColumnInfo(name = "remote_id", defaultValue = "''") val remoteId: String,
    @ColumnInfo(name = "loan_id") val loanId: Int,
    @ColumnInfo(name = "equipment_condition") val equipmentCondition: CondicionEquipo,
    val notes: String,
    /** Fecha y hora reales de la entrega. */
    @ColumnInfo(name = "return_date") val returnDate: String,
    // Null cuando no se concedió el permiso o no se pudo obtener la ubicación
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "sync_status", defaultValue = "'PENDIENTE'")
    val syncStatus: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE
)

fun ReturnEntity.aDominio() = Devolucion(
    id = id,
    solicitudId = loanId,
    condicion = equipmentCondition,
    observacion = notes,
    fechaDevolucion = returnDate,
    latitud = latitude,
    longitud = longitude
)
