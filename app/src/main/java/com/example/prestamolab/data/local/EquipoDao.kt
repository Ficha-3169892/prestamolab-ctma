package com.example.prestamolab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos")
    suspend fun getAllEquipos(): List<EquipoEntity>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun getEquipoById(id: Int): EquipoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipos(equipos: List<EquipoEntity>)

    @Query("UPDATE equipos SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Int, estado: String)
}
