package com.example.prestamolab.data.repository

import com.example.prestamolab.model.Devolucion
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.NuevaDevolucion
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow

interface PrestamoRepository {
    val equipos: Flow<List<Equipo>>
    val solicitudes: Flow<List<SolicitudPrestamo>>
    val devoluciones: Flow<List<Devolucion>>
    suspend fun obtenerEquipo(id: Int): Equipo?

    /** Falla si el equipo no existe o no está DISPONIBLE. */
    suspend fun crearSolicitud(nueva: NuevaSolicitud): Result<SolicitudPrestamo>

    /** Idempotente: cancelar una solicitud ya CANCELADA no produce cambios (TC-16). */
    suspend fun cancelarSolicitud(id: Int): Result<Unit>

    /** Cierra un préstamo PRESTADO: lo marca DEVUELTO y libera el equipo. Falla en cualquier otro estado. */
    suspend fun registrarDevolucion(nueva: NuevaDevolucion): Result<Devolucion>
}
