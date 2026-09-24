package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.model.EstadoEquipo
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipments ORDER BY id")
    fun observarTodos(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipments WHERE id = :id")
    suspend fun obtener(id: Int): EquipmentEntity?

    @Insert
    suspend fun insertar(equipos: List<EquipmentEntity>)

    @Query("UPDATE equipments SET status = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: EstadoEquipo)
}
