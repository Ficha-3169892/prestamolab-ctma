package com.example.prestamolab.ui

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.prestamolab.MainActivity
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.location.LocationProvider
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.Ubicacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DevolucionUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val app: PrestamoLabApp = ApplicationProvider.getApplicationContext()
    private lateinit var proveedorReal: LocationProvider

    /** Ubicación fija: la prueba no depende del GPS ni de estar dentro del CTMA. */
    private val ubicacionFalsa = object : LocationProvider {
        override suspend fun ubicacionActual() = Result.success(Ubicacion(6.2518, -75.5636, 12f))
    }

    @Before
    fun preparar() {
        // El diálogo de permisos es del sistema; se concede por adelantado para probar el flujo de la app
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            grantRuntimePermission(app.packageName, Manifest.permission.ACCESS_FINE_LOCATION)
            grantRuntimePermission(app.packageName, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        proveedorReal = app.container.locationProvider
        app.container.locationProvider = ubicacionFalsa
        reiniciarDatosSemilla()
        composeTestRule.iniciarSesionComoEstudiante()
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
    }

    @After
    fun restaurar() {
        app.container.locationProvider = proveedorReal
    }

    @Test
    fun TC_HU05_01_SoloElPrestamoPrestadoOfreceRegistrarDevolucion() {
        composeTestRule.onAllNodesWithText("Registrar devolución").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Cancelar Solicitud").assertCountEquals(1)
    }

    @Test
    fun TC_HU13_02_CapturarUbicacion_MuestraCoordenadasYMensaje() {
        composeTestRule.onNodeWithText("Registrar devolución").performClick()

        composeTestRule.onNodeWithText("Capturar ubicación actual").performClick()

        composeTestRule.onNodeWithText("Ubicación capturada correctamente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lat: 6.251800, Lng: -75.563600").assertIsDisplayed()
        composeTestRule.onNodeWithText("Precisión: ±12 m").assertIsDisplayed()
    }

    @Test
    fun TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo() {
        composeTestRule.onNodeWithText("Registrar devolución").performClick()
        composeTestRule.onNodeWithText("Préstamo #2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Equipo: Kit Arduino Uno").assertIsDisplayed()

        composeTestRule.onNodeWithText("Bueno").performClick()
        composeTestRule.onNodeWithText("Capturar ubicación actual").performClick()
        composeTestRule.onNodeWithText("Ubicación capturada correctamente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar devolución").performScrollTo().performClick()

        // Vuelve a Mis Solicitudes sin el préstamo devuelto
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Mis Solicitudes (1)").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Registrar devolución").assertDoesNotExist()

        val devolucion = runBlocking { app.container.prestamoRepository.devoluciones.first() }.single()
        assertEquals(CondicionEquipo.BUENO, devolucion.condicion)
        assertEquals(6.2518, devolucion.latitud!!, 0.0)
        assertEquals(-75.5636, devolucion.longitud!!, 0.0)
    }

    @Test
    fun TC_HU05_04_EquipoDanadoSinObservacion_MuestraError() {
        composeTestRule.onNodeWithText("Registrar devolución").performClick()

        composeTestRule.onNodeWithText("Dañado").performClick()
        composeTestRule.onNodeWithText("Confirmar devolución").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Describe el daño (mínimo 10 caracteres).").assertIsDisplayed()
    }
}
