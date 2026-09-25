package com.example.prestamolab.auth

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.viewmodel.PrestamoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RolePermissionsTest {

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun adminPuedeAgregarEquipoExitosamente() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        var agregado = false
        viewModel.agregarEquipo(
            nombre = "Osciloscopio Test",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.DISPONIBLE,
            userRole = "admin"
        ) { agregado = true }

        assertTrue(agregado)
        val equipos = repository.obtenerEquipos().first()
        assertTrue(equipos.any { it.nombre == "Osciloscopio Test" })
    }

    @Test
    fun usuarioNormalNoPuedeAgregarEquipoYGeneraError() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        var agregado = false
        viewModel.agregarEquipo(
            nombre = "Osciloscopio Test",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.DISPONIBLE,
            userRole = "usuario"
        ) { agregado = true }

        assertTrue(!agregado)
        assertNotNull(viewModel.uiState.value.mensajeError)
        assertTrue(viewModel.uiState.value.mensajeError!!.contains("Acceso denegado"))
    }

    @Test
    fun adminPuedeEliminarEquipo() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val iniciales = repository.obtenerEquipos().first()
        val primerEquipoId = iniciales.first().id

        var eliminado = false
        viewModel.eliminarEquipo(primerEquipoId, "admin") {
            eliminado = true
        }

        assertTrue(eliminado)
        val finales = repository.obtenerEquipos().first()
        assertTrue(finales.none { it.id == primerEquipoId })
    }

    @Test
    fun usuarioNormalNoPuedeEliminarEquipo() = runBlocking {
        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val iniciales = repository.obtenerEquipos().first()
        val primerEquipoId = iniciales.first().id

        var eliminado = false
        viewModel.eliminarEquipo(primerEquipoId, "usuario") {
            eliminado = true
        }

        assertTrue(!eliminado)
        assertNotNull(viewModel.uiState.value.mensajeError)
        assertEquals("Acceso denegado: Requiere rol de Administrador", viewModel.uiState.value.mensajeError)
    }
}
