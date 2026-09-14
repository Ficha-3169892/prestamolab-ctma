package com.example.prestamolab.viewmodel

import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakePrestamoRepository : PrestamoRepository {
    var equiposList = listOf(Equipo(id = 1, nombre = "Laptop Dell", categoria = CategoriaEquipo.COMPUTACION, estado = EstadoEquipo.DISPONIBLE))
    var solicitudesList = mutableListOf<SolicitudPrestamo>()
    var failCrearSolicitud = false

    override fun obtenerEquipos(): List<Equipo> = equiposList
    override fun obtenerEquipo(id: Int): Equipo? = equiposList.find { it.id == id }
    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudesList
    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudesList.find { it.id == id }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        return if (failCrearSolicitud) {
            Result.failure(Exception("Error forzado"))
        } else {
            solicitudesList.add(solicitud)
            Result.success(Unit)
        }
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val index = solicitudesList.indexOfFirst { it.id == id }
        if (index != -1) {
            solicitudesList[index] = solicitudesList[index].copy(estado = EstadoSolicitud.CANCELADA)
            return Result.success(Unit)
        }
        return Result.failure(Exception("No encontrada"))
    }
}

class PrestamoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakePrestamoRepository
    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setUp() {
        fakeRepository = FakePrestamoRepository()
        viewModel = PrestamoViewModel(fakeRepository)
    }

    @Test
    fun registrarSolicitud_falla_cuandoDatosSonInvalidos() {
        var successCalled = false
        
        // Ambiente inválido (vacío)
        viewModel.registrarSolicitud(1, "", "Propósito válido largo de diez caracteres", 5) {
            successCalled = true
        }
        assertEquals("El ambiente o destino es obligatorio.", viewModel.uiState.value.mensaje)
        assertTrue(viewModel.uiState.value.solicitudes.isEmpty())

        // Propósito inválido (corto)
        viewModel.registrarSolicitud(1, "Ambiente", "Corto", 5) {
            successCalled = true
        }
        assertEquals("El propósito debe tener entre 10 y 180 caracteres.", viewModel.uiState.value.mensaje)

        // Duración inválida (fuera de 1..8)
        viewModel.registrarSolicitud(1, "Ambiente", "Propósito válido largo de diez caracteres", 9) {
            successCalled = true
        }
        assertEquals("La duración debe estar entre 1 y 8 horas.", viewModel.uiState.value.mensaje)
        
        assertEquals(false, successCalled)
    }

    @Test
    fun registrarSolicitud_creaConExito_cuandoDatosSonValidos() {
        var successCalled = false
        
        viewModel.registrarSolicitud(1, "Ambiente B", "Propósito válido largo de diez caracteres", 4) {
            successCalled = true
        }

        assertEquals("Solicitud registrada con éxito.", viewModel.uiState.value.mensaje)
        assertEquals(1, viewModel.uiState.value.solicitudes.size)
        assertEquals(EstadoSolicitud.SOLICITADA, viewModel.uiState.value.solicitudes[0].estado)
        assertEquals(true, successCalled)
    }

    @Test
    fun cancelarSolicitud_cambiaEstadoACancelada() {
        // Primero registramos una válida
        fakeRepository.solicitudesList.add(
            SolicitudPrestamo(
                id = 1,
                equipoId = 1,
                ambienteDestino = "Ambiente",
                proposito = "Propósito válido",
                duracionHoras = 4,
                estado = EstadoSolicitud.SOLICITADA
            )
        )
        viewModel.cargarDatos()

        viewModel.cancelarSolicitud(1)

        assertEquals("Solicitud cancelada con éxito.", viewModel.uiState.value.mensaje)
        assertEquals(EstadoSolicitud.CANCELADA, viewModel.uiState.value.solicitudes[0].estado)
    }
}
