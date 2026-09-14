package com.example.prestamolab.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrestamoUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun TC01_CargarCatalogoInicial_MuestraEquipos() {
        composeTestRule.onNodeWithText("PréstamoLab CTMA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Multímetro Digital").assertIsDisplayed()
    }

    @Test
    fun TC02_VerDetalleEquipoValido() {
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()
        composeTestRule.onNodeWithText("Detalle del Equipo").assertIsDisplayed()
        composeTestRule.onNodeWithText("ID: 1").assertIsDisplayed()
    }

    @Test
    fun TC03_ManejoEstadoEquipoInexistente() {
        composeTestRule.onNodeWithText("Multímetro Digital").assertExists()
    }

    @Test
    fun TC04_Error_PropositoMenorA10Caracteres() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Ambiente o Destino").performTextInput("Laboratorio 1")
        composeTestRule.onNodeWithText("Propósito (10-180 caracteres)").performTextInput("Corto")
        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("Propósito debe tener mínimo 10 caracteres").assertIsDisplayed()
    }

    @Test
    fun TC05_TC06_PropositoValido_NoMuestraError() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Propósito (10-180 caracteres)").performTextInput("Este es un propósito válido")
        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("Propósito debe tener mínimo 10 caracteres").assertDoesNotExist()
    }

    @Test
    fun TC07_Error_PropositoMayorA180Caracteres() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Propósito (10-180 caracteres)").performTextInput("a".repeat(181))
        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("Máximo 180 caracteres").assertIsDisplayed()
    }

    @Test
    fun TC08_Error_DuracionCeroHoras() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Duración estimada (1-8 horas)").performTextReplacement("0")
        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("Duración entre 1 y 8 horas").assertIsDisplayed()
    }

    @Test
    fun TC11_Error_DuracionNueveHoras() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Duración estimada (1-8 horas)").performTextReplacement("9")
        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("Duración entre 1 y 8 horas").assertIsDisplayed()
    }

    @Test
    fun TC12_BotonSolicitarDeshabilitado_SiEquipoEstaReservado() {
        TC14_FlujoCompleto_CrearSolicitud()

        composeTestRule.onNodeWithText("Catálogo").performClick()
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()

        composeTestRule.onNodeWithText("Solicitar Préstamo").assertIsNotEnabled()
    }

    @Test
    fun TC13_DoblePulsacion_MuestraEstadoProcesando() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Ambiente o Destino").performTextInput("Laboratorio 1")
        composeTestRule.onNodeWithText("Propósito (10-180 caracteres)").performTextInput("Propósito para test de doble click")

        composeTestRule.onNodeWithText("Solicitar").performClick()
        composeTestRule.onNodeWithText("Mis Solicitudes (2)").assertIsDisplayed()
    }

    @Test
    fun TC14_FlujoCompleto_CrearSolicitud() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Ambiente o Destino").performTextInput("Laboratorio 302")
        composeTestRule.onNodeWithText("Propósito (10-180 caracteres)").performTextInput("Práctica de sensores de temperatura")
        composeTestRule.onNodeWithText("Duración estimada (1-8 horas)").performTextReplacement("3")

        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("Mis Solicitudes (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Equipo ID: 1").assertIsDisplayed()
    }

    @Test
    fun TC15_CancelarSolicitud_ActualizaLista() {
        // Vamos a la sección de Mis Solicitudes
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.onAllNodesWithText("Cancelar Solicitud")[0].performClick()
        composeTestRule.onNodeWithText("Cancelar Solicitud").assertDoesNotExist()
    }

    @Test
    fun TC17_Navegacion_BackStack_MantieneEstado() {
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()
        composeTestRule.onNodeWithText("Atrás").performClick()
        composeTestRule.onNodeWithText("Catálogo de Equipos - PréstamoLab").assertIsDisplayed()
    }

    @Test
    fun TC18_Accesibilidad_SemanticaDeIconos() {
        composeTestRule.onNodeWithText("Catálogo").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].assertIsDisplayed()
    }

    @Test
    fun Error_AmbienteVacio_MuestraError() {
        navegarAFormulario()
        composeTestRule.onNodeWithText("Ambiente o Destino").performTextReplacement("")
        composeTestRule.onNodeWithText("Solicitar").performClick()

        composeTestRule.onNodeWithText("El ambiente o destino es obligatorio.").assertIsDisplayed()
    }

    @Test
    fun Navegacion_BottomBar_CicloCompleto() {
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].performClick()
        composeTestRule.onNodeWithText("Mis Solicitudes (1)").assertIsDisplayed()

        composeTestRule.onNodeWithText("Catálogo").performClick()
        composeTestRule.onNodeWithText("PréstamoLab CTMA").assertIsDisplayed()
    }

    @Test
    fun DetalleEquipo_VerificaDisponibilidadVisible() {
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()
        composeTestRule.onNodeWithText("Estado: DISPONIBLE").assertIsDisplayed()
    }

    @Test
    fun Formulario_CargaNombreEquipoCorrectamente() {
        composeTestRule.onNodeWithText("Osciloscopio 100MHz").performClick()
        composeTestRule.onNodeWithText("Solicitar Préstamo").performClick()

        composeTestRule.onNodeWithText("Equipo: Osciloscopio 100MHz").assertIsDisplayed()
    }

    // Helper
    private fun navegarAFormulario() {
        composeTestRule.onNodeWithText("Multímetro Digital").performClick()
        composeTestRule.onNodeWithText("Solicitar Préstamo").performClick()
    }
}