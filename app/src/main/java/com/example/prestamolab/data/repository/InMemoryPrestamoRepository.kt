package com.example.prestamolab.data.repository

import com.example.prestamolab.model.*

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(1, "Multímetro Digital", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Kit de Electrónica", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Cámara Fotográfica", CategoriaEquipo.PERIFERICOS, EstadoEquipo.RESERVADO)
    )

    private val solicitudes = mutableListOf<SolicitudPrestamo>()
    private var siguienteSolicitudId = 1

    override fun obtenerEquipos(): List<Equipo> = equipos.toList()

    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }

    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes.toList()

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    @Synchronized
    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex == -1) return Result.failure(IllegalArgumentException("Equipo no encontrado"))

        val equipo = equipos[equipoIndex]
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(IllegalStateException("El equipo no está disponible"))
        }

        val nuevaSolicitud = solicitud.copy(id = siguienteSolicitudId++)
        solicitudes.add(nuevaSolicitud)
        equipos[equipoIndex] = equipo.copy(estado = EstadoEquipo.RESERVADO)

        return Result.success(Unit)
    }

    @Synchronized
    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val index = solicitudes.indexOfFirst { it.id == id }
        if (index == -1) return Result.failure(IllegalArgumentException("Solicitud no encontrada"))

        val solicitud = solicitudes[index]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(IllegalStateException("Solo se pueden cancelar solicitudes en estado SOLICITADA"))
        }

        solicitudes[index] = solicitud.copy(estado = EstadoSolicitud.CANCELADA)

        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
        }

        return Result.success(Unit)
    }
}
