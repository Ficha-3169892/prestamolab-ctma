package com.example.prestamolab.repository

import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

interface PrestamoRepository {

    fun obtenerEquipos(): List<Equipo>

    fun obtenerEquipo(id: Int): Equipo?

    fun obtenerSolicitudes(): List<SolicitudPrestamo>

    fun obtenerSolicitud(id: Int): SolicitudPrestamo?

    fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit>

    fun cancelarSolicitud(id: Int): Result<Unit>
}