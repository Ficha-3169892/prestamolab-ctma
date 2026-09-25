package com.example.prestamolab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PrestamoDao {

    // Operaciones para Equipos
    @Query("SELECT * FROM equipos")
    fun obtenerTodosLosEquipos(): List < EquipmentEntity >

    @Query("SELECT * FROM equipos WHERE id = :id")
    fun obtenerEquipoPorId(id: Int): EquipmentEntity?

    @Insert
    fun insertarEquipos(equipos: List < EquipmentEntity >)

    @Update
    fun actualizarEquipo(equipo: EquipmentEntity)

    // Operaciones para Solicitudes
    @Query("SELECT * FROM solicitudes")
    fun obtenerTodasLasSolicitudes(): List < LoanEntity >

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun obtenerSolicitudPorId(id: Int): LoanEntity?

    @Insert
    fun insertarSolicitud(solicitud: LoanEntity): Long

    @Update
    fun actualizarSolicitud(solicitud: LoanEntity)
}
