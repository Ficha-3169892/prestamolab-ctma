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
import com.example.prestamolab.ui.evidencias.EvidenciasViewModel
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * HU-08 con el préstamo semilla #2 (PRESTADO). El permiso de cámara nunca se concede en las pruebas:
 * revocarlo después mataría el proceso de prueba.
 */
@RunWith(AndroidJUnit4::class)
class EvidenciasUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private val paquete get() = instrumentacion.targetContext.packageName

    @Before
    fun prepararDatos() {
        reiniciarDatosSemilla()
        composeTestRule.iniciarSesionComoEstudiante()
    }

    private fun abrirEvidenciasDelPrestamo2() {
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.onNodeWithText("Evidencias").performClick()
    }

    @Test
    fun LasEvidenciasSeAbrenDesdeMisSolicitudes() {
        abrirEvidenciasDelPrestamo2()

        composeTestRule.onNodeWithText("Evidencias del préstamo #2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fotos del estado del equipo al recibir el equipo.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aún no hay evidencias en este préstamo.").assertIsDisplayed()
    }

    @Test
    fun LaDevolucionAbreLasEvidenciasDeDevolucion() {
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.onNodeWithText("Registrar devolución").performClick()
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Adjuntar evidencia de devolución"))
        composeTestRule.onNodeWithText("Adjuntar evidencia de devolución").performClick()

        composeTestRule.onNodeWithText("Fotos del estado del equipo al devolver el equipo.").assertIsDisplayed()
    }

    /**
     * CA-HU08-01 y CA-HU08-03: al tocar "Adjuntar evidencia" aparece el diálogo del sistema (no al abrir
     * la app); al rechazarlo se explica el motivo y la app sigue funcionando.
     */
    @Test
    fun TC_HU08_01_Y_03_ElPermisoSePideAlAdjuntar_YNegarloMuestraUnMensaje() {
        val contexto = instrumentacion.targetContext
        assumeTrue(
            "Requiere el permiso de cámara sin conceder",
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        )
        // Sin esto, tras negarlo dos veces Android deja de mostrar el diálogo
        instrumentacion.uiAutomation.executeShellCommand(
            "pm clear-permission-flags $paquete ${Manifest.permission.CAMERA} user-set user-fixed"
        ).close()
        abrirEvidenciasDelPrestamo2()

        composeTestRule.onNodeWithText("Adjuntar evidencia").performClick()

        // El diálogo pertenece al sistema (otro paquete) y está en primer plano
        composeTestRule.waitUntil(5_000) {
            instrumentacion.uiAutomation.rootInActiveWindow?.packageName?.toString()?.let { it != paquete } == true
        }
        instrumentacion.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText(EvidenciasViewModel.MENSAJE_SIN_PERMISO).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Evidencias del préstamo #2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aún no hay evidencias en este préstamo.").assertIsDisplayed()
    }
}
