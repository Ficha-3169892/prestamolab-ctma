package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.prestamolab.data.local.entity.ReturnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnDao {
    @Query("SELECT * FROM returns ORDER BY id")
    fun observarTodos(): Flow<List<ReturnEntity>>

    /** Devuelve el id asignado por SQLite. */
    @Insert
    suspend fun insertar(devolucion: ReturnEntity): Long
}
