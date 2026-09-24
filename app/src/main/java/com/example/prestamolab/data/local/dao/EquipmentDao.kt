package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.EquipmentEntity
import com.example.prestamolab.model.EstadoEquipo
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipments ORDER BY id")
    fun observarTodos(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipments WHERE id = :id")
    suspend fun obtener(id: Int): EquipmentEntity?

    @Query("SELECT * FROM equipments WHERE remote_id = :remoteId")
    suspend fun obtenerPorRemoteId(remoteId: String): EquipmentEntity?

    @Insert
    suspend fun insertar(equipo: EquipmentEntity): Long

    @Insert
    suspend fun insertar(equipos: List<EquipmentEntity>)

    @Update
    suspend fun actualizar(equipo: EquipmentEntity)

    /** Cambio hecho en el dispositivo: queda pendiente de enviar a Supabase. */
    @Query("UPDATE equipments SET status = :estado, sync_status = 'PENDIENTE' WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: EstadoEquipo)

    @Query("SELECT * FROM equipments WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<EquipmentEntity>

    /** Solo si no volvió a cambiar mientras se enviaba; si cambió, sigue PENDIENTE. */
    @Query("UPDATE equipments SET sync_status = :resultado WHERE id = :id AND status = :enviado AND sync_status = 'PENDIENTE'")
    suspend fun marcarEnviado(id: Int, enviado: EstadoEquipo, resultado: EstadoSincronizacion)
}
