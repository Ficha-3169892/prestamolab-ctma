package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.prestamolab.data.local.entity.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos")
    fun getAllEquipos(): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun getEquipoById(id: Int): EquipoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipos(equipos: List<EquipoEntity>)

    @Query("DELETE FROM equipos")
    suspend fun deleteAllEquipos()

    @Query("UPDATE equipos SET estado = :nuevoEstado WHERE id = :id")
    suspend fun updateEstado(id: Int, nuevoEstado: com.example.prestamolab.model.EstadoEquipo)
}
