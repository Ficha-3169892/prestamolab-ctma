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
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.model.Ubicacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regresión del flujo crítico de la guía (actividades 35 y 36), de punta a punta en la UI y con los dos roles:
 * Catálogo → Detalle → Solicitud → Mis préstamos → aprobación del instructor → Devolución.
 * Cada tramo tiene además sus pruebas propias; esta verifica que encajan entre sí.
 */
@RunWith(AndroidJUnit4::class)
class RegresionFlujoCriticoUiTest {

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
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            grantRuntimePermission(app.packageName, Manifest.permission.ACCESS_FINE_LOCATION)
            grantRuntimePermission(app.packageName, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        proveedorReal = app.container.locationProvider
        app.container.locationProvider = ubicacionFalsa
        reiniciarDatosSemilla()
    }

    @After
    fun restaurar() {
        app.container.locationProvider = proveedorReal
    }

    @Test
    fun CatalogoSolicitudMisPrestamosAprobacionYDevolucion() {
        // 1. Estudiante: catálogo → detalle → solicitud
        composeTestRule.iniciarSesionComoEstudiante()
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()
        composeTestRule.onNodeWithText("Estado: DISPONIBLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solicitar Préstamo").performClick()
        composeTestRule.onNodeWithText("Ambiente o Destino").performTextInput("Laboratorio 302")
        composeTestRule.onNodeWithText("Propósito (10-180 caracteres)").performTextInput("Medición de voltajes en la práctica")
        composeTestRule.onNodeWithText("Duración estimada (1-8 horas)").performTextReplacement("2")
        composeTestRule.onNodeWithText("Solicitar").performClick()

        // 2. Mis préstamos muestra la solicitud nueva, pendiente de aprobación
        composeTestRule.onNodeWithText("Mis Solicitudes (3)").assertIsDisplayed()
        val solicitud = solicitudDelMultimetro()
        assertEquals(EstadoSolicitud.SOLICITADA, solicitud.estado)
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Equipo: Multímetro Digital"))
        composeTestRule.onNodeWithText("Equipo: Multímetro Digital").assertIsDisplayed()

        // 3. Instructor: aprueba esa solicitud (hay otra pendiente en los datos semilla)
        composeTestRule.iniciarSesionComoInstructor()
        composeTestRule.onNodeWithText("Revisar solicitudes").performClick()
        composeTestRule.onNodeWithText("Revisar solicitudes (2)").assertIsDisplayed()
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("aprobar-${solicitud.id}"))
        composeTestRule.onNodeWithTag("aprobar-${solicitud.id}").performClick()
        composeTestRule.onNodeWithText("Revisar solicitudes (1)").assertIsDisplayed()
        assertEquals(EstadoSolicitud.PRESTADO, solicitudDelMultimetro().estado)

        // 4. Estudiante: registra la devolución con ubicación desde Mis préstamos
        composeTestRule.iniciarSesionComoEstudiante()
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("devolver-${solicitud.id}"))
        composeTestRule.onNodeWithTag("devolver-${solicitud.id}").performClick()
        composeTestRule.onNodeWithText("Equipo: Multímetro Digital").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bueno").performClick()
        composeTestRule.onNodeWithText("Capturar ubicación actual").performClick()
        composeTestRule.onNodeWithText("Ubicación capturada correctamente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar devolución").performScrollTo().performClick()

        // 5. El préstamo queda cerrado y el equipo vuelve a estar disponible en el catálogo
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithTag("devolver-${solicitud.id}").fetchSemanticsNodes().isEmpty()
        }
        assertEquals(EstadoSolicitud.DEVUELTO, solicitudDelMultimetro().estado)
        val multimetro = runBlocking { app.container.prestamoRepository.equipos.first() }
            .single { it.id == solicitud.equipoId }
        assertEquals(EstadoEquipo.DISPONIBLE, multimetro.estado)
        composeTestRule.onNodeWithText("Catálogo").performClick()
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()
        composeTestRule.onNodeWithText("Estado: DISPONIBLE").assertIsDisplayed()
    }

    private fun solicitudDelMultimetro(): SolicitudPrestamo = runBlocking {
        val multimetro = app.container.prestamoRepository.equipos.first().single { it.nombre == "Multímetro Digital" }
        app.container.prestamoRepository.solicitudes.first().single { it.equipoId == multimetro.id }
    }
}
