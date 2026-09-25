package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidences WHERE loan_id = :prestamoId ORDER BY id")
    fun observarPorPrestamo(prestamoId: Int): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidences WHERE id = :id")
    suspend fun obtener(id: Int): EvidenceEntity?

    @Insert
    suspend fun insertar(evidencia: EvidenceEntity): Long

    @Query("SELECT * FROM evidences WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<EvidenceEntity>

    /** La foto ya está en Storage: si luego falla el registro en la tabla, no se vuelve a subir. */
    @Query("UPDATE evidences SET photo_url = :url WHERE id = :id")
    suspend fun guardarUrl(id: Int, url: String)

    /** La ubicación llega después de la foto: la evidencia vuelve a quedar pendiente de enviar. */
    @Query("UPDATE evidences SET latitude = :latitud, longitude = :longitud, sync_status = 'PENDIENTE' WHERE id = :id")
    suspend fun guardarUbicacion(id: Int, latitud: Double, longitud: Double)

    /** Solo si la ubicación no cambió mientras se enviaba; si cambió, sigue PENDIENTE. */
    @Query(
        "UPDATE evidences SET sync_status = :resultado WHERE id = :id AND sync_status = 'PENDIENTE' " +
            "AND latitude IS :latitud AND longitude IS :longitud"
    )
    suspend fun marcarEnviada(id: Int, latitud: Double?, longitud: Double?, resultado: EstadoSincronizacion)
}
