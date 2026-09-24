package com.example.prestamolab.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
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
    private var cambiosLocales = 0
    private var siguienteUuid = 0

    private val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    private val instanteFijo = formato.parse("2026-09-24 08:00")!!.time

    private fun nueva(equipoId: Int) = NuevaSolicitud(
        equipoId = equipoId,
        usuarioId = "uuid-estudiante",
        solicitante = "Aprendiz Test",
        ambiente = "Lab 1",
        proposito = "Proposito valido",
        duracionHoras = 2
    )

    @Before
    fun setup() {
        db = PrestamoLabDatabase.construir(ApplicationProvider.getApplicationContext<Context>(), enMemoria = true)
        DatosSemilla.reiniciar(db)
        repository = RoomPrestamoRepository(
            db,
            reloj = { instanteFijo },
            generarRemoteId = { "uuid-${++siguienteUuid}" },
            alCambiarLocalmente = { cambiosLocales++ }
        )
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
    fun TC_HU06_02_SolicitudNueva_QuedaPendienteConUuidYPideSincronizar() = runTest {
        val solicitud = repository.crearSolicitud(nueva(1)).getOrThrow()

        val guardada = db.loanDao().obtener(solicitud.id)!!
        assertEquals(EstadoSincronizacion.PENDIENTE, guardada.syncStatus)
        assertEquals("uuid-1", guardada.remoteId)
        assertEquals("uuid-estudiante", guardada.userId)
        // El equipo reservado también debe llegar a Supabase
        assertEquals(EstadoSincronizacion.PENDIENTE, db.equipmentDao().obtener(1)!!.syncStatus)
        assertEquals(1, cambiosLocales)
    }

    @Test
    fun OperacionRechazada_NoPideSincronizar() = runTest {
        repository.crearSolicitud(nueva(2))

        assertEquals(0, cambiosLocales)
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
    fun DevolucionYaRecibidaDeOtroDispositivo_SeRechazaSinCerrarLaApp() = runTest {
        // Caso real del 2026-09-24: la devolución llegó de Supabase pero el préstamo seguía PRESTADO
        db.returnDao().insertar(
            com.example.prestamolab.data.local.entity.ReturnEntity(
                remoteId = "r-remota", loanId = 2, equipmentCondition = CondicionEquipo.BUENO, notes = "",
                returnDate = "2026-09-24 14:06", latitude = null, longitude = null,
                syncStatus = EstadoSincronizacion.SINCRONIZADO
            )
        )

        val resultado = repository.registrarDevolucion(NuevaDevolucion(2, CondicionEquipo.BUENO, "", null))

        assertEquals("Este préstamo ya tiene una devolución registrada", resultado.exceptionOrNull()?.message)
        assertEquals(EstadoSolicitud.PRESTADO, repository.solicitudes.first().first { it.id == 2 }.estado)
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

        DatosSemilla.reiniciar(db)

        assertEquals(listOf(1, 2), repository.solicitudes.first().map { it.id })
        assertEquals(3, repository.crearSolicitud(nueva(1)).getOrThrow().id)
    }

    @Test
    fun TC_HU14_02_AprobarSolicitud_EntregaElEquipoYQuedaPendienteDeEnviar() = runTest {
        repository.aprobarSolicitud(1, "uuid-instructor").getOrThrow()

        val prestamo = db.loanDao().obtener(1)!!
        assertEquals(EstadoSolicitud.PRESTADO, prestamo.status)
        assertEquals("uuid-instructor", prestamo.reviewedBy)
        assertNull(prestamo.rejectionReason)
        assertEquals(EstadoSincronizacion.PENDIENTE, prestamo.syncStatus)
        assertEquals(EstadoEquipo.PRESTADO, repository.obtenerEquipo(2)?.estado)
        assertEquals(1, cambiosLocales)
    }

    @Test
    fun TC_HU14_03_RechazarSolicitud_GuardaElMotivoYLiberaElEquipo() = runTest {
        repository.rechazarSolicitud(1, "uuid-instructor", " Equipo en calibración ").getOrThrow()

        val solicitud = repository.solicitudes.first().first { it.id == 1 }
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.estado)
        assertEquals("Equipo en calibración", solicitud.motivoRechazo)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.obtenerEquipo(2)?.estado)
    }

    @Test
    fun TC_HU14_04_AprobarUnaSolicitudCancelada_SeRechazaSinCambios() = runTest {
        repository.cancelarSolicitud(1).getOrThrow()
        val cambiosAntes = cambiosLocales

        val resultado = repository.aprobarSolicitud(1, "uuid-instructor")

        assertTrue(resultado.exceptionOrNull() is IllegalStateException)
        assertEquals(EstadoSolicitud.CANCELADA, db.loanDao().obtener(1)?.status)
        assertNull(db.loanDao().obtener(1)?.reviewedBy)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.obtenerEquipo(2)?.estado)
        assertEquals(cambiosAntes, cambiosLocales)
    }

    @Test
    fun RechazarSinMotivo_NoCambiaNada() = runTest {
        assertTrue(repository.rechazarSolicitud(1, "uuid-instructor", "  ").isFailure)

        assertEquals(EstadoSolicitud.SOLICITADA, db.loanDao().obtener(1)?.status)
        assertEquals(EstadoEquipo.RESERVADO, repository.obtenerEquipo(2)?.estado)
    }

    @Test
    fun TC_HU12_01_RegistrarEquipo_QuedaDisponibleEnElCatalogoYPendienteDeEnviar() = runTest {
        val equipo = repository.registrarEquipo(" Proyector Epson ", "Audiovisual").getOrThrow()

        assertEquals("Proyector Epson", equipo.nombre)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo.estado)
        assertTrue(repository.equipos.first().any { it.id == equipo.id })
        val entidad = db.equipmentDao().obtener(equipo.id)!!
        assertEquals(EstadoSincronizacion.PENDIENTE, entidad.syncStatus)
        assertEquals("uuid-1", entidad.remoteId)
        assertEquals(1, cambiosLocales)
    }

    @Test
    fun TC_HU12_02_DatosInvalidos_NoSeGuardan() = runTest {
        assertTrue(repository.registrarEquipo("", "Audiovisual").isFailure)
        assertTrue(repository.editarEquipo(1, "Multímetro", " ").isFailure)

        assertEquals(5, repository.equipos.first().size)
        assertEquals("Herramienta", repository.obtenerEquipo(1)?.categoria)
        assertEquals(0, cambiosLocales)
    }

    @Test
    fun TC_HU12_03_EditarEquipo_CambiaCatalogoYDetalle() = runTest {
        repository.editarEquipo(1, "Multímetro Fluke", "Medición").getOrThrow()

        assertEquals("Multímetro Fluke", repository.equipos.first().first { it.id == 1 }.nombre)
        val detalle = repository.obtenerEquipo(1)!!
        assertEquals("Medición", detalle.categoria)
        assertEquals(EstadoEquipo.DISPONIBLE, detalle.estado)
    }

    @Test
    fun TC_HU12_04_EliminarEquipoConPrestamoActivo_SeRechaza() = runTest {
        // Equipo 2: solicitud #1 SOLICITADA; equipo 5: préstamo #2 PRESTADO
        listOf(2, 5).forEach { id ->
            val resultado = repository.eliminarEquipo(id)

            assertEquals("No se puede eliminar: el equipo tiene un préstamo activo.", resultado.exceptionOrNull()?.message)
            assertNotNull(repository.obtenerEquipo(id))
        }
    }

    @Test
    fun EliminarEquipoSinPrestamos_DesapareceYQuedaPendienteDeEnviar() = runTest {
        repository.eliminarEquipo(3).getOrThrow()

        assertNull(repository.obtenerEquipo(3))
        assertTrue(repository.equipos.first().none { it.id == 3 })
        val pendiente = db.equipmentDao().pendientes().single()
        assertTrue(pendiente.deleted)
        assertEquals(3, pendiente.id)
    }
}
