package com.example.prestamolab.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.local.CatalogoInicial
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.remote.ActividadRemota
import com.example.prestamolab.data.remote.DevolucionRemota
import com.example.prestamolab.data.remote.EquipoRemoto
import com.example.prestamolab.data.remote.EvidenciaRemota
import com.example.prestamolab.data.remote.PrestamosRemoteDataSource
import com.example.prestamolab.data.remote.FechasSupabase
import com.example.prestamolab.data.remote.PrestamoRemoto
import com.example.prestamolab.data.remote.SupabaseHttpException
import com.example.prestamolab.data.repository.RoomActividadRepository
import com.example.prestamolab.data.repository.RoomEvidenciaRepository
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.DatosActividad
import com.example.prestamolab.model.EtapaEvidencia
import com.example.prestamolab.model.ReglasActividad
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
        assertEquals(EstadoEquipo.RESERVADO.name, remoto.estadosEnviados[enviado.equipoId])
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
    fun LaRevisionDelInstructorSeEnviaConElEstadoDelEquipo() = runTest {
        repository.rechazarSolicitud(1, instructor.id, "Equipo en calibración").getOrThrow()

        sincronizador.sincronizar(instructor)

        val enviado = remoto.prestamos.single { it.id.endsWith("001") }
        assertEquals("RECHAZADA", enviado.estado)
        assertEquals(instructor.id, enviado.revisadoPor)
        assertEquals("Equipo en calibración", enviado.motivoRechazo)
        // El instructor envía el equipo completo (upsert), no solo el estado
        assertEquals("DISPONIBLE", remoto.equipos.single { it.id == CatalogoInicial.remoteIdPorIdLocal.getValue(2) }.estado)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.loanDao().obtener(1)?.syncStatus)
    }

    @Test
    fun ElEstudianteRecibeElMotivoDeRechazo() = runTest {
        remoto.prestamos += PrestamoRemoto(
            "5eed0000-0000-4000-8000-000000000001", estudiante.id, CatalogoInicial.remoteIdPorIdLocal.getValue(2),
            "RECHAZADA", "2026-09-02T13:00:00+00:00", "2026-09-02T15:00:00+00:00", "Laboratorio 302",
            "Práctica de señales", 2, revisadoPor = instructor.id, motivoRechazo = "Equipo en calibración"
        )

        sincronizador.sincronizar(estudiante)

        val solicitud = repository.solicitudes.first().first { it.id == 1 }
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.estado)
        assertEquals("Equipo en calibración", solicitud.motivoRechazo)
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

        assertEquals(1, (resultado as ResultadoSincronizacion.Exito).rechazados)
        assertEquals(EstadoSincronizacion.ERROR, db.loanDao().obtener(rechazada.id)!!.syncStatus)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.loanDao().obtener(aceptada.id)!!.syncStatus)
    }

    @Test
    fun UnRegistroEnErrorNoSePisaConLaVersionRemota() = runTest {
        // Caso real del 2026-09-24: Supabase rechazó el cambio a DEVUELTO y la recepción lo revertía
        repository.registrarDevolucion(NuevaDevolucion(2, CondicionEquipo.BUENO, "", null)).getOrThrow()
        remoto.prestamoRechazado = "5eed0000-0000-4000-8000-000000000002"
        remoto.errorDeRechazo = SupabaseHttpException(400, "null value in column \"user_role\"")
        remoto.prestamos += PrestamoRemoto(
            "5eed0000-0000-4000-8000-000000000002", estudiante.id, CatalogoInicial.remoteIdPorIdLocal.getValue(5),
            "PRESTADO", "2026-09-03T13:00:00+00:00", "2026-09-03T17:00:00+00:00", "Ambiente de Electrónica",
            "Prototipo de sensores IoT", 4
        )

        sincronizador.sincronizar(estudiante)

        val local = db.loanDao().obtener(2)!!
        assertEquals(EstadoSolicitud.DEVUELTO, local.status)
        assertEquals(EstadoSincronizacion.ERROR, local.syncStatus)
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

    @Test
    fun ElInstructorEnviaElEquipoNuevoCompleto() = runTest {
        val nuevo = repository.registrarEquipo("Proyector Epson", "Audiovisual").getOrThrow()
        val remoteId = db.equipmentDao().obtener(nuevo.id)!!.remoteId

        sincronizador.sincronizar(instructor)

        assertTrue(remoto.equipos.contains(EquipoRemoto(remoteId, "Proyector Epson", "Audiovisual", "DISPONIBLE")))
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.equipmentDao().obtener(nuevo.id)?.syncStatus)
    }

    @Test
    fun ElEstudianteSoloEnviaElEstadoDelEquipo() = runTest {
        solicitar(1)

        sincronizador.sincronizar(estudiante)

        assertEquals("RESERVADO", remoto.estadosEnviados[CatalogoInicial.remoteIdPorIdLocal.getValue(1)])
        // No se usó el upsert completo, que podría revertir un cambio de nombre del instructor
        assertTrue(remoto.equipos.none { it.id == CatalogoInicial.remoteIdPorIdLocal.getValue(1) })
    }

    @Test
    fun LaEliminacionSeEnviaYBorraElEquipoLocal() = runTest {
        val remoteId = CatalogoInicial.remoteIdPorIdLocal.getValue(3)
        remoto.equipos += equipoRemoto(3, EstadoEquipo.DISPONIBLE)
        repository.eliminarEquipo(3).getOrThrow()

        sincronizador.sincronizar(instructor)

        assertEquals(listOf(remoteId), remoto.equiposEliminados)
        assertNull(db.equipmentDao().obtenerPorRemoteId(remoteId))
    }

    @Test
    fun SiSupabaseRechazaLaEliminacion_ElEquipoVuelve() = runTest {
        remoto.equipos += equipoRemoto(3, EstadoEquipo.DISPONIBLE, "Cautín Estación de Soldadura")
        remoto.errorAlEliminarEquipo = SupabaseHttpException(409, "loans_equipment_id_fkey")
        repository.eliminarEquipo(3).getOrThrow()

        val resultado = sincronizador.sincronizar(instructor)

        assertEquals(1, (resultado as ResultadoSincronizacion.Exito).rechazados)
        assertNotNull(repository.obtenerEquipo(3))
    }

    @Test
    fun UnEquipoEliminadoEnSupabaseDesapareceDelTelefono() = runTest {
        // Supabase solo conserva los equipos 1, 2, 4 y 5: el 3 lo eliminó otro dispositivo
        listOf(1, 2, 4, 5).forEach { remoto.equipos += equipoRemoto(it, EstadoEquipo.DISPONIBLE) }

        sincronizador.sincronizar(estudiante)

        assertNull(repository.obtenerEquipo(3))
        assertEquals(listOf(1, 2, 4, 5), repository.equipos.first().map { it.id })
    }

    @Test
    fun UnEquipoConPrestamosNoSeBorraAunqueFalteEnSupabase() = runTest {
        sincronizador.sincronizar(estudiante)

        // Los equipos 2 y 5 tienen préstamos: se conservan para no perder el historial
        assertNotNull(repository.obtenerEquipo(2))
        assertNotNull(repository.obtenerEquipo(5))
    }

    private fun actividades() = RoomActividadRepository(db, reloj = { fechaFija("2026-09-24 08:00") })

    private fun fechaFija(texto: String) = ReglasActividad.aInstante(texto)!!

    @Test
    fun ElInstructorEnviaLaActividadNuevaConFechaIso() = runTest {
        val repositorio = actividades()
        val creada = repositorio.crear(
            DatosActividad("Taller de soldadura", "SMD", "Lab 1", "2026-10-05 14:00"), instructor.id
        ).getOrThrow()

        sincronizador.sincronizar(instructor)

        val enviada = remoto.actividades.single { it.titulo == "Taller de soldadura" }
        assertEquals(fechas.aIso("2026-10-05 14:00"), enviada.fecha)
        assertEquals(instructor.id, enviada.instructorId)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.activityDao().obtener(creada.id)?.syncStatus)
    }

    @Test
    fun ElEstudianteNoEnviaActividades() = runTest {
        // Una actividad pendiente en el teléfono del estudiante no debería existir; si existe, no se envía
        db.activityDao().marcarEliminada(1)

        sincronizador.sincronizar(estudiante)

        assertTrue(remoto.actividadesEliminadas.isEmpty())
    }

    @Test
    fun LaEliminacionDeUnaActividadSeEnviaYLaBorraDelTelefono() = runTest {
        val remoteId = DatosSemilla.actividades.single().remoteId
        actividades().eliminar(1).getOrThrow()

        sincronizador.sincronizar(instructor)

        assertEquals(listOf(remoteId), remoto.actividadesEliminadas)
        assertNull(db.activityDao().obtenerPorRemoteId(remoteId))
    }

    @Test
    fun ElEstudianteRecibeLasActividadesYSeBorranLasQueYaNoExisten() = runTest {
        remoto.actividades += ActividadRemota(
            "a-nueva", "Taller de redes", "", "Lab 5", "2026-10-10T13:00:00+00:00", instructor.id
        )

        sincronizador.sincronizar(estudiante)

        val lista = actividades().actividades.first()
        // La semilla (SINCRONIZADA) ya no está en Supabase: se borra; la nueva llega con hora local
        assertEquals(listOf("Taller de redes"), lista.map { it.titulo })
        assertEquals(fechas.desdeIso("2026-10-10T13:00:00+00:00"), lista.single().fecha)
    }

    private val fotosLocales = mutableMapOf("content://prueba/foto1.jpg" to byteArrayOf(7, 7, 7))

    private fun sincronizadorConFotos() = SincronizadorPrestamos(db, remoto, fechas) { fotosLocales[it] }

    private suspend fun evidenciaDelPrestamo2() = RoomEvidenciaRepository(db)
        .registrar(2, EtapaEvidencia.ENTREGA, "content://prueba/foto1.jpg").getOrThrow()

    @Test
    fun TC_HU08_05_LaFotoSeSubeAStorageYSeGuardaSuUrlRemota() = runTest {
        val evidencia = evidenciaDelPrestamo2()
        val entidad = db.evidenceDao().obtener(evidencia.id)!!

        sincronizadorConFotos().sincronizar(estudiante)

        val ruta = "5eed0000-0000-4000-8000-000000000002/${entidad.remoteId}.jpg"
        assertArrayEquals(byteArrayOf(7, 7, 7), remoto.fotos[ruta])
        val remota = remoto.evidencias.single()
        assertEquals("https://storage.prueba/evidencias/$ruta", remota.urlFoto)
        assertEquals("5eed0000-0000-4000-8000-000000000002", remota.prestamoId)
        assertEquals("ENTREGA", remota.etapa)
        val guardada = db.evidenceDao().obtener(evidencia.id)!!
        assertEquals(remota.urlFoto, guardada.photoUrl)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, guardada.syncStatus)
    }

    @Test
    fun SiFallaElRegistro_LaFotoNoSeVuelveASubir() = runTest {
        val evidencia = evidenciaDelPrestamo2()
        // La foto sube, pero el registro en la tabla falla por un error temporal
        val sincronizador = sincronizadorConFotos()
        val remotoConFalloEnTabla = object : PrestamosRemoteDataSource by remoto {
            override suspend fun guardarEvidencia(evidencia: EvidenciaRemota) = throw SupabaseHttpException(503, "")
        }
        SincronizadorPrestamos(db, remotoConFalloEnTabla, fechas) { fotosLocales[it] }.sincronizar(estudiante)
        assertEquals(1, remoto.fotos.size)
        assertNotNull(db.evidenceDao().obtener(evidencia.id)!!.photoUrl)
        assertEquals(EstadoSincronizacion.PENDIENTE, db.evidenceDao().obtener(evidencia.id)!!.syncStatus)

        fotosLocales.clear() // si intentara subirla de nuevo, ya no encontraría el archivo
        sincronizador.sincronizar(estudiante)

        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.evidenceDao().obtener(evidencia.id)!!.syncStatus)
        assertEquals(1, remoto.evidencias.size)
    }

    @Test
    fun LaEvidenciaEsperaASuPrestamo() = runTest {
        // El préstamo 2 cambió en el teléfono y aún no está en Supabase
        db.loanDao().actualizarEstado(2, EstadoSolicitud.PRESTADO)
        remoto.prestamoRechazado = "5eed0000-0000-4000-8000-000000000002"
        remoto.errorDeRechazo = SupabaseHttpException(409, "conflicto")
        val evidencia = evidenciaDelPrestamo2()

        sincronizadorConFotos().sincronizar(estudiante)

        assertTrue(remoto.fotos.isEmpty())
        assertEquals(EstadoSincronizacion.PENDIENTE, db.evidenceDao().obtener(evidencia.id)!!.syncStatus)
    }

    @Test
    fun SiLaFotoYaNoExiste_LaEvidenciaQuedaEnError() = runTest {
        val evidencia = evidenciaDelPrestamo2()
        fotosLocales.clear()

        val resultado = sincronizadorConFotos().sincronizar(estudiante)

        assertEquals(1, (resultado as ResultadoSincronizacion.Exito).rechazados)
        assertEquals(EstadoSincronizacion.ERROR, db.evidenceDao().obtener(evidencia.id)!!.syncStatus)
    }

    @Test
    fun LaUbicacionDeLaEvidenciaLlegaASupabase() = runTest {
        val evidencia = evidenciaDelPrestamo2()
        RoomEvidenciaRepository(db).agregarUbicacion(evidencia.id, Ubicacion(6.2518, -75.5636, 12f))

        sincronizadorConFotos().sincronizar(estudiante)

        val remota = remoto.evidencias.single()
        assertEquals(6.2518, remota.latitud!!, 0.0)
        assertEquals(-75.5636, remota.longitud!!, 0.0)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, db.evidenceDao().obtener(evidencia.id)!!.syncStatus)
    }

    @Test
    fun SiLaUbicacionLlegaDespuesDeEnviar_SeVuelveAEnviar() = runTest {
        val evidencia = evidenciaDelPrestamo2()
        sincronizadorConFotos().sincronizar(estudiante)
        assertNull(remoto.evidencias.single().latitud)

        RoomEvidenciaRepository(db).agregarUbicacion(evidencia.id, Ubicacion(6.2518, -75.5636, 12f))
        sincronizadorConFotos().sincronizar(estudiante)

        assertEquals(6.2518, remoto.evidencias.single().latitud!!, 0.0)
        // La foto no se vuelve a subir: ya tenía su URL
        assertEquals(1, remoto.fotos.size)
    }
}
