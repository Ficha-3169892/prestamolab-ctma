package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.prestamolab.data.local.entity.ActivityEntity
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    // "yyyy-MM-dd HH:mm" ordena igual como texto que como fecha
    @Query("SELECT * FROM activities WHERE deleted = 0 ORDER BY scheduled_at, id")
    fun observarTodas(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id AND deleted = 0")
    suspend fun obtener(id: Int): ActivityEntity?

    /** Incluye las eliminadas pendientes: la recepción no debe volver a crearlas. */
    @Query("SELECT * FROM activities WHERE remote_id = :remoteId")
    suspend fun obtenerPorRemoteId(remoteId: String): ActivityEntity?

    @Insert
    suspend fun insertar(actividad: ActivityEntity): Long

    @Update
    suspend fun actualizar(actividad: ActivityEntity)

    @Query(
        "UPDATE activities SET title = :titulo, description = :descripcion, location = :ambiente, " +
            "scheduled_at = :fecha, sync_status = 'PENDIENTE' WHERE id = :id AND deleted = 0"
    )
    suspend fun editar(id: Int, titulo: String, descripcion: String, ambiente: String, fecha: String)

    @Query("UPDATE activities SET deleted = 1, sync_status = 'PENDIENTE' WHERE id = :id")
    suspend fun marcarEliminada(id: Int)

    /** Supabase no aceptó el DELETE: la actividad vuelve y la próxima recepción la actualiza. */
    @Query("UPDATE activities SET deleted = 0, sync_status = 'SINCRONIZADO' WHERE id = :id")
    suspend fun restaurar(id: Int)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun borrar(id: Int)

    @Query("SELECT * FROM activities WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<ActivityEntity>

    /** Recibidas de Supabase sin cambios locales: se borran si allá ya no existen. */
    @Query("SELECT * FROM activities WHERE deleted = 0 AND sync_status = 'SINCRONIZADO'")
    suspend fun sincronizadas(): List<ActivityEntity>

    /** Solo si no volvió a cambiar mientras se enviaba; si cambió, sigue PENDIENTE. */
    @Query(
        "UPDATE activities SET sync_status = :resultado WHERE id = :id AND title = :titulo " +
            "AND description = :descripcion AND location = :ambiente AND scheduled_at = :fecha " +
            "AND deleted = 0 AND sync_status = 'PENDIENTE'"
    )
    suspend fun marcarEnviada(
        id: Int,
        titulo: String,
        descripcion: String,
        ambiente: String,
        fecha: String,
        resultado: EstadoSincronizacion
    )
}
