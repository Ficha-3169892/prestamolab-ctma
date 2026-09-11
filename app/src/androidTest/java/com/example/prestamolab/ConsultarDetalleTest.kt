package com.example.prestamolab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.prestamolab.ui.equipo.EquipoDetalleScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel
import org.junit.Rule
import org.junit.Test

class ConsultarDetalleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun detalle_muestra_nombre_del_equipo() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                EquipoDetalleScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitarClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .assertIsDisplayed()
    }

    @Test
    fun detalle_muestra_categoria_y_estado() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                EquipoDetalleScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitarClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Categoría")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("ELECTRONICA")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Estado")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("DISPONIBLE")
            .assertIsDisplayed()
    }

    @Test
    fun equipo_disponible_muestra_boton_solicitar() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                EquipoDetalleScreen(
                    equipoId = 1,
                    viewModel = viewModel,
                    onSolicitarClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Solicitar préstamo")
            .assertIsDisplayed()
    }

    @Test
    fun equipo_inexistente_muestra_mensaje() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                EquipoDetalleScreen(
                    equipoId = 999,
                    viewModel = viewModel,
                    onSolicitarClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Equipo no encontrado")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "El equipo solicitado no existe o ya no está disponible."
            )
            .assertIsDisplayed()
    }

    @Test
    fun equipo_inexistente_muestra_boton_volver() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                EquipoDetalleScreen(
                    equipoId = 999,
                    viewModel = viewModel,
                    onSolicitarClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Volver")
            .assertIsDisplayed()
    }

    @Test
    fun equipo_no_disponible_no_muestra_boton_solicitar() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                EquipoDetalleScreen(
                    equipoId = 3,
                    viewModel = viewModel,
                    onSolicitarClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Cámara Digital")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("RESERVADO")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Solicitar préstamo")
            .assertDoesNotExist()
    }
}