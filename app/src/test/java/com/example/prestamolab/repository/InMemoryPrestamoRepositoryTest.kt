package com.example.prestamolab.repository

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPrestamoRepositoryTest {

    // TC-01: Obtener los equipos devuelve el catálogo con datos
    @Test
    fun obtenerEquiposDevuelveCatalogoConDatos() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        // Usamos .first() para leer el primer valor emitido por el Flow
        val equipos = repository.obtenerEquipos().first()

        assertTrue(equipos.isNotEmpty())
    }

    // TC-02: Obtener un equipo por ID devuelve el equipo correcto
    @Test
    fun obtenerEquipoConIdValidoDevuelveEquipoCorrecto() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val equipos = repository.obtenerEquipos().first()
        val equipoEsperado = equipos.first()

        val equipo = repository.obtenerEquipo(equipoEsperado.id).first()

        assertNotNull(equipo)
        assertEquals(equipoEsperado.id, equipo?.id)
        assertEquals(equipoEsperado.nombre, equipo?.nombre)
    }

    // TC-03: Obtener un equipo con ID inexistente devuelve null
    @Test
    fun obtenerEquipoConIdInexistenteDevuelveNull() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val equipo = repository.obtenerEquipo(-999).first()

        assertEquals(null, equipo)
    }

    // TC-12: No permitir crear una solicitud para equipos no disponibles
    @Test
    fun noSePuedeCrearSolicitudParaEquipoNoDisponible() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado != EstadoEquipo.DISPONIBLE }

        val solicitud = SolicitudPrestamo(
            id = 0,
            equipoId = equipo.id,
            ambienteDestino = "Laboratorio",
            proposito = "Intento de préstamo",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        // Las funciones de escritura ahora son "suspend", runBlocking nos permite llamarlas
        val resultado = repository.crearSolicitud(solicitud)

        assertTrue(resultado.isFailure)
        assertTrue(repository.obtenerSolicitudes().first().isEmpty())
    }

    // TC-14: Crear una solicitud válida
    @Test
    fun crearSolicitudValidaCreaSolicitudYReservaEquipo() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        val solicitud = SolicitudPrestamo(
            id = 0,
            equipoId = equipo.id,
            ambienteDestino = "Laboratorio",
            proposito = "Prueba de préstamo",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(solicitud)

        assertTrue(resultado.isSuccess)

        val solicitudes = repository.obtenerSolicitudes().first()
        assertEquals(1, solicitudes.size)

        val solicitudCreada = solicitudes.first()

        assertEquals(equipo.id, solicitudCreada.equipoId)
        assertEquals(EstadoSolicitud.SOLICITADA, solicitudCreada.estado)

        val equipoActualizado = repository.obtenerEquipo(equipo.id).first()

        assertEquals(EstadoEquipo.RESERVADO, equipoActualizado?.estado)
    }

    // TC-15: Cancelar una solicitud realizada
    @Test
    fun cancelarSolicitudSolicitadaLaMarcaComoCanceladaYLiberaEquipo() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        val solicitud = SolicitudPrestamo(
            id = 0,
            equipoId = equipo.id,
            ambienteDestino = "Laboratorio",
            proposito = "Prueba de cancelación",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        val crearResultado = repository.crearSolicitud(solicitud)
        assertTrue(crearResultado.isSuccess)

        val solicitudCreada = repository.obtenerSolicitudes().first().first()

        val cancelarResultado = repository.cancelarSolicitud(solicitudCreada.id)

        assertTrue(cancelarResultado.isSuccess)

        val solicitudActualizada = repository.obtenerSolicitud(solicitudCreada.id).first()

        assertEquals(EstadoSolicitud.CANCELADA, solicitudActualizada?.estado)

        val equipoActualizado = repository.obtenerEquipo(equipo.id).first()

        assertEquals(EstadoEquipo.DISPONIBLE, equipoActualizado?.estado)
    }

    // TC-16: Una solicitud CANCELADA no puede cancelarse nuevamente
    @Test
    fun noSePuedeCancelarUnaSolicitudYaCancelada() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val equipos = repository.obtenerEquipos().first()
        val equipo = equipos.first { it.estado == EstadoEquipo.DISPONIBLE }

        val solicitud = SolicitudPrestamo(
            id = 0,
            equipoId = equipo.id,
            ambienteDestino = "Laboratorio",
            proposito = "Prueba de doble cancelación",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA
        )

        // Crear la solicitud
        val crearResultado = repository.crearSolicitud(solicitud)
        assertTrue(crearResultado.isSuccess)

        val solicitudCreada = repository.obtenerSolicitudes().first().first()

        // Primera cancelación: debe funcionar
        val primeraCancelacion = repository.cancelarSolicitud(solicitudCreada.id)

        assertTrue(primeraCancelacion.isSuccess)

        // Segunda cancelación: debe ser rechazada
        val segundaCancelacion = repository.cancelarSolicitud(solicitudCreada.id)

        assertTrue(segundaCancelacion.isFailure)

        // El estado debe seguir siendo CANCELADA
        val solicitudFinal = repository.obtenerSolicitud(solicitudCreada.id).first()

        assertEquals(EstadoSolicitud.CANCELADA, solicitudFinal?.estado)
    }

    // No permitir cancelar solicitudes inexistentes
    @Test
    fun cancelarSolicitudInexistenteFalla() = runBlocking {
        val repository = InMemoryPrestamoRepository()

        val resultado = repository.cancelarSolicitud(-999)

        assertTrue(resultado.isFailure)
    }
}
