package com.example.prestamolab.viewmodel

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrestamoViewModelTest {

    // TC-12: Intentar solicitar un equipo no disponible
    @Test
    fun guardarSolicitudParaEquipoNoDisponibleFalla() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipo = repository.obtenerEquipos()
            .first { it.estado != EstadoEquipo.DISPONIBLE }

        val resultado = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Intento de solicitud",
            duracion = 2
        )

        assertFalse(resultado)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
    }

    // TC-13: Doble pulsación de Guardar no genera duplicados
    @Test
    fun dobleGuardadoNoCreaDosSolicitudes() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipo = repository.obtenerEquipos()
            .first { it.estado == EstadoEquipo.DISPONIBLE }

        val primeraSolicitud = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Primera solicitud",
            duracion = 2
        )

        val segundaSolicitud = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Segunda solicitud",
            duracion = 2
        )

        assertTrue(primeraSolicitud)
        assertFalse(segundaSolicitud)

        assertEquals(
            1,
            repository.obtenerSolicitudes().size
        )
    }

    // TC-14: Crear una solicitud válida
    @Test
    fun guardarSolicitudValidaActualizaElEstado() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipo = repository.obtenerEquipos()
            .first { it.estado == EstadoEquipo.DISPONIBLE }

        val resultado = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Prueba de solicitud",
            duracion = 2
        )

        assertTrue(resultado)

        assertEquals(1, viewModel.uiState.value.solicitudes.size)
        assertEquals(
            EstadoSolicitud.SOLICITADA,
            viewModel.uiState.value.solicitudes.first().estado
        )

        assertEquals(
            EstadoEquipo.RESERVADO,
            viewModel.uiState.value.equipos
                .first { it.id == equipo.id }
                .estado
        )
    }

    // TC-15: Cancelar una solicitud SOLICITADA
    @Test
    fun cancelarSolicitudActualizaSolicitudYDisponibilidad() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipo = repository.obtenerEquipos()
            .first { it.estado == EstadoEquipo.DISPONIBLE }

        val creada = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Solicitud para cancelar",
            duracion = 2
        )

        assertTrue(creada)

        val solicitudId = viewModel.uiState.value.solicitudes.first().id

        viewModel.cancelarSolicitud(solicitudId)

        assertEquals(
            EstadoSolicitud.CANCELADA,
            viewModel.uiState.value.solicitudSeleccionada?.estado
        )

        assertEquals(
            EstadoEquipo.DISPONIBLE,
            viewModel.uiState.value.equipos
                .first { it.id == equipo.id }
                .estado
        )
    }

    // Validación: ambiente obligatorio
    @Test
    fun guardarSolicitudSinAmbienteFalla() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipo = repository.obtenerEquipos()
            .first { it.estado == EstadoEquipo.DISPONIBLE }

        val resultado = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "",
            proposito = "Propósito válido",
            duracion = 2
        )

        assertFalse(resultado)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
    }

    // Validación: datos inválidos no se guardan
    @Test
    fun datosInvalidosNoCreanSolicitud() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipo = repository.obtenerEquipos()
            .first { it.estado == EstadoEquipo.DISPONIBLE }

        val resultado = viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Corto",
            duracion = 2
        )

        assertFalse(resultado)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
    }

    // Manejo de ID de equipo inexistente
    @Test
    fun seleccionarEquipoInexistenteDejaEquipoSeleccionadoEnNull() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        viewModel.seleccionarEquipo(-999)

        assertNull(viewModel.uiState.value.equipoSeleccionado)
    }

    // Manejo de ID de solicitud inexistente
    @Test
    fun seleccionarSolicitudInexistenteDejaSolicitudSeleccionadaEnNull() {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        viewModel.seleccionarSolicitud(-999)

        assertNull(viewModel.uiState.value.solicitudSeleccionada)
    }
}
