package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.ReturnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnDao {
    @Query("SELECT * FROM returns ORDER BY id")
    fun observarTodos(): Flow<List<ReturnEntity>>

    @Query("SELECT * FROM returns WHERE remote_id = :remoteId")
    suspend fun obtenerPorRemoteId(remoteId: String): ReturnEntity?

    @Query("SELECT * FROM returns WHERE loan_id = :loanId")
    suspend fun obtenerPorPrestamo(loanId: Int): ReturnEntity?

    /** Devuelve el id asignado por SQLite. */
    @Insert
    suspend fun insertar(devolucion: ReturnEntity): Long

    @Update
    suspend fun actualizar(devolucion: ReturnEntity)

    @Query("SELECT * FROM returns WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<ReturnEntity>

    // Una devolución no cambia después de registrarse, así que no hace falta comparar campos
    @Query("UPDATE returns SET sync_status = :resultado WHERE id = :id AND sync_status = 'PENDIENTE'")
    suspend fun marcarEnviado(id: Int, resultado: EstadoSincronizacion)
}
