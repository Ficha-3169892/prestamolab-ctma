package com.example.prestamolab.viewmodel

import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.repository.InMemoryPrestamoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrestamoViewModelTest {

    private lateinit var repository: InMemoryPrestamoRepository
    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setUp() {
        repository = InMemoryPrestamoRepository()
        viewModel = PrestamoViewModel(repository)
    }

    @Test
    fun alIniciar_debeCargarLosEquipos() {
        val state = viewModel.uiState.value

        assertEquals(5, state.equipos.size)
    }

    @Test
    fun alIniciar_noDebeHaberSolicitudes() {
        val state = viewModel.uiState.value

        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun alIniciar_noDebeHaberMensaje() {
        val state = viewModel.uiState.value

        assertNull(state.mensaje)
    }

    @Test
    fun alIniciar_noDebeEstarGuardando() {
        val state = viewModel.uiState.value

        assertFalse(state.guardando)
    }

    @Test
    fun obtenerEquipo_cuandoExiste_debeRetornarlo() {
        val equipo = viewModel.obtenerEquipo(1)

        assertEquals("Multímetro Digital", equipo?.nombre)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun obtenerEquipo_cuandoNoExiste_debeRetornarNull() {
        val equipo = viewModel.obtenerEquipo(999)

        assertNull(equipo)
    }

    @Test
    fun obtenerSolicitud_cuandoNoExiste_debeRetornarNull() {
        val solicitud = viewModel.obtenerSolicitud(999)

        assertNull(solicitud)
    }

    @Test
    fun crearSolicitud_cuandoEquipoDisponible_debeRetornarTrue() {
        val resultado = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        assertTrue(resultado)
    }

    @Test
    fun crearSolicitud_debeAgregarLaSolicitudAlEstado() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        val solicitudes = viewModel.uiState.value.solicitudes

        assertEquals(1, solicitudes.size)
        assertEquals(1, solicitudes[0].id)
        assertEquals(1, solicitudes[0].equipoId)
        assertEquals(
            EstadoSolicitud.SOLICITADA,
            solicitudes[0].estado
        )
    }

    @Test
    fun crearSolicitud_debeEliminarEspaciosAlInicioYFinal() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "  Laboratorio 1  ",
            proposito = "  Práctica de electrónica  ",
            duracionHoras = 3
        )

        val solicitud = viewModel.uiState.value.solicitudes[0]

        assertEquals(
            "Laboratorio 1",
            solicitud.ambienteDestino
        )

        assertEquals(
            "Práctica de electrónica",
            solicitud.proposito
        )
    }

    @Test
    fun crearSolicitud_debeMostrarMensajeDeExito() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        assertEquals(
            "Solicitud creada correctamente",
            viewModel.uiState.value.mensaje
        )
    }

    @Test
    fun crearSolicitud_debeActualizarElEstadoDelEquipo() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        val equipo = viewModel.obtenerEquipo(1)

        assertEquals(
            EstadoEquipo.RESERVADO,
            equipo?.estado
        )
    }

    @Test
    fun crearSolicitud_cuandoEquipoNoExiste_debeRetornarFalse() {
        val resultado = viewModel.crearSolicitud(
            equipoId = 999,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        assertFalse(resultado)
    }

    @Test
    fun crearSolicitud_cuandoEquipoNoExiste_debeMostrarMensajeDeError() {
        viewModel.crearSolicitud(
            equipoId = 999,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        assertEquals(
            "El equipo no existe",
            viewModel.uiState.value.mensaje
        )
    }

    @Test
    fun crearSolicitud_cuandoEquipoNoDisponible_debeRetornarFalse() {
        val resultado = viewModel.crearSolicitud(
            equipoId = 3,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        assertFalse(resultado)
    }

    @Test
    fun crearSolicitud_cuandoEquipoNoDisponible_debeMostrarMensajeDeError() {
        viewModel.crearSolicitud(
            equipoId = 3,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        assertEquals(
            "El equipo no está disponible",
            viewModel.uiState.value.mensaje
        )
    }

    @Test
    fun crearSolicitud_dosVecesParaElMismoEquipo_debeFallarLaSegunda() {
        val primera = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio 1",
            proposito = "Primera práctica",
            duracionHoras = 2
        )

        val segunda = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio 2",
            proposito = "Segunda práctica",
            duracionHoras = 3
        )

        assertTrue(primera)
        assertFalse(segunda)
    }

    @Test
    fun crearSolicitud_dosVecesParaElMismoEquipo_debeMantenerUnaSolaSolicitud() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio 1",
            proposito = "Primera práctica",
            duracionHoras = 2
        )

        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio 2",
            proposito = "Segunda práctica",
            duracionHoras = 3
        )

        assertEquals(
            1,
            viewModel.uiState.value.solicitudes.size
        )
    }

    @Test
    fun crearSolicitud_debeGenerarIdsConsecutivos() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio 1",
            proposito = "Primera práctica",
            duracionHoras = 2
        )

        viewModel.cancelarSolicitud(1)

        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio 2",
            proposito = "Segunda práctica",
            duracionHoras = 3
        )

        val solicitudes = viewModel.uiState.value.solicitudes

        assertEquals(1, solicitudes[0].id)
        assertEquals(2, solicitudes[1].id)
    }

    @Test
    fun cancelarSolicitud_cuandoExiste_debeCambiarSuEstado() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        viewModel.cancelarSolicitud(1)

        val solicitud = viewModel.obtenerSolicitud(1)

        assertEquals(
            EstadoSolicitud.CANCELADA,
            solicitud?.estado
        )
    }

    @Test
    fun cancelarSolicitud_debeLiberarElEquipo() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        viewModel.cancelarSolicitud(1)

        val equipo = viewModel.obtenerEquipo(1)

        assertEquals(
            EstadoEquipo.DISPONIBLE,
            equipo?.estado
        )
    }

    @Test
    fun cancelarSolicitud_debeMostrarMensajeDeExito() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2
        )

        viewModel.cancelarSolicitud(1)

        assertEquals(
            "Solicitud cancelada correctamente",
            viewModel.uiState.value.mensaje
        )
    }

    @Test
    fun cancelarSolicitud_cuandoNoExiste_debeMostrarMensajeDeError() {
        viewModel.cancelarSolicitud(999)

        assertEquals(
            "La solicitud no existe",
            viewModel.uiState.value.mensaje
        )
    }

    @Test
    fun limpiarMensaje_debeEliminarElMensaje() {
        viewModel.cancelarSolicitud(999)

        assertTrue(
            viewModel.uiState.value.mensaje != null
        )

        viewModel.limpiarMensaje()

        assertNull(
            viewModel.uiState.value.mensaje
        )
    }
}
