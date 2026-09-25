package com.example.prestamolab.data.auth

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DataStoreSessionStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val nombreArchivo = "sesion-test-${UUID.randomUUID()}"
    private val sesionPrueba = Sesion(
        Usuario("u-1", "Aprendiz", "a@ctma.edu.co", Rol.INSTRUCTOR),
        token = "5a0e0000-0000-4000-8000-000000000001"
    )

    private val cifrador = KeystoreCifradorToken(alias = "prestamolab-token-prueba")

    private fun abrirDataStore(job: Job) = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
        context.preferencesDataStoreFile(nombreArchivo)
    }

    /** Crea un DataStore sobre el mismo archivo, como ocurre al reiniciar el proceso de la app. */
    private fun abrirStore(job: Job) = DataStoreSessionStore(abrirDataStore(job), cifrador)

    @After
    fun borrarArchivo() {
        context.preferencesDataStoreFile(nombreArchivo).delete()
    }

    @Test
    fun TC_HU10_04_SesionPersisteAlReabrirElAlmacenamiento() = runTest {
        val primeraEjecucion = Job()
        abrirStore(primeraEjecucion).guardar(sesionPrueba)
        primeraEjecucion.cancelAndJoin()

        val segundaEjecucion = Job()
        val leida = abrirStore(segundaEjecucion).sesion.first()
        segundaEjecucion.cancelAndJoin()

        assertEquals(sesionPrueba, leida)
    }

    @Test
    fun LimpiarSesion_NoDejaDatosAlReabrir() = runTest {
        val primeraEjecucion = Job()
        abrirStore(primeraEjecucion).apply {
            guardar(sesionPrueba)
            limpiar()
        }
        primeraEjecucion.cancelAndJoin()

        val segundaEjecucion = Job()
        val leida = abrirStore(segundaEjecucion).sesion.first()
        segundaEjecucion.cancelAndJoin()

        assertNull(leida)
    }

    @Test
    fun ElTokenSeGuardaCifrado_NuncaEnTextoPlano() = runTest {
        val ejecucion = Job()
        val dataStore = abrirDataStore(ejecucion)
        DataStoreSessionStore(dataStore, cifrador).guardar(sesionPrueba)

        // Lo que queda en el archivo: ni el token ni un fragmento de él
        val guardado = dataStore.data.first().asMap().values.joinToString(" ")
        ejecucion.cancelAndJoin()

        assertFalse(guardado.contains(sesionPrueba.token!!))
        assertFalse(guardado.contains("5a0e0000"))
    }

    @Test
    fun CifrarYDescifrar_RecuperaElMismoToken_ConUnIvDistintoCadaVez() {
        val token = "5a0e0000-0000-4000-8000-000000000001"

        val primero = cifrador.cifrar(token)
        val segundo = cifrador.cifrar(token)

        assertNotEquals("GCM usa un IV nuevo en cada cifrado", primero, segundo)
        assertEquals(token, cifrador.descifrar(primero))
        assertEquals(token, cifrador.descifrar(segundo))
    }

    @Test
    fun UnValorAlteradoOIlegible_NoDevuelveToken() {
        val cifrado = cifrador.cifrar("5a0e0000-0000-4000-8000-000000000001")
        val alterado = cifrado.dropLast(4) + "AAAA"

        assertNull(cifrador.descifrar(alterado))
        assertNull(cifrador.descifrar("no-es-base64-valido!"))
    }
}
