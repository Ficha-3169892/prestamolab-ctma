package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.repository.InMemoryPrestamoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrestamoViewModelTest {

    private lateinit var repository: InMemoryPrestamoRepository
    private lateinit var viewModel: PrestamoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryPrestamoRepository()
        viewModel = PrestamoViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun alIniciar_debeCargarLosEquipos() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(5, state.equipos.size)
    }

    @Test
    fun alIniciar_noDebeHaberSolicitudes() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun obtenerEquipo_cuandoExiste_debeRetornarlo() = runTest {
        val equipo = viewModel.obtenerEquipo(1)
        assertEquals("Multímetro Digital", equipo?.nombre)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun crearSolicitud_cuandoEquipoDisponible_debeEmitirEvento() = runTest {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )
        advanceUntilIdle()
        
        // El estado debería tener la solicitud
        val solicitudes = viewModel.uiState.value.solicitudes
        assertEquals(1, solicitudes.size)
        assertEquals(EstadoSolicitud.SOLICITADA, solicitudes[0].estado)
    }

    @Test
    fun cancelarSolicitud_debeLiberarElEquipo() = runTest {
        viewModel.crearSolicitud(1, "Lab", "Prop", 2)
        advanceUntilIdle()
        
        viewModel.cancelarSolicitud(1)
        advanceUntilIdle()

        val equipo = viewModel.obtenerEquipo(1)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun registrarDevolucion_debeLiberarElEquipo() = runTest {
        viewModel.crearSolicitud(1, "Lab", "Prop", 2)
        advanceUntilIdle()
        
        viewModel.registrarDevolucion(1, "uri")
        advanceUntilIdle()

        val equipo = viewModel.obtenerEquipo(1)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
        
        val solicitud = viewModel.obtenerSolicitud(1)
        assertEquals(EstadoSolicitud.DEVUELTA, solicitud?.estado)
    }

    @Test
    fun loginAdmin_conCredencialesCorrectas_debeIniciarSesion() = runTest {
        val exito = viewModel.loginAdmin("admin@gmail.com", "admin123")
        assertTrue(exito)
        assertTrue(viewModel.uiState.value.isAdminLoggedIn)
    }

    @Test
    fun loginAdmin_conCredencialesIncorrectas_debeFallar() = runTest {
        val exito = viewModel.loginAdmin("usuario@gmail.com", "wrongpass")
        assertFalse(exito)
        assertFalse(viewModel.uiState.value.isAdminLoggedIn)
    }

    @Test
    fun logoutAdmin_debeCerrarSesion() = runTest {
        viewModel.loginAdmin("admin@gmail.com", "admin123")
        assertTrue(viewModel.uiState.value.isAdminLoggedIn)

        viewModel.logoutAdmin()
        assertFalse(viewModel.uiState.value.isAdminLoggedIn)
    }

    @Test
    fun borrarHistorial_debeEliminarDevueltasYCanceladas() = runTest {
        viewModel.crearSolicitud(1, "Lab", "Prop", 2)
        advanceUntilIdle()

        viewModel.registrarDevolucion(1, "uri")
        advanceUntilIdle()

        viewModel.borrarHistorial()
        advanceUntilIdle()

        val solicitudes = viewModel.uiState.value.solicitudes
        assertTrue(solicitudes.isEmpty())
    }
}
