package com.example.prestamolab.ui

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.data.sync.AvisosSincronizacion
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import app.cash.turbine.test
import com.example.prestamolab.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PrestamoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: InMemoryPrestamoRepository
    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setup() {
        repository = InMemoryPrestamoRepository()
        viewModel = PrestamoViewModel(repository)
    }

    @Test
    fun `TC-01 - Cargar datos iniciales carga equipos y solicitudes correctamente`() {
        val state = viewModel.uiState.value
        assertFalse(state.equipos.isEmpty())
        assertFalse(state.solicitudes.isEmpty())
    }

    @Test
    fun `TC-02 - Seleccionar equipo valido actualiza el estado correctamente`() {
        val equipo = viewModel.uiState.value.equipos.first()
        viewModel.seleccionarEquipoParaDetalle(equipo)

        val state = viewModel.uiState.value
        assertEquals(equipo, state.equipoSeleccionado)
        assertNull(state.equipoNoEncontrado)
    }

    @Test
    fun `TC-03 - Seleccionar equipo inexistente pone equipoSeleccionado como null`() {
        viewModel.seleccionarEquipoPorId(999)

        val state = viewModel.uiState.value
        assertNull(state.equipoSeleccionado)
        // La ruta equipo/999 muestra "Equipo no encontrado" en lugar de quedarse cargando
        assertEquals(999, state.equipoNoEncontrado)
    }

    @Test
    fun `Seleccionar por id un equipo existente lo carga desde el repositorio`() {
        viewModel.seleccionarEquipoPorId(4)

        assertEquals("Fuente de Poder DC", viewModel.uiState.value.equipoSeleccionado?.nombre)
        assertNull(viewModel.uiState.value.equipoNoEncontrado)
    }

    @Test
    fun `TC-04 - Proposito con 9 caracteres retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("123456789")
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
        assertEquals("Propósito debe tener mínimo 10 caracteres", viewModel.uiState.value.errorProposito)
    }

    @Test
    fun `TC-05 - Proposito con 10 caracteres es valido`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("1234567890")
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
        assertNull(viewModel.uiState.value.errorProposito)
    }

    @Test
    fun `TC-06 - Proposito con 180 caracteres es valido`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("a".repeat(180))
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
    }

    @Test
    fun `TC-07 - Proposito con 181 caracteres retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("a".repeat(181))
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
        assertEquals("Máximo 180 caracteres", viewModel.uiState.value.errorProposito)
    }

    @Test
    fun `TC-08 - Duracion 0 horas retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("Propósito válido")
        viewModel.onDuracionChanged("0")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
        assertEquals("Duración entre 1 y 8 horas", viewModel.uiState.value.errorDuracion)
    }

    @Test
    fun `TC-09 - Duracion 1 hora es valida`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("Propósito válido")
        viewModel.onDuracionChanged("1")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
    }

    @Test
    fun `TC-10 - Duracion 8 horas es valida`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("Propósito válido")
        viewModel.onDuracionChanged("8")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
    }

    @Test
    fun `TC-11 - Duracion 9 horas retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("Propósito válido")
        viewModel.onDuracionChanged("9")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
        assertEquals("Duración entre 1 y 8 horas", viewModel.uiState.value.errorDuracion)
    }

    @Test
    fun `TC-12 - Intentar solicitar equipo RESERVADO retorna falso`() {
        val equipo = Equipo(99, "Test", "Cat", EstadoEquipo.RESERVADO)
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Proposito valido")
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
    }

    @Test
    fun `TC-13 - Doble pulsacion mientras se guarda crea una sola solicitud`() = runTest {
        // Repositorio que no responde hasta que liberamos la llamada: simula red o disco lento
        val respuesta = CompletableDeferred<Result<com.example.prestamolab.model.SolicitudPrestamo>>()
        val repoLento = mockk<PrestamoRepository> {
            every { equipos } returns MutableStateFlow(listOf(Equipo(1, "Multímetro", "Herramienta", EstadoEquipo.DISPONIBLE)))
            every { solicitudes } returns MutableStateFlow(emptyList())
            coEvery { crearSolicitud(any()) } coAnswers { respuesta.await() }
        }
        val vm = PrestamoViewModel(repoLento)
        vm.seleccionarEquipoParaDetalle(vm.uiState.value.equipos.first())
        vm.onAmbienteChanged("Lab 1")
        vm.onPropositoChanged("Proposito valido")
        vm.onDuracionChanged("2")

        assertTrue(vm.guardarSolicitud())
        assertTrue(vm.uiState.value.guardando)
        assertFalse(vm.guardarSolicitud())
        assertFalse(vm.guardarSolicitud())

        respuesta.complete(Result.failure(IllegalStateException("fin")))
        coVerify(exactly = 1) { repoLento.crearSolicitud(any()) }
        assertFalse(vm.uiState.value.guardando)
    }

    @Test
    fun `TC-14 - Crear solicitud valida completa cambia estado a SOLICITADA y equipo a RESERVADO`() {
        val equipo = viewModel.uiState.value.equipos.first { it.id == 3 }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Lab 305")
        viewModel.onPropositoChanged("Practica de Redes")
        viewModel.onDuracionChanged("4")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
        assertFalse(viewModel.uiState.value.guardando)
        val equipoFinal = viewModel.uiState.value.equipos.find { it.id == 3 }
        assertEquals(EstadoEquipo.RESERVADO, equipoFinal?.estado)
        val solicitudFinal = viewModel.uiState.value.solicitudes.last()
        assertEquals(EstadoSolicitud.SOLICITADA, solicitudFinal.estado)
        // PB-08: la solicitud conserva los datos del formulario
        assertEquals("Lab 305", solicitudFinal.ambiente)
        assertEquals("Practica de Redes", solicitudFinal.proposito)
        assertEquals(4, solicitudFinal.duracionHoras)
    }

    @Test
    fun `TC-15 - Cancelar solicitud SOLICITADA pasa a CANCELADA y libera equipo`() {
        val idSolicitud = 1 // De los datos iniciales
        assertEquals(EstadoEquipo.RESERVADO, viewModel.uiState.value.equipos.find { it.id == 2 }?.estado)
        viewModel.cancelarSolicitud(idSolicitud)

        val solicitud = viewModel.uiState.value.solicitudes.find { it.id == idSolicitud }
        assertEquals(EstadoSolicitud.CANCELADA, solicitud?.estado)

        val equipo = viewModel.uiState.value.equipos.find { it.id == 2 }
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun `TC-16 - Re-cancelar solicitud CANCELADA no produce cambios adicionales`() {
        val id = 1
        viewModel.cancelarSolicitud(id)
        val estadoIntermedio = viewModel.uiState.value.solicitudes.find { it.id == id }?.estado

        viewModel.cancelarSolicitud(id)

        val estadoFinal = viewModel.uiState.value.solicitudes.find { it.id == id }?.estado
        assertEquals(EstadoSolicitud.CANCELADA, estadoIntermedio)
        assertEquals(EstadoSolicitud.CANCELADA, estadoFinal)
    }

    @Test
    fun `Solicitud valida emite SolicitudRegistrada para que el NavHost vaya a Mis Solicitudes`() = runTest {
        viewModel.eventos.test {
            viewModel.seleccionarEquipoParaDetalle(viewModel.uiState.value.equipos.first { it.id == 3 })
            viewModel.onAmbienteChanged("Lab 305")
            viewModel.onPropositoChanged("Practica de Redes")
            viewModel.onDuracionChanged("4")

            viewModel.guardarSolicitud()

            val id = viewModel.uiState.value.solicitudes.last().id
            assertEquals(EventoPrestamo.SolicitudRegistrada(id), awaitItem())
        }
    }

    @Test
    fun `Validacion Ambiente - Ambiente vacio retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("")
        viewModel.onPropositoChanged("Proposito valido")
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
        assertEquals("El ambiente o destino es obligatorio.", viewModel.uiState.value.errorAmbiente)
    }

    @Test
    fun `Fallo del repositorio muestra mensaje de error y libera el boton`() = runTest {
        val repoFallido = mockk<PrestamoRepository> {
            every { equipos } returns MutableStateFlow(listOf(Equipo(1, "Multímetro", "Herramienta", EstadoEquipo.DISPONIBLE)))
            every { solicitudes } returns MutableStateFlow(emptyList())
            coEvery { crearSolicitud(any()) } returns Result.failure(IllegalStateException("El equipo no está disponible"))
        }
        val vm = PrestamoViewModel(repoFallido)
        vm.seleccionarEquipoParaDetalle(vm.uiState.value.equipos.first())
        vm.onAmbienteChanged("Lab 1")
        vm.onPropositoChanged("Proposito valido")
        vm.onDuracionChanged("2")

        vm.guardarSolicitud()

        val state = vm.uiState.value
        assertEquals("El equipo no está disponible", state.mensajeError)
        assertFalse(state.guardando)
        // Sin evento no hay navegación: el usuario sigue en el formulario
        vm.eventos.test { expectNoEvents() }
    }

    @Test
    fun `Detalle seleccionado se actualiza cuando cambia el estado del equipo`() {
        val equipo = viewModel.uiState.value.equipos.first { it.id == 2 }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        assertEquals(EstadoEquipo.RESERVADO, viewModel.uiState.value.equipoSeleccionado?.estado)

        viewModel.cancelarSolicitud(1) // libera el equipo 2

        assertEquals(EstadoEquipo.DISPONIBLE, viewModel.uiState.value.equipoSeleccionado?.estado)
    }

    @Test
    fun `TC-HU03-08 - El instructor no puede registrar solicitudes aunque llegue al formulario`() {
        val vm = PrestamoViewModel(repository, Usuario("u-instructor", "Instructor CTMA", "instructor@sena.edu.co", Rol.INSTRUCTOR))
        val antes = vm.uiState.value.solicitudes.size
        vm.seleccionarEquipoParaDetalle(vm.uiState.value.equipos.first { it.estado == EstadoEquipo.DISPONIBLE })
        vm.onAmbienteChanged("Lab 1")
        vm.onPropositoChanged("Proposito valido")
        vm.onDuracionChanged("2")

        assertFalse(vm.guardarSolicitud())
        assertEquals(antes, vm.uiState.value.solicitudes.size)
    }

    @Test
    fun `Mis Solicitudes del estudiante solo muestra las suyas y el instructor ve todas`() = runTest {
        val otro = Usuario("u-otro", "Otro Aprendiz", "otro@sena.edu.co", Rol.ESTUDIANTE)
        val vmOtro = PrestamoViewModel(repository, otro)
        vmOtro.seleccionarEquipoParaDetalle(vmOtro.uiState.value.equipos.first { it.id == 1 })
        vmOtro.onAmbienteChanged("Lab 2")
        vmOtro.onPropositoChanged("Proposito del otro aprendiz")
        vmOtro.onDuracionChanged("1")
        assertTrue(vmOtro.guardarSolicitud())

        assertEquals(listOf("u-otro"), vmOtro.uiState.value.solicitudes.map { it.usuarioId })
        assertTrue(viewModel.uiState.value.solicitudes.all { it.usuarioId == PrestamoViewModel.USUARIO_DEMO.id })
        val instructor = PrestamoViewModel(repository, Usuario("u-instructor", "Instructor", "i@sena.edu.co", Rol.INSTRUCTOR))
        assertEquals(3, instructor.uiState.value.solicitudes.size)
    }

    @Test
    fun `TC-HU07-04 - El aviso de sincronizacion se muestra y se puede descartar`() {
        val avisos = AvisosSincronizacion()
        val vm = PrestamoViewModel(repository, avisos = avisos)

        avisos.informar("No se encontró un recurso en el servidor.")
        assertEquals("No se encontró un recurso en el servidor.", vm.uiState.value.avisoSincronizacion)

        vm.descartarAvisoSincronizacion()
        assertNull(vm.uiState.value.avisoSincronizacion)
    }
}
