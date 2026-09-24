package com.example.prestamolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.prestamolab.model.Actividad

/** Copia local de `public.activities` (HU-11); las columnas usan los mismos nombres que Supabase. */
@Entity(tableName = "activities", indices = [Index(value = ["remote_id"], unique = true)])
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** activities.id (uuid): se genera en el dispositivo, así reenviar el registro no lo duplica. */
    @ColumnInfo(name = "remote_id") val remoteId: String,
    val title: String,
    val description: String,
    /** activities.location: el ambiente donde se realiza. */
    val location: String,
    /** "yyyy-MM-dd HH:mm" en hora del dispositivo; en Supabase es timestamptz. */
    @ColumnInfo(name = "scheduled_at") val scheduledAt: String,
    @ColumnInfo(name = "instructor_id") val instructorId: String,
    @ColumnInfo(name = "sync_status") val syncStatus: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE,
    /** Solo local: eliminada por el instructor; se borra al confirmar el DELETE en Supabase. */
    val deleted: Boolean = false
)

fun ActivityEntity.aDominio() = Actividad(id, title, description, location, scheduledAt, instructorId)
