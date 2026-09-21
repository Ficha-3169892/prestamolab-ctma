package com.example.prestamolab.repository

import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryPrestamoRepositoryTest {

    private lateinit var repository: InMemoryPrestamoRepository

    @Before
    fun setUp() {
        repository = InMemoryPrestamoRepository()
    }

    @Test
    fun obtenerEquipos_debeRetornarCincoEquipos() = runBlocking {
        val equipos = repository.obtenerEquipos().first()
        assertEquals(5, equipos.size)
    }

    @Test
    fun obtenerEquipo_cuandoExiste_debeRetornarlo() = runBlocking {
        val equipo = repository.obtenerEquipo(1)
        assertEquals("Multímetro Digital", equipo?.nombre)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun obtenerEquipo_cuandoNoExiste_debeRetornarNull() = runBlocking {
        val equipo = repository.obtenerEquipo(999)
        assertNull(equipo)
    }

    @Test
    fun obtenerSolicitudes_inicialmenteDebeEstarVacio() = runBlocking {
        val solicitudes = repository.obtenerSolicitudes().first()
        assertTrue(solicitudes.isEmpty())
    }

    @Test
    fun crearSolicitud_cuandoEquipoDisponible_debeCrearSolicitud() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(solicitud)

        assertTrue(resultado.isSuccess)
        assertEquals(1, repository.obtenerSolicitudes().first().size)
        assertEquals(solicitud, repository.obtenerSolicitud(1))
    }

    @Test
    fun crearSolicitud_debeCambiarEquipoADReservado() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        repository.crearSolicitud(solicitud)
        val equipo = repository.obtenerEquipo(1)
        assertEquals(EstadoEquipo.RESERVADO, equipo?.estado)
    }

    @Test
    fun crearSolicitud_cuandoEquipoNoExiste_debeFallar() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 999,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(solicitud)

        assertTrue(resultado.isFailure)
        assertEquals("El equipo no existe", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun crearSolicitud_cuandoEquipoNoDisponible_debeFallar() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 3,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(solicitud)

        assertTrue(resultado.isFailure)
        assertEquals("El equipo no está disponible", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun crearSolicitud_cuandoYaExisteSolicitudActiva_debeFallar() = runBlocking {
        val primeraSolicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Primera práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        repository.crearSolicitud(primeraSolicitud)

        val segundaSolicitud = SolicitudPrestamo(
            id = 2,
            equipoId = 1,
            ambienteDestino = "Otro laboratorio",
            proposito = "Segunda práctica",
            duracionHoras = 3,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(segundaSolicitud)

        assertTrue(resultado.isFailure)
        assertEquals("Ya existe una solicitud activa para este equipo", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cancelarSolicitud_cuandoExisteYEstaSolicitada_debeCancelarla() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        repository.crearSolicitud(solicitud)
        val resultado = repository.cancelarSolicitud(1)

        assertTrue(resultado.isSuccess)
        assertEquals(EstadoSolicitud.CANCELADA, repository.obtenerSolicitud(1)?.estado)
    }

    @Test
    fun cancelarSolicitud_debeLiberarElEquipo() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        repository.crearSolicitud(solicitud)
        repository.cancelarSolicitud(1)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.obtenerEquipo(1)?.estado)
    }

    @Test
    fun cancelarSolicitud_cuandoNoExiste_debeFallar() = runBlocking {
        val resultado = repository.cancelarSolicitud(999)
        assertTrue(resultado.isFailure)
        assertEquals("La solicitud no existe", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cancelarSolicitud_cuandoYaEstaCancelada_debeFallar() = runBlocking {
        val solicitud = SolicitudPrestamo(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Laboratorio",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        repository.crearSolicitud(solicitud)
        repository.cancelarSolicitud(1)
        val resultado = repository.cancelarSolicitud(1)

        assertTrue(resultado.isFailure)
        assertEquals("Solo se pueden cancelar solicitudes SOLICITADA", resultado.exceptionOrNull()?.message)
    }
}
