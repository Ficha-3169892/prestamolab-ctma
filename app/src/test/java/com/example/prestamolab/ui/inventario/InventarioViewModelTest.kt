package com.example.prestamolab.ui.inventario

import app.cash.turbine.test
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class InventarioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val instructor = Usuario("u-instructor", "Instructor CTMA", "instructor@sena.edu.co", Rol.INSTRUCTOR)
    private val estudiante = Usuario("u-estudiante", "Estudiante CTMA", "estudiante@sena.edu.co", Rol.ESTUDIANTE)

    private lateinit var repository: InMemoryPrestamoRepository
    private lateinit var viewModel: InventarioViewModel

    @Before
    fun setup() {
        repository = InMemoryPrestamoRepository()
        viewModel = InventarioViewModel(repository, instructor)
    }

    private fun llenar(nombre: String, categoria: String) {
        viewModel.onNombreChanged(nombre)
        viewModel.onCategoriaChanged(categoria)
    }

    @Test
    fun `TC-HU12-01 - Registrar un equipo lo deja DISPONIBLE y avisa para volver`() = runTest {
        viewModel.eventos.test {
            llenar("Proyector Epson", "Audiovisual")
            viewModel.guardar()

            assertEquals(EventoInventario.EquipoGuardado, awaitItem())
        }
        val nuevo = viewModel.uiState.value.equipos.single { it.nombre == "Proyector Epson" }
        assertEquals(EstadoEquipo.DISPONIBLE, nuevo.estado)
        assertEquals("", viewModel.uiState.value.formulario.nombre)
    }

    @Test
    fun `TC-HU12-02 - Nombre y categoria vacios muestran el error de cada campo y no se guarda`() {
        val antes = viewModel.uiState.value.equipos.size

        llenar("  ", "")
        viewModel.guardar()

        val errores = viewModel.uiState.value.formulario.errores
        assertEquals("El nombre es obligatorio.", errores.nombre)
        assertEquals("La categoría es obligatoria.", errores.categoria)
        assertEquals(antes, viewModel.uiState.value.equipos.size)
    }

    @Test
    fun `Escribir en un campo limpia solo su error`() {
        viewModel.guardar()

        viewModel.onNombreChanged("Proyector")

        assertNull(viewModel.uiState.value.formulario.errores.nombre)
        assertNotNull(viewModel.uiState.value.formulario.errores.categoria)
    }

    @Test
    fun `TC-HU12-03 - Editar carga el equipo y guarda los cambios`() {
        viewModel.editarEquipo(1)
        assertEquals("Multímetro Digital", viewModel.uiState.value.formulario.nombre)

        viewModel.onCategoriaChanged("Medición")
        viewModel.guardar()

        val editado = viewModel.uiState.value.equipos.single { it.id == 1 }
        assertEquals("Medición", editado.categoria)
        assertEquals("Multímetro Digital", editado.nombre)
    }

    @Test
    fun `Editar un equipo inexistente muestra el estado no encontrado`() {
        viewModel.editarEquipo(99)

        assertTrue(viewModel.uiState.value.equipoNoEncontrado)
    }

    @Test
    fun `TC-HU12-04 - Eliminar un equipo con prestamo activo muestra el motivo y lo conserva`() {
        // Equipo 2: solicitud semilla #1 en estado SOLICITADA
        viewModel.eliminarEquipo(2)

        assertEquals("No se puede eliminar: el equipo tiene un préstamo activo.", viewModel.uiState.value.mensajeError)
        assertTrue(viewModel.uiState.value.equipos.any { it.id == 2 })
    }

    @Test
    fun `Eliminar un equipo sin prestamos lo quita del inventario`() {
        viewModel.eliminarEquipo(3)

        assertNull(viewModel.uiState.value.mensajeError)
        assertTrue(viewModel.uiState.value.equipos.none { it.id == 3 })
    }

    @Test
    fun `TC-HU12-05 - El estudiante no puede registrar ni eliminar equipos`() {
        val vm = InventarioViewModel(repository, estudiante)
        val antes = vm.uiState.value.equipos

        vm.onNombreChanged("Proyector")
        vm.onCategoriaChanged("Audiovisual")
        vm.guardar()
        vm.eliminarEquipo(3)

        assertEquals(antes, vm.uiState.value.equipos)
    }
}
