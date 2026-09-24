package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.model.EstadoSolicitud
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY id")
    fun observarTodos(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun obtener(id: Int): LoanEntity?

    @Query("SELECT * FROM loans WHERE remote_id = :remoteId")
    suspend fun obtenerPorRemoteId(remoteId: String): LoanEntity?

    /** Devuelve el id asignado por SQLite. */
    @Insert
    suspend fun insertar(prestamo: LoanEntity): Long

    @Insert
    suspend fun insertar(prestamos: List<LoanEntity>)

    @Update
    suspend fun actualizar(prestamo: LoanEntity)

    /** Cambio hecho en el dispositivo: queda pendiente de enviar a Supabase. */
    @Query("UPDATE loans SET status = :estado, sync_status = 'PENDIENTE' WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: EstadoSolicitud)

    @Query("SELECT * FROM loans WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<LoanEntity>

    /** Solo si no volvió a cambiar mientras se enviaba; si cambió, sigue PENDIENTE. */
    @Query("UPDATE loans SET sync_status = :resultado WHERE id = :id AND status = :enviado AND sync_status = 'PENDIENTE'")
    suspend fun marcarEnviado(id: Int, enviado: EstadoSolicitud, resultado: EstadoSincronizacion)
}
