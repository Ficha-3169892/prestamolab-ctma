package com.example.prestamolab.ui

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.prestamolab.MainActivity
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CA-HU13-01: el permiso de ubicación se pide al tocar "Capturar ubicación actual", nunca al abrir la app.
 * Requiere la ubicación sin conceder: DevolucionUiTest la concede durante la suite completa, así que esta
 * prueba se ejecuta aparte, después de `adb shell pm revoke` (ver docs/PLAN_PRUEBAS.md).
 */
@RunWith(AndroidJUnit4::class)
class PermisoUbicacionUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private val paquete get() = instrumentacion.targetContext.packageName

    @Before
    fun preparar() {
        val contexto = instrumentacion.targetContext
        assumeTrue(
            "Requiere la ubicación sin conceder",
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).none {
                ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
            }
        )
        // Sin esto, tras negarlo dos veces Android deja de mostrar el diálogo
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).forEach {
            instrumentacion.uiAutomation.executeShellCommand("pm clear-permission-flags $paquete $it user-set user-fixed").close()
        }
        reiniciarDatosSemilla()
        composeTestRule.iniciarSesionComoEstudiante()
    }

    @Test
    fun TC_HU13_01_ElPermisoSePideAlCapturarLaUbicacion_YNegarloNoBloqueaLaDevolucion() {
        // Abrir la app y navegar no muestra ningún diálogo del sistema
        assertAppEnPrimerPlano()
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.tocarEnMisSolicitudes("Registrar devolución")
        assertAppEnPrimerPlano()

        composeTestRule.onNodeWithText("Capturar ubicación actual").performClick()

        composeTestRule.waitUntil(5_000) {
            instrumentacion.uiAutomation.rootInActiveWindow?.packageName?.toString()?.let { it != paquete } == true
        }
        instrumentacion.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Permiso de ubicación denegado", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Confirmar devolución").performScrollTo().assertIsDisplayed()
    }

    private fun assertAppEnPrimerPlano() {
        composeTestRule.waitForIdle()
        org.junit.Assert.assertEquals(paquete, instrumentacion.uiAutomation.rootInActiveWindow?.packageName?.toString())
    }
}
