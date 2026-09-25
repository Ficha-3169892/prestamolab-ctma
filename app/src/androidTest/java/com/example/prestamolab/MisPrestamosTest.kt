package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MisPrestamosTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Comprueba que el usuario puede abrir "Mis Solicitudes" desde el catálogo.
    @Test
    fun abrirMisSolicitudesMuestraLaPantallaCorrecta() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()
    }

    // Comprueba el estado inicial cuando todavía no existen solicitudes.
    @Test
    fun sinSolicitudesMuestraMensajeInformativo() {
        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .performClick()

        composeTestRule
            .onNodeWithText("No hay solicitudes registradas.")
            .assertIsDisplayed()
    }

    // Comprueba que una solicitud creada aparece posteriormente en la lista.
    @Test
    fun solicitudCreadaApareceEnMisSolicitudes() {
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
            .performTextInput("Práctica de medición en laboratorio")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Destino: Laboratorio")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado: SOLICITADA")
            .assertIsDisplayed()
    }

    // Comprueba que una solicitud de la lista puede abrir su detalle.
    @Test
    fun seleccionarSolicitudAbreSuDetalle() {
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
            .performTextInput("Práctica de medición en laboratorio")

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
    }

    // Comprueba que volver desde "Mis Solicitudes" regresa al catálogo.
    @Test
    fun volverDesdeMisSolicitudesRegresaAlCatalogo() {
        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .performClick()

        composeTestRule
            .onNodeWithText("Volver al Catálogo")
            .performClick()

        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()
    }
}
