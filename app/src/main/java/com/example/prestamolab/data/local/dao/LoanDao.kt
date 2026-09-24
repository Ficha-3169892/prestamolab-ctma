package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.model.EstadoSolicitud
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY id")
    fun observarTodos(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun obtener(id: Int): LoanEntity?

    /** Devuelve el id asignado por SQLite. */
    @Insert
    suspend fun insertar(prestamo: LoanEntity): Long

    @Insert
    suspend fun insertar(prestamos: List<LoanEntity>)

    @Query("UPDATE loans SET status = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: EstadoSolicitud)
}
