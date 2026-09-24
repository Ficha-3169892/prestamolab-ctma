package com.example.prestamolab.data.sync

import com.example.prestamolab.data.auth.UsuariosAuthRepository
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.FakeSessionStore
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class CoordinadorSincronizacionTest {

    private val usuario = Usuario("uuid-estudiante", "Estudiante CTMA", "estudiante@sena.edu.co", Rol.ESTUDIANTE)
    private val store = FakeSessionStore(Sesion(usuario))
    private val auth = UsuariosAuthRepository(FakeUsuariosDataSource(), store)
    private val avisos = AvisosSincronizacion()

    private fun coordinador(resultado: ResultadoSincronizacion) = CoordinadorSincronizacion(
        auth,
        object : Sincronizador {
            override suspend fun sincronizar(usuario: Usuario) = resultado
        },
        avisos
    )

    @Test
    fun `Exito termina el trabajo y limpia avisos anteriores`() = runTest {
        avisos.informar("aviso viejo")

        val accion = coordinador(ResultadoSincronizacion.Exito(enviados = 2, recibidos = 5)).ejecutar()

        assertEquals(AccionTrasSincronizar.EXITO, accion)
        assertNull(avisos.mensaje.value)
    }

    @Test
    fun `Registros rechazados se avisan sin reintentar`() = runTest {
        val accion = coordinador(ResultadoSincronizacion.Exito(enviados = 1, recibidos = 0, rechazados = 2)).ejecutar()

        assertEquals(AccionTrasSincronizar.EXITO, accion)
        assertEquals("El servidor rechazó 2 cambio(s). Se conservan en el teléfono.", avisos.mensaje.value)
    }

    @Test
    fun `TC-HU07-03 - 401 cierra la sesion para volver al login`() = runTest {
        val accion = coordinador(ResultadoSincronizacion.NoAutorizado).ejecutar()

        assertEquals(AccionTrasSincronizar.FALLO, accion)
        assertNull(store.sesion.value)
    }

    @Test
    fun `TC-HU07-04 - 404 muestra un mensaje, no reintenta y conserva la sesion`() = runTest {
        val accion = coordinador(ResultadoSincronizacion.RecursoNoEncontrado("relation does not exist")).ejecutar()

        assertEquals(AccionTrasSincronizar.FALLO, accion)
        assertEquals(
            "No se encontró un recurso en el servidor. Tus datos locales se conservan.",
            avisos.mensaje.value
        )
        assertNotNull(store.sesion.value)
    }

    @Test
    fun `TC-HU07-05 - Error temporal pide reintentar sin mostrar error ni cerrar sesion`() = runTest {
        val accion = coordinador(ResultadoSincronizacion.ErrorTemporal(IOException("timeout"))).ejecutar()

        assertEquals(AccionTrasSincronizar.REINTENTAR, accion)
        assertNull(avisos.mensaje.value)
        assertNotNull(store.sesion.value)
    }

    @Test
    fun `Otro 4xx muestra el codigo y no reintenta`() = runTest {
        val accion = coordinador(ResultadoSincronizacion.Rechazado(400, "PGRST200")).ejecutar()

        assertEquals(AccionTrasSincronizar.FALLO, accion)
        assertTrue(avisos.mensaje.value!!.contains("HTTP 400"))
    }

    @Test
    fun `Sin sesion no sincroniza`() = runTest {
        store.limpiar()
        var llamadas = 0
        val coordinador = CoordinadorSincronizacion(
            auth,
            object : Sincronizador {
                override suspend fun sincronizar(usuario: Usuario): ResultadoSincronizacion {
                    llamadas++
                    return ResultadoSincronizacion.Exito(0, 0)
                }
            },
            avisos
        )

        assertEquals(AccionTrasSincronizar.EXITO, coordinador.ejecutar())
        assertEquals(0, llamadas)
    }
}
