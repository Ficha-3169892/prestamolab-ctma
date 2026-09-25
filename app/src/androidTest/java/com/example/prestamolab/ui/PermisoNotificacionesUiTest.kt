package com.example.prestamolab.ui

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.core.app.ApplicationProvider
import com.example.prestamolab.MainActivity
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CA-HU09-04: en Android 13+, con un préstamo entregado la app pide POST_NOTIFICATIONS; si se niega, sigue
 * funcionando. El runner concede el permiso para el resto de la suite, así que esta prueba se ejecuta aparte,
 * tras revocarlo desde adb y con `-e concederNotificaciones false` (ver docs/PLAN_PRUEBAS.md).
 */
@RunWith(AndroidJUnit4::class)
class PermisoNotificacionesUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private val paquete get() = instrumentacion.targetContext.packageName

    @Before
    fun preparar() {
        assumeTrue("Requiere Android 13 o superior", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        assumeTrue(
            "Requiere POST_NOTIFICATIONS sin conceder",
            ContextCompat.checkSelfPermission(instrumentacion.targetContext, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        )
        // Sin esto, tras negarlo dos veces Android deja de mostrar el diálogo
        instrumentacion.uiAutomation.executeShellCommand(
            "pm clear-permission-flags $paquete ${Manifest.permission.POST_NOTIFICATIONS} user-set user-fixed"
        ).close()
        // Semilla: el préstamo #2 está PRESTADO, así que hace falta un recordatorio
        reiniciarDatosSemilla()
    }

    @Test
    fun TC_HU09_04_ConPrestamoEntregadoSePideElPermiso_YNegarloNoBloqueaLaApp() {
        // Sesión directa: el diálogo tapa la app en cuanto aparece el área del estudiante, así que no se espera
        // el botón "Salir" como en las demás pruebas
        runBlocking {
            app.container.authRepository.cerrarSesion()
            app.container.authRepository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)
                .getOrThrow()
        }

        // El diálogo del sistema (otro paquete) queda en primer plano
        esperar { instrumentacion.uiAutomation.rootInActiveWindow?.packageName?.toString()?.let { it != paquete } == true }
        instrumentacion.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

        // La app sigue funcionando: vuelve al primer plano y Mis Solicitudes muestra el préstamo
        esperar { instrumentacion.uiAutomation.rootInActiveWindow?.packageName?.toString() == paquete }
        composeTestRule.waitUntil(5_000) { composeTestRule.onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty() }
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.onNodeWithText("Equipo: Kit Arduino Uno").assertIsDisplayed()
        assertEquals(
            PackageManager.PERMISSION_DENIED,
            ContextCompat.checkSelfPermission(instrumentacion.targetContext, Manifest.permission.POST_NOTIFICATIONS)
        )
    }

    /**
     * Espera sin consultar Compose: mientras el diálogo tapa la app, Compose no encuentra su jerarquía. El reloj de
     * la prueba sí debe avanzar: sin eso la pantalla no se recompone y el efecto que pide el permiso nunca corre.
     */
    private fun esperar(condicion: () -> Boolean) {
        val limite = System.currentTimeMillis() + 10_000
        while (!condicion()) {
            check(System.currentTimeMillis() < limite) {
                "La condición no se cumplió en 10 s; ventana activa: " +
                    instrumentacion.uiAutomation.rootInActiveWindow?.packageName
            }
            composeTestRule.mainClock.advanceTimeBy(100)
            Thread.sleep(100)
        }
    }

    private val app: PrestamoLabApp get() = ApplicationProvider.getApplicationContext()
}
