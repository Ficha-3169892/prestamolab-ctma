package com.example.prestamolab.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.local.CatalogoInicial
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.remote.DevolucionRemota
import com.example.prestamolab.data.remote.EquipoRemoto
import com.example.prestamolab.data.remote.FechasSupabase
import com.example.prestamolab.data.remote.PrestamoRemoto
import com.example.prestamolab.data.remote.SupabaseHttpException
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaDevolucion
import com.example.prestamolab.model.NuevaSolicitud
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Ubicacion
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.FakePrestamosRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.TimeZone

/** Sincronización Room ↔ Supabase con Room en memoria y el servidor simulado (HU-07). */
@RunWith(AndroidJUnit4::class)
class SincronizadorPrestamosTest {

    private val estudiante = Usuario("uuid-estudiante", "Estudiante CTMA", "estudiante@sena.edu.co", Rol.ESTUDIANTE)
    private val instructor = Usuario("uuid-instructor", "Instructor CTMA", "instructor@sena.edu.co", Rol.INSTRUCTOR)
    private val fechas = FechasSupabase(TimeZone.getTimeZone("America/Bogota"))

    private lateinit var db: PrestamoLabDatabase
    private lateinit var remoto: FakePrestamosRemoteDataSource
    private lateinit var repository: RoomPrestamoRepository
    private lateinit var sincronizador: SincronizadorPrestamos

    @Before
    fun setup() {
        db = PrestamoLabDatabase.construir(ApplicationProvider.getApplicationContext<Context>(), enMemoria = true)
        DatosSemilla.reiniciar(db)
        remoto = FakePrestamosRemoteDataSource()
        repository = RoomPrestamoRepository(db)
        sincronizador = SincronizadorPrestamos(db, remoto, fechas)
    }

    @After
    fun cerrar() {
        db.close()
    }

    private suspend fun solicitar(equipoId: Int) = repository.crearSolicitud(
        NuevaSolicitud(equipoId, estudiante.id, estudiante.nombre, "Lab 1", "Practica de redes", 2)
    ).getOrThrow()

    private fun equipoRemoto(idLocal: Int, estado: EstadoEquipo, nombre: String = "Equipo $idLocal") =
        EquipoRemoto(CatalogoInicial.remoteIdPorIdLocal.getValue(idLocal), nombre, "Herramienta", estado.name)

