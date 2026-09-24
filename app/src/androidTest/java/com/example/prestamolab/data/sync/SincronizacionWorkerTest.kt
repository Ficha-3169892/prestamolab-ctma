package com.example.prestamolab.data.sync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.data.remote.SupabaseHttpException
import com.example.prestamolab.testutil.FakePrestamosRemoteDataSource
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** El Worker traduce cada resultado a WorkManager (reintento exponencial, fallo o éxito). */
@RunWith(AndroidJUnit4::class)
class SincronizacionWorkerTest {

    private val app = ApplicationProvider.getApplicationContext<PrestamoLabApp>()
    private val remoto get() = app.container.prestamosRemoteDataSource as FakePrestamosRemoteDataSource

    private fun worker() = TestListenableWorkerBuilder<SincronizacionWorker>(app).build()

    @Before
    fun iniciarSesion() = runTest {
        DatosSemilla.reiniciar(app.container.database)
        remoto.error = null
        app.container.authRepository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)
            .getOrThrow()
    }

    @After
    fun limpiar() = runTest {
        remoto.error = null
        app.container.authRepository.cerrarSesion()
    }

    @Test
    fun ConServidorDisponible_TerminaConExito() = runTest {
        assertEquals(ListenableWorker.Result.success(), worker().doWork())
    }

    @Test
    fun TC_HU07_05_Error5xx_PideReintentarConEsperaExponencial() = runTest {
        remoto.error = SupabaseHttpException(500, "Internal Server Error")

        assertEquals(ListenableWorker.Result.retry(), worker().doWork())
        assertNotNull(app.container.authRepository.sesion.first())
    }

    @Test
    fun TC_HU07_03_Respuesta401_CierraLaSesionYFalla() = runTest {
        remoto.error = SupabaseHttpException(401, "JWT expired")

        assertEquals(ListenableWorker.Result.failure(), worker().doWork())
        assertNull(app.container.authRepository.sesion.first())
    }
}
