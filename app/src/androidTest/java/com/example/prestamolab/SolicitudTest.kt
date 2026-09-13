package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.click
import org.junit.Rule
import org.junit.Test

class SolicitudTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun abrirFormulario() {
        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Registrar Solicitud")
            .assertIsDisplayed()
    }

    private fun llenarDatosValidos(
        proposito: String = "Práctica de laboratorio"
    ) {
        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextInput("Laboratorio")

        composeTestRule
            .onNodeWithText("Propósito (10-180 caract.)")
            .performTextInput(proposito)
    }

    // Comprueba que un destino vacío impide completar correctamente la solicitud.
    @Test
    fun destinoVacioMuestraError() {
        abrirFormulario()

        llenarDatosValidos()

        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText(
                "El ambiente o destino es obligatorio."
            )
            .assertIsDisplayed()
    }

    // Comprueba que un propósito demasiado corto es rechazado.
    @Test
    fun propositoMenorA10CaracteresMuestraError() {
        abrirFormulario()

        llenarDatosValidos("123456789")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText(
                "El propósito debe tener entre 10 y 180 caracteres."
            )
            .assertIsDisplayed()
    }

    // Comprueba que un propósito válido de 10 caracteres puede continuar.
    @Test
    fun propositoDe10CaracteresEsValido() {
        abrirFormulario()

        llenarDatosValidos("1234567890")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()
    }

    // Comprueba que una duración menor al mínimo es rechazada.
    @Test
    fun duracionCeroMuestraError() {
        abrirFormulario()

        llenarDatosValidos()

        composeTestRule
            .onNodeWithText("Duración estimada (Horas 1-8)")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Duración estimada (Horas 1-8)")
            .performTextInput("0")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText(
                "La duración debe estar entre 1 y 8 horas."
            )
            .assertIsDisplayed()
    }

    // Comprueba que una duración superior al máximo es rechazada.
    @Test
    fun duracionNueveMuestraError() {
        abrirFormulario()

        llenarDatosValidos()

        composeTestRule
            .onNodeWithText("Duración estimada (Horas 1-8)")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Duración estimada (Horas 1-8)")
            .performTextInput("9")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("La duración debe estar entre 1 y 8 horas.")
            .assertIsDisplayed()
    }

    // Comprueba que una solicitud válida puede guardarse desde la interfaz.
    @Test
    fun formularioValidoCreaSolicitud() {
        abrirFormulario()

        llenarDatosValidos()

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado: SOLICITADA")
            .assertIsDisplayed()
    }
}
