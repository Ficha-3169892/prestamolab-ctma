package com.example.prestamolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.prestamolab.model.EstadoEvidencia
import com.example.prestamolab.model.EtapaEvidencia
import com.example.prestamolab.model.Evidencia

/** Copia local de `public.evidences` (HU-08) más la URI de la foto en el teléfono. */
@Entity(
    tableName = "evidences",
    foreignKeys = [ForeignKey(entity = LoanEntity::class, parentColumns = ["id"], childColumns = ["loan_id"])],
    indices = [Index("loan_id"), Index(value = ["remote_id"], unique = true)]
)
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** evidences.id (uuid): se genera en el dispositivo, así reenviar el registro no lo duplica. */
    @ColumnInfo(name = "remote_id") val remoteId: String,
    @ColumnInfo(name = "loan_id") val loanId: Int,
    val stage: EtapaEvidencia,
    /** Solo local: content:// del FileProvider (CA-HU08-02). */
    @ColumnInfo(name = "local_uri") val localUri: String,
    /** URL pública en Storage; null hasta que la foto se sube. */
    @ColumnInfo(name = "photo_url") val photoUrl: String? = null,
    /** "yyyy-MM-dd HH:mm" en hora del dispositivo; en Supabase es timestamptz. */
    @ColumnInfo(name = "taken_at") val takenAt: String,
    @ColumnInfo(name = "sync_status") val syncStatus: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE,
    /** GPS al tomar la foto; mismas columnas que evidences en Supabase. */
    val latitude: Double? = null,
    val longitude: Double? = null
)

fun EvidenceEntity.aDominio() = Evidencia(
    id, loanId, stage, localUri, photoUrl, takenAt, latitude, longitude,
    estado = when (syncStatus) {
        EstadoSincronizacion.PENDIENTE -> EstadoEvidencia.LOCAL
        EstadoSincronizacion.SINCRONIZADO -> EstadoEvidencia.SINCRONIZADA
        EstadoSincronizacion.ERROR -> EstadoEvidencia.FALLIDA
    }
)
