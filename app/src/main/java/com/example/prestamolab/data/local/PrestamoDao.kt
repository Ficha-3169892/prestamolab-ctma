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

    // Lecturas reactivas
    @Query("SELECT * FROM equipos")
    fun obtenerTodosLosEquipos(): Flow < List < EquipmentEntity > >

    @Query("SELECT * FROM equipos WHERE id = :id")
    fun obtenerEquipoPorId(id: Int): Flow < EquipmentEntity? >

    @Query("SELECT * FROM solicitudes")
    fun obtenerTodasLasSolicitudes(): Flow < List < LoanEntity > >

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun obtenerSolicitudPorId(id: Int): Flow < LoanEntity? >

    @Query("UPDATE solicitudes SET usuarioId = :newUserId WHERE usuarioId = 'local_user' OR usuarioId = ''")
    suspend fun asignarSolicitudesHuerfanas(newUserId: String)

    @Query("DELETE FROM solicitudes")
    suspend fun limpiarSolicitudesTest()

    // Escrituras asíncronas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipos(equipos: List < EquipmentEntity >)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: EquipmentEntity): Long

    @Update
    suspend fun actualizarEquipo(equipo: EquipmentEntity)

    @Delete
    suspend fun eliminarEquipo(equipo: EquipmentEntity)

    @Query("DELETE FROM equipos WHERE id = :id")
    suspend fun eliminarEquipoPorId(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitud(solicitud: LoanEntity): Long

    @Update
    suspend fun actualizarSolicitud(solicitud: LoanEntity)

    // Lecturas directas de una sola vez
    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun obtenerEquipoSync(id: Int): EquipmentEntity?

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerSolicitudSync(id: Int): LoanEntity?
}
