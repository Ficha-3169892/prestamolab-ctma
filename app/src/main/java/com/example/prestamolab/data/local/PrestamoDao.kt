package com.example.prestamolab.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestamoDao {

    @Query("SELECT * FROM prestamos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<PrestamoEntity>>

    @Query("SELECT * FROM prestamos WHERE ambienteDestino LIKE '%' || :query || '%' OR proposito LIKE '%' || :query || '%'")
    fun buscarPorAmbienteOProposito(query: String): Flow<List<PrestamoEntity>>

    @Query("SELECT * FROM prestamos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): PrestamoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(prestamo: PrestamoEntity)

    @Update
    suspend fun actualizar(prestamo: PrestamoEntity)

    @Delete
    suspend fun eliminar(prestamo: PrestamoEntity)
}
