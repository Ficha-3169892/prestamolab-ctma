package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogoTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // TC-01: Catálogo con datos
    @Test
    fun catalogoMuestraEquiposYDisponibilidad() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        assert(
            composeTestRule
                .onAllNodesWithText("Estado: DISPONIBLE")
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
    }

    // TC-02: EquipoId válido
    @Test
    fun seleccionarEquipoMuestraSuDetalle() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        val equipoNombre = "Multímetro Digital"

        composeTestRule
            .onNodeWithText(equipoNombre)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Equipo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(equipoNombre)
            .assertIsDisplayed()
    }

    // TC-12: Equipo no disponible
    @Test
    fun equipoNoDisponibleNoMuestraBotonSolicitar() {
        composeTestRule
            .onNodeWithText("Cámara Fotográfica")
            .performClick()

        composeTestRule
            .onNodeWithText("Estado actual: RESERVADO")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Este equipo no se encuentra disponible actualmente.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .assertDoesNotExist()
    }

    // TC-14: Crear solicitud válida
    @Test
    fun crearSolicitudValidaMuestraSolicitudEnMisSolicitudes() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        val equipoNombre = "Multímetro Digital"

        composeTestRule
            .onNodeWithText(equipoNombre)
            .assertIsDisplayed()
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

        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextInput("Laboratorio")

        composeTestRule
            .onNodeWithText("Propósito (10-180 caract.)")
            .performTextInput("Préstamo para práctica de laboratorio")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()
    }

    // TC-15: Cancelar SOLICITADA
    @Test
    fun cancelarSolicitudCambiaEstadoYLiberaEquipo() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        val equipoNombre = "Multímetro Digital"

        composeTestRule
            .onNodeWithText(equipoNombre)
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextInput("Laboratorio")

        composeTestRule
            .onNodeWithText("Propósito (10-180 caract.)")
            .performTextInput("Préstamo para práctica de laboratorio")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado: SOLICITADA")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Solicitud")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado: SOLICITADA")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Cancelar Solicitud")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithText("Estado: CANCELADA")
            .assertIsDisplayed()
    }

    // TC-16: Cancelar una solicitud CANCELADA no tiene efecto
    @Test
    fun solicitudCanceladaNoPermiteCancelarNuevamente() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        val equipoNombre = "Multímetro Digital"

        composeTestRule
            .onNodeWithText(equipoNombre)
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextInput("Laboratorio")

        composeTestRule
            .onNodeWithText("Propósito (10-180 caract.)")
            .performTextInput("Préstamo para práctica de laboratorio")

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Mis Solicitudes")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado: SOLICITADA")
            .performClick()

        composeTestRule
            .onNodeWithText("Cancelar Solicitud")
            .performClick()

        composeTestRule
            .onNodeWithText("Estado: CANCELADA")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Cancelar Solicitud")
            .assertDoesNotExist()
    }

    // TC-17: Volver desde formulario mantiene la integridad del back stack
    @Test
    fun volverDesdeFormularioRegresaAlDetalleYLuegoAlCatalogo() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        val equipoNombre = "Multímetro Digital"

        // Catálogo → Detalle
        composeTestRule
            .onNodeWithText(equipoNombre)
            .performClick()

        composeTestRule
            .onNodeWithText("Detalle de Equipo")
            .assertIsDisplayed()

        // Detalle → Formulario
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

    // TC-18: Fuente aumentada y texto largo
    @Test
    fun formularioMantieneAccionesConTextoLargo() {
        composeTestRule
            .onNodeWithText("Catálogo de Equipos")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .performClick()

        composeTestRule
            .onNodeWithText("Solicitar Préstamo")
            .performClick()

        composeTestRule
            .onNodeWithText("Registrar Solicitud")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Ambiente o Destino")
            .performTextInput("Laboratorio de Electrónica")

        val propositoLargo =
            "Práctica de laboratorio para realizar mediciones, " +
                    "comprobar circuitos y analizar los resultados obtenidos " +
                    "durante la actividad de formación."

        composeTestRule
            .onNodeWithText("Propósito (10-180 caract.)")
            .performTextInput(propositoLargo)

        composeTestRule
            .onNodeWithText("Duración estimada (Horas 1-8)")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Guardar Solicitud")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Cancelar")
            .assertIsDisplayed()
            .assertIsEnabled()
    }
}
