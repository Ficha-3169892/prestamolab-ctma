package com.example.prestamolab.repository

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow

interface PrestamoRepository {
    fun obtenerEquipos(): Flow<List<Equipo>>
    suspend fun obtenerEquipo(id: Int): Equipo?
    suspend fun crearEquipo(equipo: Equipo): Result<Unit>
    suspend fun actualizarEquipo(equipo: Equipo): Result<Unit>
    suspend fun eliminarEquipo(id: Int): Result<Unit>
    
    fun obtenerSolicitudes(): Flow<List<SolicitudPrestamo>>
    fun obtenerActiveSolicitudes(): Flow<List<SolicitudPrestamo>>
    suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo?
    suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit>
    suspend fun cancelarSolicitud(id: Int): Result<Unit>
    suspend fun registrarDevolucion(solicitudId: Int, fotoUri: String?, latitud: Double?, longitud: Double?): Result<Unit>
}
