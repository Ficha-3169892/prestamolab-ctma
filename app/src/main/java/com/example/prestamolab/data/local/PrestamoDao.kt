package com.example.prestamolab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PrestamoDao {
    @Query("SELECT * FROM prestamos ORDER BY id DESC")
    suspend fun obtenerTodos(): List<PrestamoEntity>

    @Query("SELECT * FROM prestamos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): PrestamoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(prestamo: PrestamoEntity): Long

    @Query("UPDATE prestamos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String): Int
}