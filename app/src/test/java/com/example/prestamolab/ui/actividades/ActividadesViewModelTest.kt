package com.example.prestamolab.ui.actividades

import app.cash.turbine.test
import com.example.prestamolab.data.repository.InMemoryActividadRepository
import com.example.prestamolab.model.ReglasActividad
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActividadesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val instructor = Usuario("u-instructor", "Instructor CTMA", "instructor@sena.edu.co", Rol.INSTRUCTOR)
    private val estudiante = Usuario("u-estudiante", "Estudiante CTMA", "estudiante@sena.edu.co", Rol.ESTUDIANTE)
    private val ahora = ReglasActividad.aInstante("2026-09-24 08:00")!!

    private lateinit var repository: InMemoryActividadRepository
    private lateinit var viewModel: ActividadesViewModel

    @Before
    fun setup() {
        repository = InMemoryActividadRepository { ahora }
        viewModel = ActividadesViewModel(repository, instructor) { ahora }
    }

    private fun llenar(
        vm: ActividadesViewModel = viewModel,
        titulo: String = "Taller de soldadura",
        fecha: String = "2026-10-05 14:00"
    ) {
        vm.onTituloChanged(titulo)
        vm.onDescripcionChanged("Soldadura de componentes SMD")
        vm.onAmbienteChanged("Ambiente de Electrónica")
        vm.onFechaChanged(fecha)
    }

    @Test
    fun `TC-HU11-01 - Crear una actividad la guarda y aparece en la lista`() = runTest {
        viewModel.eventos.test {
            llenar()
            viewModel.guardar()

            assertEquals(EventoActividad.ActividadGuardada, awaitItem())
        }
        val creada = viewModel.uiState.value.actividades.single { it.titulo == "Taller de soldadura" }
        assertEquals("2026-10-05 14:00", creada.fecha)
        assertEquals("Ambiente de Electrónica", creada.ambiente)
        assertEquals(instructor.id, creada.instructorId)
    }

    @Test
    fun `TC-HU11-02 - Titulo vacio y fecha pasada muestran el error y no se guarda`() {
        llenar(titulo = "", fecha = "2026-09-20 08:00")
        viewModel.guardar()

        val errores = viewModel.uiState.value.formulario.errores
        assertEquals("El título es obligatorio.", errores.titulo)
        assertEquals("La fecha no puede estar en el pasado.", errores.fecha)
        assertEquals(1, viewModel.uiState.value.actividades.size)
    }

    @Test
    fun `TC-HU11-03 - Editar una actividad refleja los cambios en la lista`() {
        viewModel.editarActividad(1)
        assertEquals("Práctica de osciloscopio", viewModel.uiState.value.formulario.titulo)

        viewModel.onAmbienteChanged("Laboratorio 101")
        viewModel.guardar()

        assertEquals("Laboratorio 101", viewModel.uiState.value.actividades.single().ambiente)
    }

    @Test
    fun `Editar una actividad inexistente muestra el estado no encontrado`() {
        viewModel.editarActividad(99)

        assertTrue(viewModel.uiState.value.actividadNoEncontrada)
    }

    @Test
    fun `TC-HU11-04 - Eliminar la quita de la lista`() {
        viewModel.eliminarActividad(1)

        assertTrue(viewModel.uiState.value.actividades.isEmpty())
    }

    @Test
    fun `TC-HU11-05 - El estudiante ve las actividades en solo lectura y no puede cambiarlas`() {
        val vm = ActividadesViewModel(repository, estudiante) { ahora }

        assertFalse(vm.uiState.value.puedeGestionar)
        assertEquals(1, vm.uiState.value.actividades.size)

        llenar(vm)
        vm.guardar()
        vm.eliminarActividad(1)

        assertEquals(listOf("Práctica de osciloscopio"), vm.uiState.value.actividades.map { it.titulo })
        assertTrue(viewModel.uiState.value.puedeGestionar)
    }
}
