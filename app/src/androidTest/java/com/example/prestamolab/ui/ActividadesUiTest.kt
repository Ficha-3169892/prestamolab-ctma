package com.example.prestamolab.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HU-11: actividades formativas con la actividad semilla "Práctica de osciloscopio". */
@RunWith(AndroidJUnit4::class)
class ActividadesUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepararDatos() {
        reiniciarDatosSemilla()
    }

    private fun abrirActividadesComoInstructor() {
        composeTestRule.iniciarSesionComoInstructor()
        composeTestRule.onNodeWithText("Actividades formativas").performClick()
    }

    @Test
    fun TC_HU11_01_CrearActividad_ApareceEnLaLista() {
        abrirActividadesComoInstructor()
        composeTestRule.onNodeWithText("Nueva").performClick()
        composeTestRule.onNodeWithText("Título").performTextInput("Taller de soldadura")
        composeTestRule.onNodeWithText("Ambiente").performTextInput("Ambiente de Electrónica")
        composeTestRule.onNodeWithText("Fecha y hora (AAAA-MM-DD HH:MM)").performTextInput("2030-02-01 14:00")
        composeTestRule.onNodeWithText("Guardar actividad").performClick()

        composeTestRule.onNodeWithText("Actividades formativas (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Taller de soldadura").assertIsDisplayed()
    }

    @Test
    fun TC_HU11_02_TituloVacioYFechaPasada_MuestranErroresYNoSeGuarda() {
        abrirActividadesComoInstructor()
        composeTestRule.onNodeWithText("Nueva").performClick()
        composeTestRule.onNodeWithText("Ambiente").performTextInput("Lab 1")
        composeTestRule.onNodeWithText("Fecha y hora (AAAA-MM-DD HH:MM)").performTextInput("2020-01-01 08:00")
        composeTestRule.onNodeWithText("Guardar actividad").performClick()

        composeTestRule.onNodeWithText("El título es obligatorio.").assertIsDisplayed()
        composeTestRule.onNodeWithText("La fecha no puede estar en el pasado.").assertIsDisplayed()
        composeTestRule.onNodeWithText("← Actividades").performClick()
        composeTestRule.onNodeWithText("Actividades formativas (1)").assertIsDisplayed()
    }

    @Test
    fun TC_HU11_03_EditarActividad_SeReflejaEnLaLista() {
        abrirActividadesComoInstructor()
        composeTestRule.onNodeWithTag("editar-actividad-1").performClick()
        composeTestRule.onNodeWithText("Editar actividad").assertIsDisplayed()
        composeTestRule.onNodeWithText("Laboratorio 302").performTextReplacement("Laboratorio 101")
        composeTestRule.onNodeWithText("Guardar actividad").performClick()

        composeTestRule.onNodeWithText("Ambiente: Laboratorio 101").assertIsDisplayed()
    }

    @Test
    fun TC_HU11_04_EliminarConConfirmacion_YCancelarLaConserva() {
        abrirActividadesComoInstructor()
        composeTestRule.onNodeWithTag("eliminar-actividad-1").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()
        composeTestRule.onNodeWithText("Práctica de osciloscopio").assertIsDisplayed()

        composeTestRule.onNodeWithTag("eliminar-actividad-1").performClick()
        composeTestRule.onNodeWithText("Confirmar eliminación").performClick()

        composeTestRule.onNodeWithText("No hay actividades programadas.").assertIsDisplayed()
    }

    @Test
    fun TC_HU11_05_ElEstudianteVeLasActividadesEnSoloLectura() {
        composeTestRule.iniciarSesionComoEstudiante()
        composeTestRule.onNodeWithText("Actividades").performClick()

        composeTestRule.onNodeWithText("Práctica de osciloscopio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fecha: 2030-01-15 08:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nueva").assertDoesNotExist()
        composeTestRule.onNodeWithText("Editar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Eliminar").assertDoesNotExist()
    }
}
