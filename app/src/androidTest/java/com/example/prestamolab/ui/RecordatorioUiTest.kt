package com.example.prestamolab.ui

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.recordatorios.AndroidNotificador
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CA-HU09-02: tocar el recordatorio abre la pantalla del préstamo. */
@RunWith(AndroidJUnit4::class)
class RecordatorioUiTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val app = ApplicationProvider.getApplicationContext<PrestamoLabApp>()
    private var escenario: ActivityScenario<MainActivity>? = null

    @Before
    fun preparar() {
        reiniciarDatosSemilla()
    }

    @After
    fun cerrar() {
        escenario?.close()
    }

    private fun iniciarSesion(correo: String) = runBlocking {
        app.container.authRepository.cerrarSesion()
        app.container.authRepository.iniciarSesion(correo, FakeUsuariosDataSource.CONTRASENA).getOrThrow()
    }

    /** El mismo Intent que usa la notificación, con el préstamo semilla #2 (Kit Arduino Uno). */
    private fun abrirDesdeLaNotificacion(): Intent {
        val intent = AndroidNotificador.intentApertura(app, 2)
        escenario = ActivityScenario.launch(intent)
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Préstamo #2").fetchSemanticsNodes().isNotEmpty()
        }
        return intent
    }

    @Test
    fun ElIntentDeLaNotificacionApuntaALaAppConElPrestamo() {
        val intent = AndroidNotificador.intentApertura(app as Context, 2)

        assertEquals(MainActivity::class.java.name, intent.component?.className)
        assertEquals(2, intent.getIntExtra(AndroidNotificador.EXTRA_SOLICITUD_ID, -1))
    }

    @Test
    fun TC_HU09_02_TocarElRecordatorio_AbreElPrestamo() {
        iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE)

        abrirDesdeLaNotificacion()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Préstamo #2").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Préstamo #2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Equipo: Kit Arduino Uno").assertIsDisplayed()
        assertNull("la apertura se consume una sola vez", app.container.aperturas.solicitudId.value)
    }

    @Test
    fun ConLaAppAbierta_ElRecordatorioTambienAbreElPrestamo() {
        iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE)
        escenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.waitUntil(5_000) { composeTestRule.onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty() }

        // La notificación llega a la instancia ya abierta por onNewIntent
        escenario!!.onActivity { it.startActivity(AndroidNotificador.intentApertura(it, 2)) }

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Préstamo #2").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun ElInstructorNoEsLlevadoALaDevolucion() {
        iniciarSesion(FakeUsuariosDataSource.CORREO_INSTRUCTOR)

        abrirDesdeLaNotificacion()

        composeTestRule.onNodeWithText("Gestión (Instructor)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Préstamo #2").assertDoesNotExist()
    }
}
