package com.example.prestamolab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SolicitudDao {
    @Query("SELECT * FROM solicitudes")
    suspend fun getAllSolicitudes(): List<SolicitudEntity>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun getSolicitudById(id: Int): SolicitudEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudEntity)

    @Query("UPDATE solicitudes SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Int, estado: String)
}
