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
    suspend fun agregarEquipo(equipo: Equipo): Result < Unit >
    suspend fun editarEquipo(equipo: Equipo): Result < Unit >
    suspend fun eliminarEquipo(id: Int): Result < Unit >
    suspend fun adjuntarEvidencia(solicitudId: Int, imagenBytes: ByteArray, extension: String): Result < String >
    suspend fun sincronizarEquipos()
    suspend fun sincronizarSolicitudes()
}
