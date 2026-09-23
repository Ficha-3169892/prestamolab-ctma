package com.example.prestamolab.data.repository

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow

interface PrestamoRepository {
    fun obtenerEquipos(): List<Equipo>
    fun obtenerEquipo(id: Int): Equipo?
    fun obtenerSolicitudes(): Flow<List<SolicitudPrestamo>>
    fun buscarSolicitudes(query: String): Flow<List<SolicitudPrestamo>>
    fun obtenerSolicitud(id: Int): SolicitudPrestamo?
    suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit>
    suspend fun cancelarSolicitud(id: Long): Result<Unit>
    suspend fun cancelarSolicitud(id: Int): Result<Unit> = cancelarSolicitud(id.toLong())
}
