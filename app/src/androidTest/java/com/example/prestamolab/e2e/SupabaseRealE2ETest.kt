package com.example.prestamolab.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.prestamolab.BuildConfig
import com.example.prestamolab.data.auth.CampoIdentificador
import com.example.prestamolab.data.auth.HashUtils
import com.example.prestamolab.data.auth.SupabaseUsuariosDataSource
import com.example.prestamolab.data.auth.UsuarioRemoto
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.remote.ActividadRemota
import com.example.prestamolab.data.remote.SupabaseHttpException
import com.example.prestamolab.data.remote.SupabasePrestamosDataSource
import com.example.prestamolab.data.remote.SupabaseRestClient
import com.example.prestamolab.data.repository.RoomActividadRepository
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.data.sync.ResultadoSincronizacion
import com.example.prestamolab.data.sync.SincronizadorPrestamos
import com.example.prestamolab.model.DatosActividad
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Prueba de punta a punta contra el Supabase REAL (HU-11, HU-12 y la seguridad de 009), con las clases reales de
 * la app: login por RPC, Room, repositorios y sincronización. No usa la UI.
 *
 * Solo se ejecuta si se pide:
 *   adb shell am instrument -w -r -e e2e true -e class com.example.prestamolab.e2e.SupabaseRealE2ETest \
 *       com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner
 * Crea un equipo y una actividad con nombres únicos y los elimina al terminar, aunque la prueba falle.
 */
@RunWith(AndroidJUnit4::class)
class SupabaseRealE2ETest {

    private val contexto = ApplicationProvider.getApplicationContext<Context>()
    private val marca = "E2E " + UUID.randomUUID().toString().take(8)

    /** Token de la sesión con la que habla cada cliente; cambia al "iniciar sesión" con otro rol. */
    private var tokenActual: String? = null
    private val cliente by lazy { SupabaseRestClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) { tokenActual } }
    private val login by lazy { SupabaseUsuariosDataSource(cliente) }
    private val remoto by lazy { SupabasePrestamosDataSource(cliente) }

    private val bases = mutableListOf<PrestamoLabDatabase>()
    private var tokenInstructor: String? = null
    private val equiposCreados = mutableListOf<String>()
    private val actividadesCreadas = mutableListOf<String>()

    @Before
    fun soloSiSePide() {
        assumeTrue("Prueba contra Supabase real: ejecutar con -e e2e true",
            InstrumentationRegistry.getArguments().getString("e2e") == "true")
        assumeTrue("Sin credenciales de Supabase en local.properties", BuildConfig.SUPABASE_URL.isNotBlank())
    }

    @After
    fun limpiar() = runBlocking {
        // Aunque la prueba falle, no deja datos de prueba en el servidor
        tokenInstructor?.let { tokenActual = it }
        equiposCreados.forEach { runCatching { remoto.eliminarEquipo(it) } }
        actividadesCreadas.forEach { runCatching { remoto.eliminarActividad(it) } }
        runCatching { login.cerrarSesion() }
        bases.forEach { it.close() }
    }

    private suspend fun iniciarSesion(documento: String): Usuario {
        val remotoUsuario: UsuarioRemoto = login.buscarPorCredenciales(
            CampoIdentificador.DOCUMENTO, documento, HashUtils.sha256("123456")
        ) ?: error("Credenciales de demostración rechazadas")
        tokenActual = remotoUsuario.token
        return Usuario(remotoUsuario.id, remotoUsuario.nombre, remotoUsuario.email, Rol.valueOf(remotoUsuario.rol))
    }

    /** Un teléfono nuevo: base local vacía que se llena al sincronizar. */
    private fun telefono() = PrestamoLabDatabase.construir(contexto, enMemoria = true).also { bases += it }

    @Test
    fun HU12_HU11_ElInstructorMantieneInventarioYActividades_YElEstudianteSoloLasConsulta() = runTest {
        // ── Instructor ──
        val instructor = iniciarSesion("12345")
        tokenInstructor = tokenActual
        val dbInstructor = telefono()
        val sincronizador = SincronizadorPrestamos(dbInstructor, remoto)
        assertTrue(sincronizador.sincronizar(instructor) is ResultadoSincronizacion.Exito)
        val inventario = RoomPrestamoRepository(dbInstructor)

        // HU-12: registrar, editar y eliminar un equipo llega al servidor
        val equipo = inventario.registrarEquipo("$marca equipo", "Prueba").getOrThrow()
        val idRemotoEquipo = dbInstructor.equipmentDao().obtener(equipo.id)!!.remoteId
        equiposCreados += idRemotoEquipo
        sincronizador.sincronizar(instructor)
        assertEquals("Prueba", remoto.equipos().single { it.id == idRemotoEquipo }.categoria)

        inventario.editarEquipo(equipo.id, "$marca equipo", "Prueba editada").getOrThrow()
        sincronizador.sincronizar(instructor)
        assertEquals("Prueba editada", remoto.equipos().single { it.id == idRemotoEquipo }.categoria)

        inventario.eliminarEquipo(equipo.id).getOrThrow()
        sincronizador.sincronizar(instructor)
        assertTrue(remoto.equipos().none { it.id == idRemotoEquipo })

        // HU-11: la actividad creada por el instructor llega al servidor
        val actividades = RoomActividadRepository(dbInstructor)
        val actividad = actividades.crear(
            DatosActividad("$marca actividad", "Prueba automática", "Laboratorio 302", "2030-06-01 08:00"), instructor.id
        ).getOrThrow()
        val idRemotoActividad = dbInstructor.activityDao().obtener(actividad.id)!!.remoteId
        actividadesCreadas += idRemotoActividad
        sincronizador.sincronizar(instructor)
        assertTrue(remoto.actividades().any { it.id == idRemotoActividad })

        // ── Estudiante en otro teléfono ──
        val estudiante = iniciarSesion("67890")
        val dbEstudiante = telefono()
        SincronizadorPrestamos(dbEstudiante, remoto).sincronizar(estudiante)
        // CA-HU11-05: la recibe para consultarla
        assertTrue(RoomActividadRepository(dbEstudiante).actividades.first().any { it.titulo == "$marca actividad" })
        // El servidor rechaza que el estudiante cree actividades (RLS de 009), aunque se salte la app
        val intento = runCatching {
            remoto.guardarActividad(
                ActividadRemota(UUID.randomUUID().toString(), "$marca intrusa", "", "x", "2030-06-01T13:00:00Z", estudiante.id)
            )
        }
        assertTrue("el servidor debió rechazarla", (intento.exceptionOrNull() as? SupabaseHttpException)?.codigo in setOf(401, 403))
        login.cerrarSesion()

        // ── El instructor la elimina ──
        tokenActual = tokenInstructor
        actividades.eliminar(actividad.id).getOrThrow()
        sincronizador.sincronizar(instructor)
        assertTrue(remoto.actividades().none { it.id == idRemotoActividad })
    }
}
