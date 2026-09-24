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
    // Los equipos eliminados pendientes de enviar ya no existen para la app
    @Query("SELECT * FROM equipments WHERE deleted = 0 ORDER BY id")
    fun observarTodos(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipments WHERE id = :id AND deleted = 0")
    suspend fun obtener(id: Int): EquipmentEntity?

    /** Equipos recibidos de Supabase sin cambios locales: candidatos a borrarse si allá ya no existen. */
    @Query("SELECT * FROM equipments WHERE deleted = 0 AND sync_status = 'SINCRONIZADO'")
    suspend fun sincronizados(): List<EquipmentEntity>

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

    /** HU-12: edición del instructor; queda pendiente de enviar. */
    @Query("UPDATE equipments SET name = :nombre, category = :categoria, sync_status = 'PENDIENTE' WHERE id = :id AND deleted = 0")
    suspend fun editar(id: Int, nombre: String, categoria: String)

    @Query("UPDATE equipments SET deleted = 1, sync_status = 'PENDIENTE' WHERE id = :id")
    suspend fun marcarEliminado(id: Int)

    /** Supabase no aceptó el DELETE: el equipo vuelve y la próxima recepción lo actualiza. */
    @Query("UPDATE equipments SET deleted = 0, sync_status = 'SINCRONIZADO' WHERE id = :id")
    suspend fun restaurar(id: Int)

    @Query("DELETE FROM equipments WHERE id = :id")
    suspend fun borrar(id: Int)

    /** Incluye los eliminados, que también deben enviarse. */
    @Query("SELECT * FROM equipments WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<EquipmentEntity>

    /** Solo si no volvió a cambiar mientras se enviaba; si cambió, sigue PENDIENTE. */
    @Query(
        "UPDATE equipments SET sync_status = :resultado WHERE id = :id AND status = :estado AND name = :nombre " +
            "AND category = :categoria AND deleted = 0 AND sync_status = 'PENDIENTE'"
    )
    suspend fun marcarEnviado(id: Int, estado: EstadoEquipo, nombre: String, categoria: String, resultado: EstadoSincronizacion)
}
