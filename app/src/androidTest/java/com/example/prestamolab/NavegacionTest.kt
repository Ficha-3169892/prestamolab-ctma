package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class NavegacionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Comprueba la navegación Catálogo → Detalle → Formulario.
    @Test
    fun navegarDesdeCatalogoHastaFormulario() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Equipo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Registrar Solicitud")
            .assertIsDisplayed()
    }

    // Comprueba que la navegación conserva el equipo seleccionado.
    @Test
    fun formularioMuestraElEquipoSeleccionado() {
        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitando: Multímetro Digital")
            .assertIsDisplayed()
    }

    // Comprueba que una navegación hacia una solicitud conserva sus datos.
    @Test
    fun detalleSolicitudMuestraLosDatosCorrectos() {
        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextInput("Laboratorio")

        composeTestRule
            .onNodeWithText("Propósito (10-180 caract.)")
            .performTextInput("Prueba de navegación de solicitud")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Estado: SOLICITADA")
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Solicitud")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Ambiente Destino: Laboratorio")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Duración: 1 horas")
            .assertIsDisplayed()
    }

    // Comprueba que volver desde el formulario y luego desde el detalle mantiene el back stack correcto.
    @Test
    fun volverDesdeFormularioConservaElBackStack() {
        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Equipo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Registrar Solicitud")
            .assertIsDisplayed()

        // Formulario → Detalle
        composeTestRule
            .onNodeWithText("Cancelar")
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Equipo")
            .assertIsDisplayed()

        // Detalle → Catálogo
        composeTestRule
            .onNodeWithText("Volver al Catálogo")
            .performClick()

        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()
    }
}
