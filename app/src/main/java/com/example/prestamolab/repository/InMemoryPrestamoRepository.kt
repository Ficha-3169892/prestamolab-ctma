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
            nombre = "Multímetro Digital",
            categoria = CategoriaEquipo.ELECTRONICA,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 2,
            nombre = "Kit de Arduino",
            categoria = CategoriaEquipo.ELECTRONICA,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 3,
            nombre = "Cámara Digital",
            categoria = CategoriaEquipo.AUDIOVISUAL,
            estado = EstadoEquipo.RESERVADO
        ),
        Equipo(
            id = 4,
            nombre = "Portátil Lenovo",
            categoria = CategoriaEquipo.COMPUTO,
            estado = EstadoEquipo.PRESTADO
        ),
        Equipo(
            id = 5,
            nombre = "Taladro Inalámbrico",
            categoria = CategoriaEquipo.HERRAMIENTAS,
            estado = EstadoEquipo.DISPONIBLE
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
            ?: return Result.failure(
                IllegalArgumentException("El equipo no existe")
            )

        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(
                IllegalStateException("El equipo no está disponible")
            )
        }

        val solicitudActiva = solicitudes.any {
            it.equipoId == solicitud.equipoId &&
                    it.estado == EstadoSolicitud.SOLICITADA
        }

        if (solicitudActiva) {
            return Result.failure(
                IllegalStateException(
                    "Ya existe una solicitud activa para este equipo"
                )
            )
        }

        solicitudes.add(solicitud)

        actualizarEstadoEquipo(
            solicitud.equipoId,
            EstadoEquipo.RESERVADO
        )

        return Result.success(Unit)
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {

        val indice = solicitudes.indexOfFirst {
            it.id == id
        }

        if (indice == -1) {
            return Result.failure(
                IllegalArgumentException(
                    "La solicitud no existe"
                )
            )
        }

        val solicitud = solicitudes[indice]

        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(
                IllegalStateException(
                    "Solo se pueden cancelar solicitudes SOLICITADA"
                )
            )
        }

        solicitudes[indice] = solicitud.copy(
            estado = EstadoSolicitud.CANCELADA
        )

        actualizarEstadoEquipo(
            solicitud.equipoId,
            EstadoEquipo.DISPONIBLE
        )

        return Result.success(Unit)
    }

    private fun actualizarEstadoEquipo(
        equipoId: Int,
        nuevoEstado: EstadoEquipo
    ) {
        val indice = equipos.indexOfFirst {
            it.id == equipoId
        }

        if (indice != -1) {
            equipos[indice] = equipos[indice].copy(
                estado = nuevoEstado
            )
        }
    }
}
