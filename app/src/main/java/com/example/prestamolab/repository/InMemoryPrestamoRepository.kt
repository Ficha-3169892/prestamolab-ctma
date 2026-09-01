package com.example.prestamolab.repository

import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(
            id = 1,
            nombre = "Kit de electrónica",
            categoria = CategoriaEquipo.ELECTRONICA,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 2,
            nombre = "Multímetro digital",
            categoria = CategoriaEquipo.ELECTRONICA,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 3,
            nombre = "Taladro eléctrico",
            categoria = CategoriaEquipo.HERRAMIENTA,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 4,
            nombre = "Tablet de formación",
            categoria = CategoriaEquipo.COMPUTO,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 5,
            nombre = "Cámara digital",
            categoria = CategoriaEquipo.OTRO,
            estado = EstadoEquipo.RESERVADO
        )
    )

    private val solicitudes = mutableListOf<SolicitudPrestamo>()

    override fun obtenerEquipos(): List<Equipo> {
        return equipos.toList()
    }

    override fun obtenerEquipo(id: Int): Equipo? {
        return equipos.find { it.id == id }
    }

    override fun obtenerSolicitudes(): List<SolicitudPrestamo> {
        return solicitudes.toList()
    }

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? {
        return solicitudes.find { it.id == id }
    }

    override fun crearSolicitud(
        solicitud: SolicitudPrestamo
    ): Result<Unit> {

        val equipo = obtenerEquipo(solicitud.equipoId)

        if (equipo == null) {
            return Result.failure(
                Exception("El equipo no existe")
            )
        }

        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(
                Exception("El equipo no está disponible")
            )
        }

        val solicitudDuplicada = solicitudes.any {
            it.equipoId == solicitud.equipoId &&
                    it.estado == EstadoSolicitud.SOLICITADA
        }

        if (solicitudDuplicada) {
            return Result.failure(
                Exception("Ya existe una solicitud para este equipo")
            )
        }

        solicitudes.add(solicitud)

        val posicion = equipos.indexOfFirst {
            it.id == solicitud.equipoId
        }

        equipos[posicion] = equipo.copy(
            estado = EstadoEquipo.RESERVADO
        )

        return Result.success(Unit)
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {

        val solicitud = obtenerSolicitud(id)

        if (solicitud == null) {
            return Result.failure(
                Exception("La solicitud no existe")
            )
        }

        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(
                Exception("Solo se pueden cancelar solicitudes SOLICITADA")
            )
        }

        val posicionSolicitud = solicitudes.indexOfFirst {
            it.id == id
        }

        solicitudes[posicionSolicitud] = solicitud.copy(
            estado = EstadoSolicitud.CANCELADA
        )

        val equipo = obtenerEquipo(solicitud.equipoId)

        if (equipo != null) {
            val posicionEquipo = equipos.indexOfFirst {
                it.id == solicitud.equipoId
            }

            equipos[posicionEquipo] = equipo.copy(
                estado = EstadoEquipo.DISPONIBLE
            )
        }

        return Result.success(Unit)
    }
}