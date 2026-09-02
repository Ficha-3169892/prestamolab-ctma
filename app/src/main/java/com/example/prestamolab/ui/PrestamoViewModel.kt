package com.example.prestamolab.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.SolicitudPrestamo

class PrestamoViewModel : ViewModel() {

    private val repository = InMemoryPrestamoRepository()

    var equipos by mutableStateOf<List<Equipo>>(emptyList())
        private set

    var solicitudes by mutableStateOf<List<SolicitudPrestamo>>(emptyList())
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        equipos = repository.obtenerEquipos()
        solicitudes = repository.obtenerSolicitudes()
    }

    fun solicitarPrestamo(equipoId: Int, solicitante: String, fechaInicio: String, fechaFin: String) {
        val nuevaSolicitud = SolicitudPrestamo(
            id = (solicitudes.size + 1),
            equipoId = equipoId,
            solicitante = solicitante,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            estado = "PENDIENTE"
        )

        repository.crearSolicitud(nuevaSolicitud)
            .onSuccess {
                // Forzamos asignación de nuevas listas para que Compose detecte el cambio de estado
                equipos = repository.obtenerEquipos()
                solicitudes = repository.obtenerSolicitudes()
                mensajeError = null
            }
            .onFailure {
                mensajeError = it.message
            }
    }

    fun cancelarSolicitud(id: Int) {
        repository.cancelarSolicitud(id)
            .onSuccess {
                equipos = repository.obtenerEquipos()
                solicitudes = repository.obtenerSolicitudes()
            }
            .onFailure {
                mensajeError = it.message
            }
    }
}