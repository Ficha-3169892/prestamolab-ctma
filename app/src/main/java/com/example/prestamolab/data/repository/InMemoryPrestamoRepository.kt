package com.example.prestamolab.data.repository

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(1, "Multímetro Digital", "Herramienta", "DISPONIBLE"),
        Equipo(2, "Osciloscopio 100MHz", "Laboratorio", "DISPONIBLE"),
        Equipo(3, "Cautín Estación de Soldadura", "Herramienta", "DISPONIBLE"),
        Equipo(4, "Fuente de Poder DC", "Laboratorio", "DISPONIBLE")
    )

    private val solicitudes = mutableListOf(
        SolicitudPrestamo(1, 2, "Andrés Vargas", "2026-09-02", "2026-09-05", "PENDIENTE")
    )

    override fun obtenerEquipos(): List<Equipo> = equipos.toList()

    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }

    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes.toList()

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        return try {
            solicitudes.add(solicitud)
            // Cambiamos a EN_PRESTAMO (o el estado que prefieras según tu guía)
            val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
            if (equipoIndex != -1) {
                equipos[equipoIndex] = equipos[equipoIndex].copy(estado = "EN_PRESTAMO")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val index = solicitudes.indexOfFirst { it.id == id }
        return if (index != -1) {
            val solicitudActual = solicitudes[index]
            solicitudes[index] = solicitudActual.copy(estado = "CANCELADA")

            // Al cancelar, el equipo vuelve a estar disponible
            val equipoIndex = equipos.indexOfFirst { it.id == solicitudActual.equipoId }
            if (equipoIndex != -1) {
                equipos[equipoIndex] = equipos[equipoIndex].copy(estado = "DISPONIBLE")
            }

            Result.success(Unit)
        } else {
            Result.failure(Exception("Solicitud no encontrada"))
        }
    }
}