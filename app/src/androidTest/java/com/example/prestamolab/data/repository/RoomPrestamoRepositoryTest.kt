package com.example.prestamolab.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaDevolucion
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.Ubicacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Locale

/** Reglas del repositorio sobre una base Room en memoria con los datos semilla. */
@RunWith(AndroidJUnit4::class)
class RoomPrestamoRepositoryTest {

    private lateinit var db: PrestamoLabDatabase
    private lateinit var repository: RoomPrestamoRepository

    private val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    private val instanteFijo = formato.parse("2026-09-24 08:00")!!.time

    private fun nueva(equipoId: Int) = NuevaSolicitud(
        equipoId = equipoId,
        solicitante = "Aprendiz Test",
        ambiente = "Lab 1",
        proposito = "Proposito valido",
        duracionHoras = 2
    )

    @Before
    fun setup() {
        db = PrestamoLabDatabase.construir(ApplicationProvider.getApplicationContext<Context>(), enMemoria = true)
        repository = RoomPrestamoRepository(db, reloj = { instanteFijo })
    }

    @After
    fun cerrar() {
        db.close()
    }

    @Test
    fun LaBaseNuevaCargaLosDatosSemilla() = runTest {
        assertEquals(5, repository.equipos.first().size)
        assertEquals(listOf(1, 2), repository.solicitudes.first().map { it.id })
        assertTrue(repository.devoluciones.first().isEmpty())
    }

    @Test
    fun CrearSolicitud_AsignaId_FechaLimite_YReservaElEquipo() = runTest {
        val solicitud = repository.crearSolicitud(nueva(1)).getOrThrow()

        assertEquals(3, solicitud.id) // la semilla tiene las solicitudes #1 y #2
        assertEquals(EstadoSolicitud.SOLICITADA, solicitud.estado)
        assertEquals("Lab 1", solicitud.ambiente)
        assertEquals("2026-09-24 08:00", solicitud.fechaInicio)
        // loans.return_date = inicio + duración pactada
        assertEquals("2026-09-24 10:00", solicitud.fechaFin)
        assertEquals(EstadoEquipo.RESERVADO, repository.obtenerEquipo(1)?.estado)
        assertEquals(solicitud, repository.solicitudes.first().last())
    }

    @Test
    fun CrearSolicitudSobreEquipoReservado_FallaSinCrearRegistros() = runTest {
        val antes = repository.solicitudes.first().size

        val resultado = repository.crearSolicitud(nueva(2))

        assertTrue(resultado.isFailure)
        assertEquals(antes, repository.solicitudes.first().size)
    }

    @Test
    fun CrearSolicitudSobreEquipoInexistente_Falla() = runTest {
        assertTrue(repository.crearSolicitud(nueva(999)).exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun CancelarSolicitudInexistente_Falla() = runTest {
        assertTrue(repository.cancelarSolicitud(999).isFailure)
    }

    @Test
    fun Cancelar_EsIdempotenteYLiberaElEquipo() = runTest {
        assertTrue(repository.cancelarSolicitud(1).isSuccess)
        assertTrue(repository.cancelarSolicitud(1).isSuccess)

        assertEquals(EstadoSolicitud.CANCELADA, repository.solicitudes.first().first { it.id == 1 }.estado)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.obtenerEquipo(2)?.estado)
    }

    @Test
    fun CancelarUnPrestamoPrestado_FallaYNoLiberaElEquipo() = runTest {
        assertTrue(repository.cancelarSolicitud(2).isFailure)
        assertEquals(EstadoEquipo.PRESTADO, repository.obtenerEquipo(5)?.estado)
    }

    @Test
    fun TC_HU05_03_Devolucion_GuardaReturnEntityConCondicionFechaYUbicacion() = runTest {
        val ubicacion = Ubicacion(latitud = 6.2518, longitud = -75.5636, precisionMetros = 8f)

        val devolucion = repository.registrarDevolucion(
            NuevaDevolucion(2, CondicionEquipo.CON_NOVEDAD, "  Cable suelto  ", ubicacion)
        ).getOrThrow()

        val guardada = repository.devoluciones.first().single()
        assertEquals(devolucion, guardada)
        assertEquals(2, guardada.solicitudId)
        assertEquals(CondicionEquipo.CON_NOVEDAD, guardada.condicion)
        assertEquals("Cable suelto", guardada.observacion)
        assertEquals("2026-09-24 08:00", guardada.fechaDevolucion)
        assertEquals(6.2518, guardada.latitud!!, 0.0)
        assertEquals(-75.5636, guardada.longitud!!, 0.0)
        assertEquals(EstadoSolicitud.DEVUELTO, repository.solicitudes.first().first { it.id == 2 }.estado)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.obtenerEquipo(5)?.estado)
    }

    @Test
    fun DevolucionSinUbicacion_GuardaCoordenadasNulas() = runTest {
        repository.registrarDevolucion(NuevaDevolucion(2, CondicionEquipo.BUENO, "", ubicacion = null)).getOrThrow()

        val guardada = repository.devoluciones.first().single()
        assertNull(guardada.latitud)
        assertNull(guardada.longitud)
    }

    @Test
    fun TC_HU05_05_DevolverUnPrestamoYaDevuelto_SeRechazaSinCambios() = runTest {
        val nueva = NuevaDevolucion(2, CondicionEquipo.BUENO, "", ubicacion = null)
        assertTrue(repository.registrarDevolucion(nueva).isSuccess)

        val segunda = repository.registrarDevolucion(nueva)

        assertTrue(segunda.isFailure)
        assertEquals(1, repository.devoluciones.first().size)
        assertEquals(EstadoSolicitud.DEVUELTO, repository.solicitudes.first().first { it.id == 2 }.estado)
    }

    @Test
    fun DevolverUnaSolicitudNoEntregada_SeRechaza() = runTest {
        val resultado = repository.registrarDevolucion(NuevaDevolucion(1, CondicionEquipo.BUENO, "", null))

        assertTrue(resultado.isFailure)
        assertEquals(EstadoEquipo.RESERVADO, repository.obtenerEquipo(2)?.estado)
    }

    @Test
    fun TC_HU06_03_ElFlujoDeEquiposEmiteLosCambiosDeRoom() = runTest {
        repository.equipos.test {
            assertEquals(EstadoEquipo.DISPONIBLE, awaitItem().first { it.id == 1 }.estado)
            repository.crearSolicitud(nueva(1))
            assertEquals(EstadoEquipo.RESERVADO, awaitItem().first { it.id == 1 }.estado)
        }
    }

    @Test
    fun Reiniciar_RestauraLaSemillaYLosIds() = runTest {
        repository.crearSolicitud(nueva(1)).getOrThrow()
        repository.crearSolicitud(nueva(3)).getOrThrow()

        db.reiniciar()

        assertEquals(listOf(1, 2), repository.solicitudes.first().map { it.id })
        assertEquals(3, repository.crearSolicitud(nueva(1)).getOrThrow().id)
    }
}
