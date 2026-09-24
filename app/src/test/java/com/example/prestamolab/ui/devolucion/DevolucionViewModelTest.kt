package com.example.prestamolab.ui.devolucion

import com.example.prestamolab.data.location.UbicacionNoDisponibleException
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.testutil.FakeLocationProvider
import com.example.prestamolab.testutil.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DevolucionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: InMemoryPrestamoRepository
    private lateinit var ubicacion: FakeLocationProvider

    // Datos semilla: #2 está PRESTADO (Kit Arduino Uno, equipo 5); #1 está SOLICITADA
    private val prestamoPrestado = 2
    private val solicitudPendiente = 1

    @Before
    fun setup() {
        repository = InMemoryPrestamoRepository(ahora = { "2026-09-23 10:30" })
        ubicacion = FakeLocationProvider()
    }

    private fun viewModel(id: Int = prestamoPrestado) = DevolucionViewModel(id, repository, ubicacion)

    @Test
    fun `Carga el prestamo con el nombre del equipo`() {
        val state = viewModel().uiState.value

        assertFalse(state.cargando)
        assertEquals(prestamoPrestado, state.solicitud?.id)
        assertEquals("Kit Arduino Uno", state.nombreEquipo)
        assertTrue(state.puedeDevolverse)
    }

    @Test
    fun `TC-HU05-01 - Un prestamo que no esta PRESTADO no se puede devolver`() {
        val vm = viewModel(solicitudPendiente)
        vm.onCondicionSeleccionada(CondicionEquipo.BUENO)

        vm.registrarDevolucion()

        assertFalse(vm.uiState.value.puedeDevolverse)
        assertTrue(repository.devoluciones.value.isEmpty())
    }

    @Test
    fun `TC-HU05-02 - Devolver un prestamo PRESTADO lo marca DEVUELTO y libera el equipo`() {
        val vm = viewModel()
        vm.onCondicionSeleccionada(CondicionEquipo.BUENO)

        vm.registrarDevolucion()

        assertTrue(vm.uiState.value.completada)
        assertEquals(EstadoSolicitud.DEVUELTO, repository.solicitudes.value.first { it.id == prestamoPrestado }.estado)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.equipos.value.first { it.id == 5 }.estado)
    }

    @Test
    fun `TC-HU05-03 - Se guarda la devolucion con condicion, observacion y fecha`() {
        val vm = viewModel()
        vm.onCondicionSeleccionada(CondicionEquipo.CON_NOVEDAD)
        vm.onObservacionChanged("  Falta un cable USB  ")

        vm.registrarDevolucion()

        val devolucion = repository.devoluciones.value.single()
        assertEquals(prestamoPrestado, devolucion.solicitudId)
        assertEquals(CondicionEquipo.CON_NOVEDAD, devolucion.condicion)
        assertEquals("Falta un cable USB", devolucion.observacion)
        assertEquals("2026-09-23 10:30", devolucion.fechaDevolucion)
    }

    @Test
    fun `TC-HU05-04 - Equipo Danado exige observacion de al menos 10 caracteres`() {
        val vm = viewModel()
        vm.onCondicionSeleccionada(CondicionEquipo.DANADO)
        vm.onObservacionChanged("roto")

        vm.registrarDevolucion()

        assertEquals("Describe el daño (mínimo 10 caracteres).", vm.uiState.value.errorObservacion)
        assertTrue(repository.devoluciones.value.isEmpty())

        vm.onObservacionChanged("Pantalla rota")
        vm.registrarDevolucion()

        assertTrue(vm.uiState.value.completada)
    }

    @Test
    fun `La condicion del equipo es obligatoria`() {
        val vm = viewModel()

        vm.registrarDevolucion()

        assertEquals("Selecciona el estado del equipo.", vm.uiState.value.errorCondicion)
        assertTrue(repository.devoluciones.value.isEmpty())
    }

    @Test
    fun `TC-HU13-02 - Capturar ubicacion muestra las coordenadas y el mensaje de exito`() {
        val vm = viewModel()

        vm.capturarUbicacion()

        val state = vm.uiState.value
        assertEquals(FakeLocationProvider.UBICACION_CTMA, state.ubicacion)
        assertEquals("Ubicación capturada correctamente", state.mensajeUbicacion)
        assertFalse(state.capturandoUbicacion)
    }

    @Test
    fun `TC-HU13-04 - La devolucion guarda latitud y longitud capturadas`() {
        val vm = viewModel()
        vm.capturarUbicacion()
        vm.onCondicionSeleccionada(CondicionEquipo.BUENO)

        vm.registrarDevolucion()

        val devolucion = repository.devoluciones.value.single()
        assertEquals(6.2518, devolucion.latitud!!, 0.0)
        assertEquals(-75.5636, devolucion.longitud!!, 0.0)
    }

    @Test
    fun `TC-HU13-05 - Con permiso denegado la devolucion se registra sin coordenadas`() {
        val vm = viewModel()
        vm.onPermisoUbicacionDenegado()
        vm.onCondicionSeleccionada(CondicionEquipo.BUENO)

        vm.registrarDevolucion()

        assertEquals(
            "Permiso de ubicación denegado. La devolución se registrará sin coordenadas.",
            vm.uiState.value.mensajeUbicacion
        )
        val devolucion = repository.devoluciones.value.single()
        assertNull(devolucion.latitud)
        assertNull(devolucion.longitud)
        assertEquals(0, ubicacion.llamadas)
    }

    @Test
    fun `TC-HU13-05 - GPS desactivado muestra aviso y permite devolver sin coordenadas`() {
        ubicacion.resultado = Result.failure(UbicacionNoDisponibleException())
        val vm = viewModel()

        vm.capturarUbicacion()
        vm.onCondicionSeleccionada(CondicionEquipo.BUENO)
        vm.registrarDevolucion()

        assertNull(vm.uiState.value.ubicacion)
        assertEquals(UbicacionNoDisponibleException().message, vm.uiState.value.mensajeUbicacion)
        assertNull(repository.devoluciones.value.single().latitud)
    }

    @Test
    fun `Prestamo inexistente muestra estado vacio sin fallar`() {
        val state = viewModel(999).uiState.value

        assertFalse(state.cargando)
        assertNull(state.solicitud)
        assertFalse(state.puedeDevolverse)
    }
}