    @Test
    fun TC_HU07_01_PendientesSeEnvianYQuedanSincronizados() = runTest {
        val solicitud = solicitar(1)
        val local = db.loanDao().obtener(solicitud.id)!!
        assertEquals(EstadoSincronizacion.PENDIENTE, local.syncStatus)

        val resultado = sincronizador.sincronizar(estudiante)

        assertEquals(2, (resultado as ResultadoSincronizacion.Exito).enviados) // préstamo y equipo reservado
        val enviado = remoto.prestamos.single { it.id == local.remoteId }
        assertEquals(estudiante.id, enviado.usuarioId)
        assertEquals(CatalogoInicial.remoteIdPorIdLocal[1], enviado.equipoId)
        assertEquals("SOLICITADA", enviado.estado)
        assertEquals(EstadoEquipo.RESERVADO.name, remoto.equipos.single { it.id == enviado.equipoId }.estado)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.loanDao().obtener(solicitud.id)!!.syncStatus)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.equipmentDao().obtener(1)!!.syncStatus)
    }

    @Test
    fun ReintentarUnEnvioNoDuplicaEnSupabase() = runTest {
        solicitar(1)
        sincronizador.sincronizar(estudiante)
        // Simula que el trabajo se interrumpió antes de marcarlo y se repite
        db.openHelper.writableDatabase.execSQL("UPDATE loans SET sync_status = 'PENDIENTE'")

        sincronizador.sincronizar(estudiante)

        assertEquals(3, remoto.prestamos.size) // 2 de la semilla + 1 nuevo, sin duplicados
    }

    @Test
    fun DevolucionConGpsSeEnviaDespuesDeSuPrestamo() = runTest {
        repository.registrarDevolucion(
            NuevaDevolucion(2, CondicionEquipo.BUENO, "", Ubicacion(6.2518, -75.5636, 8f))
        ).getOrThrow()

        sincronizador.sincronizar(estudiante)

        val devolucion = remoto.devoluciones.single()
        assertEquals("5eed0000-0000-4000-8000-000000000002", devolucion.prestamoId)
        assertEquals(6.2518, devolucion.latitud!!, 0.0)
        assertEquals("DEVUELTO", remoto.prestamos.single { it.id == devolucion.prestamoId }.estado)
        assertTrue(db.returnDao().pendientes().isEmpty())
    }

    @Test
    fun TC_HU07_02_DatosRemotosNuevosActualizanRoom() = runTest {
        remoto.equipos += equipoRemoto(2, EstadoEquipo.DISPONIBLE, "Osciloscopio 100MHz")
        remoto.equipos += EquipoRemoto("e-nuevo", "Proyector Epson", "Audiovisual", "DISPONIBLE")
        remoto.prestamos += PrestamoRemoto(
            "5eed0000-0000-4000-8000-000000000001", estudiante.id, CatalogoInicial.remoteIdPorIdLocal.getValue(2),
            "RECHAZADA", "2026-09-02T13:00:00+00:00", "2026-09-02T15:00:00+00:00", "Laboratorio 302",
            "Práctica de señales", 2, nombreSolicitante = "Estudiante CTMA"
        )

        val resultado = sincronizador.sincronizar(estudiante)

        assertTrue(resultado is ResultadoSincronizacion.Exito)
        val equipos = repository.equipos.first()
        assertTrue(equipos.any { it.nombre == "Proyector Epson" })
        assertEquals(EstadoEquipo.DISPONIBLE, equipos.first { it.id == 2 }.estado)
        val solicitud = repository.solicitudes.first().first { it.id == 1 }
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.estado)
        assertEquals("Estudiante CTMA", solicitud.solicitante)
        assertEquals("2026-09-02 08:00", solicitud.fechaInicio)
    }

    @Test
    fun LaRecepcionNoPisaCambiosLocalesPendientes() = runTest {
        repository.cancelarSolicitud(1).getOrThrow() // local: CANCELADA y equipo 2 DISPONIBLE, pendientes
        // Supabase todavía tiene la versión anterior, pero el envío ocurre primero
        remoto.prestamos += PrestamoRemoto(
            "5eed0000-0000-4000-8000-000000000001", estudiante.id, CatalogoInicial.remoteIdPorIdLocal.getValue(2),
            "SOLICITADA", "2026-09-02T13:00:00+00:00", "2026-09-02T15:00:00+00:00", "Laboratorio 302",
            "Práctica de señales", 2
        )

        sincronizador.sincronizar(estudiante)

        assertEquals(EstadoSolicitud.CANCELADA, repository.solicitudes.first().first { it.id == 1 }.estado)
        assertEquals("CANCELADA", remoto.prestamos.single { it.id.endsWith("001") }.estado)
    }

    @Test
    fun ElEstudianteSoloRecibeSusPrestamosYElInstructorTodos() = runTest {
        remoto.prestamos += PrestamoRemoto(
            "otro", "uuid-otro", CatalogoInicial.remoteIdPorIdLocal.getValue(3), "SOLICITADA",
            "2026-09-05T13:00:00+00:00", "2026-09-05T14:00:00+00:00", "Lab 9", "Proposito del otro", 1
        )
        remoto.equipos += equipoRemoto(3, EstadoEquipo.RESERVADO)

        sincronizador.sincronizar(estudiante)
        assertNull(db.loanDao().obtenerPorRemoteId("otro"))

        sincronizador.sincronizar(instructor)
        assertNotNull(db.loanDao().obtenerPorRemoteId("otro"))
        assertEquals(listOf(estudiante.id, null), remoto.filtrosDePrestamos)
    }

    @Test
    fun TC_HU07_03_Respuesta401DevuelveNoAutorizado() = runTest {
        solicitar(1)
        remoto.error = SupabaseHttpException(401, "Invalid API key")

        assertEquals(ResultadoSincronizacion.NoAutorizado, sincronizador.sincronizar(estudiante))
    }

    @Test
    fun TC_HU07_04_Respuesta404ConservaLosDatosLocales() = runTest {
        val solicitud = solicitar(1)
        remoto.error = SupabaseHttpException(404, "relation \"public.loans\" does not exist")

        val resultado = sincronizador.sincronizar(estudiante)

        assertTrue(resultado is ResultadoSincronizacion.RecursoNoEncontrado)
        assertEquals(3, repository.solicitudes.first().size)
        assertEquals(EstadoSincronizacion.PENDIENTE, db.loanDao().obtener(solicitud.id)!!.syncStatus)
    }

    @Test
    fun TC_HU07_05_Error5xxOTiempoAgotadoDejaLosRegistrosPendientes() = runTest {
        val solicitud = solicitar(1)

        for (error in listOf(SupabaseHttpException(503, "Service Unavailable"), SocketTimeoutException("timeout"))) {
            remoto.error = error
            val resultado = sincronizador.sincronizar(estudiante)

            assertTrue(resultado is ResultadoSincronizacion.ErrorTemporal)
            assertEquals(EstadoSincronizacion.PENDIENTE, db.loanDao().obtener(solicitud.id)!!.syncStatus)
        }
    }

    @Test
    fun SinRedAlRecibir_NoDejaRoomAMedias() = runTest {
        remoto.equipos += EquipoRemoto("e-nuevo", "Proyector Epson", "Audiovisual", "DISPONIBLE")
        remoto.error = IOException("sin red")

        sincronizador.sincronizar(estudiante)

        assertNull(db.equipmentDao().obtenerPorRemoteId("e-nuevo"))
    }

    @Test
    fun UnRegistroRechazadoQuedaEnErrorYLosDemasSeEnvian() = runTest {
        val rechazada = solicitar(1)
        val aceptada = solicitar(3)
        remoto.prestamoRechazado = db.loanDao().obtener(rechazada.id)!!.remoteId
        remoto.errorDeRechazo = SupabaseHttpException(400, "violates check constraint")

        val resultado = sincronizador.sincronizar(estudiante)

        assertTrue(resultado is ResultadoSincronizacion.Exito)
        assertEquals(EstadoSincronizacion.ERROR, db.loanDao().obtener(rechazada.id)!!.syncStatus)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.loanDao().obtener(aceptada.id)!!.syncStatus)
    }

    @Test
    fun UnCambioDuranteElEnvioSiguePendiente() = runTest {
        val solicitud = solicitar(1)
        val enviada = db.loanDao().obtener(solicitud.id)!!
        // El usuario cancela justo después de leer el pendiente y antes de marcarlo
        repository.cancelarSolicitud(solicitud.id)

        db.loanDao().marcarEnviado(enviada.id, enviada.status, EstadoSincronizacion.SINCRONIZADO)

        assertEquals(EstadoSincronizacion.PENDIENTE, db.loanDao().obtener(solicitud.id)!!.syncStatus)
    }

    @Test
    fun LaDevolucionRemotaSeGuardaConSuUbicacion() = runTest {
        remoto.prestamos += PrestamoRemoto(
            "5eed0000-0000-4000-8000-000000000002", estudiante.id, CatalogoInicial.remoteIdPorIdLocal.getValue(5),
            "DEVUELTO", "2026-09-03T13:00:00+00:00", "2026-09-03T17:00:00+00:00", "Ambiente de Electrónica",
            "Prototipo de sensores IoT", 4
        )
        remoto.devoluciones += DevolucionRemota(
            "r1", "5eed0000-0000-4000-8000-000000000002", "CON_NOVEDAD", "Cable suelto",
            "2026-09-03T16:30:00+00:00", 6.25, -75.56
        )

        sincronizador.sincronizar(estudiante)

        val devolucion = repository.devoluciones.first().single()
        assertEquals(2, devolucion.solicitudId)
        assertEquals(CondicionEquipo.CON_NOVEDAD, devolucion.condicion)
        assertEquals(6.25, devolucion.latitud!!, 0.0)
        assertEquals("2026-09-03 11:30", devolucion.fechaDevolucion)
    }
}
