package com.example.prestamolab.viewmodel

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrestamoViewModelTest {

    // Simulamos el "Main Thread" de Android para que el ViewModel no explote
    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // TC-12: Intentar solicitar un equipo no disponible
    @Test
    fun guardarSolicitudParaEquipoNoDisponibleFalla() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado != EstadoEquipo.DISPONIBLE }

        var exito = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Intento de solicitud",
            duracion = 2
        ) { exito = true } // Callback de éxito

        assertFalse(exito)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
    }

    // TC-13: Doble pulsación de Guardar no genera duplicados
    @Test
    fun dobleGuardadoNoCreaDosSolicitudes() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        var primeraSolicitud = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Primera solicitud",
            duracion = 2
        ) { primeraSolicitud = true }

        var segundaSolicitud = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Segunda solicitud",
            duracion = 2
        ) { segundaSolicitud = true }

        assertTrue(primeraSolicitud)
        assertFalse(segundaSolicitud)

        assertEquals(1, repository.obtenerSolicitudes().first().size)
    }

    // TC-14: Crear una solicitud válida
    @Test
    fun guardarSolicitudValidaActualizaElEstado() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        var exito = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Prueba de solicitud",
            duracion = 2
        ) { exito = true }

        assertTrue(exito)

        assertEquals(1, viewModel.uiState.value.solicitudes.size)
        assertEquals(
            EstadoSolicitud.SOLICITADA,
            viewModel.uiState.value.solicitudes.first().estado
        )

        assertEquals(
            EstadoEquipo.RESERVADO,
            viewModel.uiState.value.equipos.first { it.id == equipo.id }.estado
        )
    }

    // TC-15: Cancelar una solicitud SOLICITADA
    @Test
    fun cancelarSolicitudActualizaSolicitudYDisponibilidad() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        var creada = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Solicitud para cancelar",
            duracion = 2
        ) { creada = true }

        assertTrue(creada)

        val solicitudId = viewModel.uiState.value.solicitudes.first().id

        viewModel.seleccionarSolicitud(solicitudId)
        viewModel.cancelarSolicitud(solicitudId)

        assertEquals(
            EstadoSolicitud.CANCELADA,
            viewModel.uiState.value.solicitudSeleccionada?.estado
        )

        assertEquals(
            EstadoEquipo.DISPONIBLE,
            viewModel.uiState.value.equipos.first { it.id == equipo.id }.estado
        )
    }

    // Validación: ambiente obligatorio
    @Test
    fun guardarSolicitudSinAmbienteFalla() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        var exito = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "",
            proposito = "Propósito válido",
            duracion = 2
        ) { exito = true }

        assertFalse(exito)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
    }

    // Validación: datos inválidos no se guardan
    @Test
    fun datosInvalidosNoCreanSolicitud() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        var exito = false
        viewModel.guardarSolicitud(
            equipoId = equipo.id,
            ambiente = "Laboratorio",
            proposito = "Corto",
            duracion = 2
        ) { exito = true }

        assertFalse(exito)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
    }

    // Manejo de ID de equipo inexistente
    @Test
    fun seleccionarEquipoInexistenteDejaEquipoSeleccionadoEnNull() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        viewModel.seleccionarEquipo(-999)
        assertNull(viewModel.uiState.value.equipoSeleccionado)
    }

    // Manejo de ID de solicitud inexistente
    @Test
    fun seleccionarSolicitudInexistenteDejaSolicitudSeleccionadaEnNull() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        viewModel.seleccionarSolicitud(-999)
        assertNull(viewModel.uiState.value.solicitudSeleccionada)
    }
}
