package com.example.prestamolab.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HU-14: el instructor revisa la solicitud semilla #1 (Osciloscopio, RESERVADO). */
@RunWith(AndroidJUnit4::class)
class RevisarSolicitudesUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun abrirRevisionComoInstructor() {
        reiniciarDatosSemilla()
        composeTestRule.iniciarSesionComoInstructor()
        composeTestRule.onNodeWithText("Revisar solicitudes").performClick()
    }

    @Test
    fun TC_HU14_01_ListaMuestraEstudianteEquipoAmbientePropositoYDuracion() {
        composeTestRule.onNodeWithText("Revisar solicitudes (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estudiante: Andrés Vargas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Equipo: Osciloscopio 100MHz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ambiente: Laboratorio 302").assertIsDisplayed()
        composeTestRule.onNodeWithText("Propósito: Práctica de señales").assertIsDisplayed()
        composeTestRule.onNodeWithText("Duración: 2 h").assertIsDisplayed()
    }

    @Test
    fun TC_HU14_02_AprobarQuitaLaSolicitudYElEquipoQuedaPrestado() {
        composeTestRule.onNodeWithText("Aprobar").performClick()

        composeTestRule.onNodeWithText("No hay solicitudes pendientes.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Catálogo").performClick()
        composeTestRule.onNodeWithText("Osciloscopio 100MHz").performClick()
        composeTestRule.onNodeWithText("Estado: PRESTADO").assertIsDisplayed()
    }

    @Test
    fun TC_HU14_03_RechazarExigeMotivoYLiberaElEquipo() {
        composeTestRule.onNodeWithText("Rechazar").performClick()
        composeTestRule.onNodeWithText("Confirmar rechazo").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Motivo del rechazo").performTextInput("Equipo en calibración")
        composeTestRule.onNodeWithText("Confirmar rechazo").performClick()

        composeTestRule.onNodeWithText("No hay solicitudes pendientes.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Catálogo").performClick()
        composeTestRule.onNodeWithText("Osciloscopio 100MHz").performClick()
        composeTestRule.onNodeWithText("Estado: DISPONIBLE").assertIsDisplayed()
    }

    @Test
    fun CancelarElDialogoDeRechazo_ConservaLaSolicitud() {
        composeTestRule.onNodeWithText("Rechazar").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        composeTestRule.onNodeWithText("Revisar solicitudes (1)").assertIsDisplayed()
    }

    @Test
    fun AtrasVuelveAGestion() {
        composeTestRule.onNodeWithText("← Gestión").performClick()

        composeTestRule.onNodeWithText("Gestión (Instructor)").assertIsDisplayed()
    }
}
