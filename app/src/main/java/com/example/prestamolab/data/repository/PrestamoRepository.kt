package com.example.prestamolab.data.repository

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow

interface PrestamoRepository {
    fun obtenerEquipos(): Flow < List < Equipo > >
    fun obtenerEquipo(id: Int): Flow < Equipo? >
    fun obtenerSolicitudes(): Flow < List < SolicitudPrestamo > >
    fun obtenerSolicitud(id: Int): Flow < SolicitudPrestamo? >
    suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result < Unit >
    suspend fun cancelarSolicitud(id: Int): Result < Unit >
}
