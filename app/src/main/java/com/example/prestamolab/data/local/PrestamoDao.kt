package com.example.prestamolab.data.local

import androidx.room.Dao
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

    // Escrituras asíncronas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipos(equipos: List < EquipmentEntity >)

    @Update
    suspend fun actualizarEquipo(equipo: EquipmentEntity)

    @Insert
    suspend fun insertarSolicitud(solicitud: LoanEntity): Long

    @Update
    suspend fun actualizarSolicitud(solicitud: LoanEntity)

    // Lecturas directas de una sola vez (necesarias para el repositorio)
    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun obtenerEquipoSync(id: Int): EquipmentEntity?

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerSolicitudSync(id: Int): LoanEntity?
}
