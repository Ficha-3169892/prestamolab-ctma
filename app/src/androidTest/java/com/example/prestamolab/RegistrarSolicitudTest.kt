package com.example.prestamolab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.prestamolab.ui.solicitud.SolicitarScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel
import org.junit.Rule
import org.junit.Test

class RegistrarSolicitudTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun formulario_muestra_datos_del_equipo() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                SolicitarScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitudCreada = {},
                    onBackClick = {}
                )
            }
        }

        // Usamos hasClickAction para distinguir el botón del título Text
        composeTestRule
            .onNode(hasText("Solicitar préstamo") and hasClickAction())
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Equipo: Multímetro Digital")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado: DISPONIBLE")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Ambiente o destino")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Propósito")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Duración en horas")
            .assertIsDisplayed()
    }

    @Test
    fun formulario_vacio_muestra_errores_de_validacion() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                SolicitarScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitudCreada = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNode(hasText("Solicitar préstamo") and hasClickAction())
            .performClick()

        composeTestRule
            .onNodeWithText("El ambiente o destino es obligatorio")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("El propósito debe tener entre 10 y 180 caracteres")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("La duración debe estar entre 1 y 8 horas")
            .assertIsDisplayed()
    }

    @Test
    fun formulario_valido_crea_solicitud() {
        val viewModel = PrestamoViewModel()

        var solicitudCreada = false

        composeTestRule.setContent {
            MaterialTheme {
                SolicitarScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitudCreada = {
                        solicitudCreada = true
                    },
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Ambiente o destino")
            .performTextInput("Laboratorio 1")

        composeTestRule
            .onNodeWithText("Propósito")
            .performTextInput(
                "Realizar prácticas de electrónica"
            )

        composeTestRule
            .onNodeWithText("Duración en horas")
            .performTextInput("4")

        composeTestRule
            .onNode(hasText("Solicitar préstamo") and hasClickAction())
            .performClick()

        composeTestRule.waitForIdle()

        assert(solicitudCreada)

        // Verificamos que el mensaje aparezca en pantalla
        composeTestRule
            .onNodeWithText("Solicitud creada correctamente", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun solicitud_creada_cambia_estado_del_equipo() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                SolicitarScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitudCreada = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Ambiente o destino")
            .performTextInput("Laboratorio 1")

        composeTestRule
            .onNodeWithText("Propósito")
            .performTextInput(
                "Realizar prácticas de electrónica"
            )

        composeTestRule
            .onNodeWithText("Duración en horas")
            .performTextInput("4")

        composeTestRule
            .onNode(hasText("Solicitar préstamo") and hasClickAction())
            .performClick()

        composeTestRule.waitForIdle()

        assert(
            viewModel.obtenerEquipo(1)?.estado.toString() == "RESERVADO"
        )
    }

    @Test
    fun equipo_no_disponible_no_muestra_formulario() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                SolicitarScreen(
                    equipoId = 3,
                    viewModel = viewModel,
                    onSolicitudCreada = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Equipo no disponible")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Este equipo no puede solicitarse porque actualmente está RESERVADO."
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Volver")
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithText("Ambiente o destino")
            .assertCountEquals(0)
    }

    @Test
    fun equipo_inexistente_no_permite_registro() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                SolicitarScreen(
                    equipoId = 999,
                    viewModel = viewModel,
                    onSolicitudCreada = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Equipo no encontrado")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "No es posible realizar una solicitud para este equipo."
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Volver")
            .assertIsDisplayed()
    }
}
