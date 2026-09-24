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

    @Query("UPDATE evidences SET sync_status = :resultado WHERE id = :id AND sync_status = 'PENDIENTE'")
    suspend fun marcarEnviada(id: Int, resultado: EstadoSincronizacion)
}
