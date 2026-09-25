package com.example.prestamolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.LoanConDetalle
import com.example.prestamolab.data.local.entity.LoanEntity
import com.example.prestamolab.model.EstadoSolicitud
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY id")
    fun observarTodos(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun obtener(id: Int): LoanEntity?

    /** CA-HU06-05: préstamo, equipo y evidencias en una sola llamada (@Relation). */
    @Transaction
    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun obtenerConDetalle(id: Int): LoanConDetalle?

    /** CA-HU13-03: la ubicación llega después de crear la solicitud y queda pendiente de enviar. */
    @Query(
        "UPDATE loans SET latitude = :latitud, longitude = :longitud, location_accuracy = :precision, " +
            "sync_status = 'PENDIENTE' WHERE id = :id"
    )
    suspend fun guardarUbicacion(id: Int, latitud: Double, longitud: Double, precision: Float?)

    /** Estados de los préstamos de un equipo: deciden si se puede eliminar (CA-HU12-04). */
    @Query("SELECT status FROM loans WHERE equipment_id = :equipoId")
    suspend fun estadosPorEquipo(equipoId: Int): List<EstadoSolicitud>

    @Query("SELECT * FROM loans WHERE remote_id = :remoteId")
    suspend fun obtenerPorRemoteId(remoteId: String): LoanEntity?

    /** Devuelve el id asignado por SQLite. */
    @Insert
    suspend fun insertar(prestamo: LoanEntity): Long

    @Insert
    suspend fun insertar(prestamos: List<LoanEntity>)

    @Update
    suspend fun actualizar(prestamo: LoanEntity)

    /** Cambio hecho en el dispositivo: queda pendiente de enviar a Supabase. */
    @Query("UPDATE loans SET status = :estado, sync_status = 'PENDIENTE' WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: EstadoSolicitud)

    /** Revisión del instructor (HU-14): también queda pendiente de enviar. */
    @Query(
        "UPDATE loans SET status = :estado, reviewed_by = :revisor, rejection_reason = :motivo, " +
            "sync_status = 'PENDIENTE' WHERE id = :id"
    )
    suspend fun registrarRevision(id: Int, estado: EstadoSolicitud, revisor: String, motivo: String?)

    @Query("SELECT * FROM loans WHERE sync_status = 'PENDIENTE' ORDER BY id")
    suspend fun pendientes(): List<LoanEntity>

    /** Solo si no volvió a cambiar (estado ni ubicación) mientras se enviaba; si cambió, sigue PENDIENTE. */
    @Query(
        "UPDATE loans SET sync_status = :resultado WHERE id = :id AND status = :enviado " +
            "AND latitude IS :latitud AND longitude IS :longitud AND sync_status = 'PENDIENTE'"
    )
    suspend fun marcarEnviado(
        id: Int,
        enviado: EstadoSolicitud,
        latitud: Double?,
        longitud: Double?,
        resultado: EstadoSincronizacion
    )
}
