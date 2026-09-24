package com.example.prestamolab.data.repository

import app.cash.turbine.test
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaSolicitud
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryPrestamoRepositoryTest {

    private lateinit var repository: InMemoryPrestamoRepository

    private fun nueva(equipoId: Int) = NuevaSolicitud(
        equipoId = equipoId,
        solicitante = "Aprendiz Test",
        ambiente = "Lab 1",
        proposito = "Proposito valido",
        duracionHoras = 2
    )

    @Before
    fun setup() {
        repository = InMemoryPrestamoRepository()
    }

    @Test
    fun `crear solicitud asigna id incremental, conserva datos y reserva el equipo`() = runTest {
        val solicitud = repository.crearSolicitud(nueva(1)).getOrThrow()

        assertEquals(2, solicitud.id)
        assertEquals(EstadoSolicitud.SOLICITADA, solicitud.estado)
        assertEquals("Lab 1", solicitud.ambiente)
        assertEquals(EstadoEquipo.RESERVADO, repository.obtenerEquipo(1)?.estado)
    }

    @Test
    fun `crear solicitud sobre equipo RESERVADO falla sin crear registros`() = runTest {
        val antes = repository.solicitudes.value.size

        val resultado = repository.crearSolicitud(nueva(2))

        assertTrue(resultado.isFailure)
        assertEquals(antes, repository.solicitudes.value.size)
    }

    @Test
    fun `crear solicitud sobre equipo inexistente falla`() = runTest {
        assertTrue(repository.crearSolicitud(nueva(999)).exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `cancelar solicitud inexistente falla`() = runTest {
        assertTrue(repository.cancelarSolicitud(999).isFailure)
    }

    @Test
    fun `cancelar es idempotente`() = runTest {
        assertTrue(repository.cancelarSolicitud(1).isSuccess)
        assertTrue(repository.cancelarSolicitud(1).isSuccess)
        assertEquals(EstadoSolicitud.CANCELADA, repository.solicitudes.value.first { it.id == 1 }.estado)
    }

    @Test
    fun `el flujo de equipos emite el cambio de disponibilidad`() = runTest {
        repository.equipos.test {
            assertEquals(EstadoEquipo.DISPONIBLE, awaitItem().first { it.id == 1 }.estado)
            repository.crearSolicitud(nueva(1))
            assertEquals(EstadoEquipo.RESERVADO, awaitItem().first { it.id == 1 }.estado)
        }
    }
}
