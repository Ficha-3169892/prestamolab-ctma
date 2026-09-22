package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.prestamolab.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {
    @Query("SELECT * FROM solicitudes ORDER BY fechaSolicitud DESC")
    fun getAllSolicitudes(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE estado IN ('SOLICITADA', 'APROBADA', 'ENTREGADA')")
    fun getActiveSolicitudes(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun getSolicitudById(id: Int): SolicitudEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudEntity): Long

    @Update
    suspend fun updateSolicitud(solicitud: SolicitudEntity)

    @Query("DELETE FROM solicitudes WHERE id = :id")
    suspend fun deleteSolicitudById(id: Int)

    @Query("DELETE FROM solicitudes WHERE estado IN ('DEVUELTA', 'CANCELADA', 'RECHAZADA')")
    suspend fun deleteHistorialCompletado()
}
