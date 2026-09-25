package com.example.prestamolab.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HU-01 (filtros del catálogo) y HU-04 (Mis Solicitudes) con los datos semilla. */
@RunWith(AndroidJUnit4::class)
class CatalogoFiltrosUiTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val app = ApplicationProvider.getApplicationContext<PrestamoLabApp>()
    private var escenario: ActivityScenario<MainActivity>? = null

    @Before
    fun preparar() {
        reiniciarDatosSemilla()
        abrirAppComoEstudiante()
    }

    @After
    fun cerrar() {
        escenario?.close()
    }

    private fun abrirAppComoEstudiante() {
        escenario?.close()
        iniciarSesionSinUi()
        escenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.waitUntil(5_000) { composeTestRule.onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty() }
    }

    /** Sesión directa en el repositorio, antes de abrir la actividad (como al reabrir la app). */
    private fun iniciarSesionSinUi() = runBlocking {
        app.container.authRepository.cerrarSesion()
        app.container.authRepository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)
            .getOrThrow()
    }

    @Test
    fun TC_HU01_03_SoloDisponiblesYCategoria_FiltranLaLista() {
        composeTestRule.onNodeWithText("Solo disponibles").performClick()

        composeTestRule.onNodeWithText("Osciloscopio 100MHz").assertDoesNotExist()
        composeTestRule.onNodeWithText("Kit Arduino Uno").assertDoesNotExist()
        composeTestRule.onNodeWithText("Multímetro Digital").assertIsDisplayed()

        composeTestRule.onNodeWithText("Laboratorio").performClick()

        composeTestRule.onNodeWithText("Fuente de Poder DC").assertIsDisplayed()
        composeTestRule.onNodeWithText("Multímetro Digital").assertDoesNotExist()
    }

    @Test
    fun TC_HU01_04_ElFiltroSeConservaAlCerrarYVolverAAbrirLaApp() {
        composeTestRule.onNodeWithText("Herramienta").performClick()
        composeTestRule.onNodeWithText("Osciloscopio 100MHz").assertDoesNotExist()

        abrirAppComoEstudiante()

        composeTestRule.onNodeWithText("Herramienta").assertIsSelected()
        composeTestRule.onNodeWithText("Osciloscopio 100MHz").assertDoesNotExist()
        composeTestRule.onNodeWithText("Multímetro Digital").assertIsDisplayed()
    }

    @Test
    fun TC_HU01_05_SinResultados_MuestraElMensajeYSePuedenQuitarLosFiltros() {
        // En la semilla el único equipo de Laboratorio disponible es la Fuente de Poder: al reservarla, el
        // filtro "Solo disponibles" + "Laboratorio" no deja ninguno
        runBlocking { app.container.database.equipmentDao().actualizarEstado(4, EstadoEquipo.RESERVADO) }
        composeTestRule.onNodeWithText("Solo disponibles").performClick()
        composeTestRule.onNodeWithText("Laboratorio").performClick()

        composeTestRule.onNodeWithText("No hay equipos para mostrar").assertIsDisplayed()

        composeTestRule.onNodeWithText("Quitar filtros").performClick()
        composeTestRule.onNodeWithText("Multímetro Digital").assertIsDisplayed()
    }

    @Test
    fun TC_HU04_02_CadaSolicitudMuestraEquipoFechasAmbienteYEstado() {
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()

        composeTestRule.onNodeWithText("Equipo: Osciloscopio 100MHz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ambiente: Laboratorio 302").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solicitada: 2026-09-02 08:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Devolver antes de: 2026-09-02 10:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado: SOLICITADA").assertIsDisplayed()
    }

    @Test
    fun TC_HU04_04_SinSolicitudesActivas_MuestraElMensaje() {
        runBlocking {
            app.container.database.loanDao().actualizarEstado(1, EstadoSolicitud.CANCELADA)
            app.container.database.loanDao().actualizarEstado(2, EstadoSolicitud.DEVUELTO)
        }
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()

        composeTestRule.onNodeWithText("No tienes solicitudes activas.").assertIsDisplayed()
    }
}
