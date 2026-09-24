package com.example.prestamolab.data.repository

import app.cash.turbine.test
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaDevolucion
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

        assertEquals(3, solicitud.id) // la semilla tiene las solicitudes #1 y #2
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
    fun `cancelar un prestamo PRESTADO falla y no libera el equipo`() = runTest {
        assertTrue(repository.cancelarSolicitud(2).isFailure)
        assertEquals(EstadoEquipo.PRESTADO, repository.obtenerEquipo(5)?.estado)
    }

    @Test
    fun `TC-HU05-05 - Devolver un prestamo ya DEVUELTO se rechaza sin cambios`() = runTest {
        val nueva = NuevaDevolucion(2, CondicionEquipo.BUENO, "", ubicacion = null)
        assertTrue(repository.registrarDevolucion(nueva).isSuccess)

        val segunda = repository.registrarDevolucion(nueva)

        assertTrue(segunda.isFailure)
        assertEquals(1, repository.devoluciones.value.size)
        assertEquals(EstadoSolicitud.DEVUELTO, repository.solicitudes.value.first { it.id == 2 }.estado)
    }

    @Test
    fun `devolver una solicitud que no fue entregada se rechaza`() = runTest {
        val resultado = repository.registrarDevolucion(NuevaDevolucion(1, CondicionEquipo.BUENO, "", null))

        assertTrue(resultado.isFailure)
        assertEquals(EstadoEquipo.RESERVADO, repository.obtenerEquipo(2)?.estado)
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
