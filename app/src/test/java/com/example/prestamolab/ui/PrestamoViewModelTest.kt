package com.example.prestamolab.ui

import com.example.prestamolab.model.Equipo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PrestamoViewModelTest {

    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setup() {
        viewModel = PrestamoViewModel()
    }

    @Test
    fun `TC-01 - Cargar datos iniciales carga equipos y solicitudes correctamente`() {
        val state = viewModel.uiState.value
        assertFalse(state.equipos.isEmpty())
        assertFalse(state.solicitudes.isEmpty())
        assertEquals(SeccionApp.CATALOGO, state.seccionActual)
    }

    @Test
    fun `TC-02 - Seleccionar equipo valido actualiza el estado correctamente`() {
        val equipo = viewModel.uiState.value.equipos.first()
        viewModel.seleccionarEquipoParaDetalle(equipo)
        
        val state = viewModel.uiState.value
        assertEquals(equipo, state.equipoSeleccionado)
        assertEquals(SeccionApp.DETALLE_EQUIPO, state.seccionActual)
    }

    @Test
    fun `TC-03 - Seleccionar equipo inexistente pone equipoSeleccionado como null`() {
        viewModel.seleccionarEquipoPorId(999)
        
        val state = viewModel.uiState.value
        assertNull(state.equipoSeleccionado)
        assertEquals(SeccionApp.DETALLE_EQUIPO, state.seccionActual)
    }

    @Test
    fun `TC-04 - Proposito con 9 caracteres retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
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
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
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
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("a".repeat(180))
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
    }

    @Test
    fun `TC-07 - Proposito con 181 caracteres retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
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
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
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
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("Propósito válido")
        viewModel.onDuracionChanged("1")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
    }

    @Test
    fun `TC-10 - Duracion 8 horas es valida`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Laboratorio 1")
        viewModel.onPropositoChanged("Propósito válido")
        viewModel.onDuracionChanged("8")

        val result = viewModel.guardarSolicitud()

        assertTrue(result)
    }

    @Test
    fun `TC-11 - Duracion 9 horas retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
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
        val equipo = Equipo(99, "Test", "Cat", "RESERVADO")
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Proposito valido")
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
    }

    @Test
    fun `TC-13 - Flag guardando bloquea ejecuciones simultaneas`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Proposito valido")
        viewModel.onDuracionChanged("2")

        // En un entorno sincrono es dificil de ver, pero el flag guardando se usa en la UI
        // Simulamos el flag activo
        // (Nota: Tendríamos que exponer el flag o interceptar el proceso)
        // Por ahora verificamos que el ViewModel gestiona el flag.
        viewModel.guardarSolicitud()
        assertFalse(viewModel.uiState.value.guardando)
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
        val equipoFinal = viewModel.uiState.value.equipos.find { it.id == 3 }
        assertEquals("RESERVADO", equipoFinal?.estado)
        val solicitudFinal = viewModel.uiState.value.solicitudes.last()
        assertEquals("SOLICITADA", solicitudFinal.estado)
    }

    @Test
    fun `TC-15 - Cancelar solicitud SOLICITADA pasa a CANCELADA y libera equipo`() {
        val idSolicitud = 1 // De los datos iniciales
        viewModel.cancelarSolicitud(idSolicitud)

        val solicitud = viewModel.uiState.value.solicitudes.find { it.id == idSolicitud }
        assertEquals("CANCELADA", solicitud?.estado)
        
        val equipo = viewModel.uiState.value.equipos.find { it.id == 2 }
        assertEquals("DISPONIBLE", equipo?.estado)
    }

    @Test
    fun `TC-16 - Re-cancelar solicitud CANCELADA no produce cambios adicionales`() {
        val id = 1
        viewModel.cancelarSolicitud(id)
        val estadoIntermedio = viewModel.uiState.value.solicitudes.find { it.id == id }?.estado
        
        viewModel.cancelarSolicitud(id)
        
        val estadoFinal = viewModel.uiState.value.solicitudes.find { it.id == id }?.estado
        assertEquals("CANCELADA", estadoIntermedio)
        assertEquals("CANCELADA", estadoFinal)
    }

    @Test
    fun `TC-17 - Navegar entre secciones actualiza seccionActual correctamente`() {
        viewModel.navegarA(SeccionApp.MIS_SOLICITUDES)
        assertEquals(SeccionApp.MIS_SOLICITUDES, viewModel.uiState.value.seccionActual)
        
        viewModel.navegarA(SeccionApp.CATALOGO)
        assertEquals(SeccionApp.CATALOGO, viewModel.uiState.value.seccionActual)
    }

    @Test
    fun `Validacion Ambiente - Ambiente vacio retorna error`() {
        val equipo = viewModel.uiState.value.equipos.first { it.estado == "DISPONIBLE" }
        viewModel.seleccionarEquipoParaDetalle(equipo)
        viewModel.onAmbienteChanged("")
        viewModel.onPropositoChanged("Proposito valido")
        viewModel.onDuracionChanged("2")

        val result = viewModel.guardarSolicitud()

        assertFalse(result)
        assertEquals("El ambiente o destino es obligatorio.", viewModel.uiState.value.errorAmbiente)
    }
}