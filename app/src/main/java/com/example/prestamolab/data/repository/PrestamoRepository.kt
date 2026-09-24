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

    /** HU-14: SOLICITADA → PRESTADO y el equipo queda PRESTADO. Falla en cualquier otro estado. */
    suspend fun aprobarSolicitud(id: Int, instructorId: String): Result<Unit>

    /** HU-14: SOLICITADA → RECHAZADA con motivo y el equipo vuelve a DISPONIBLE. */
    suspend fun rechazarSolicitud(id: Int, instructorId: String, motivo: String): Result<Unit>

    /** HU-12: el equipo nuevo queda DISPONIBLE. Falla si nombre o categoría no son válidos. */
    suspend fun registrarEquipo(nombre: String, categoria: String): Result<Equipo>

    suspend fun editarEquipo(id: Int, nombre: String, categoria: String): Result<Unit>

    /** Falla si el equipo tiene préstamos (CA-HU12-04). */
    suspend fun eliminarEquipo(id: Int): Result<Unit>
}
