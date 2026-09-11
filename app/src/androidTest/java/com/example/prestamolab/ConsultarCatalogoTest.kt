package com.example.prestamolab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import com.example.prestamolab.ui.catalogo.CatalogoScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel
import org.junit.Rule
import org.junit.Test

class ConsultarCatalogoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun catalogo_muestra_titulo() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                CatalogoScreen(
                    viewModel = viewModel,
                    onEquipoClick = {},
                    onMisSolicitudesClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Catálogo de equipos")
            .assertIsDisplayed()
    }

    @Test
    fun catalogo_muestra_descripcion() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                CatalogoScreen(
                    viewModel = viewModel,
                    onEquipoClick = {},
                    onMisSolicitudesClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Consulta los recursos disponibles")
            .assertIsDisplayed()
    }

    @Test
    fun catalogo_muestra_elementos() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                CatalogoScreen(
                    viewModel = viewModel,
                    onEquipoClick = {},
                    onMisSolicitudesClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Multímetro Digital")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Kit de Arduino")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Cámara Digital")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Portátil Lenovo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Taladro Inalámbrico")
            .assertIsDisplayed()
    }

    @Test
    fun catalogo_muestra_informacion_basica() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                CatalogoScreen(
                    viewModel = viewModel,
                    onEquipoClick = {},
                    onMisSolicitudesClick = {}
                )
            }
        }

        // Usamos onAllNodesWithText y seleccionamos el primero porque hay varios equipos
        // con la misma categoría y estado en los datos iniciales.
        composeTestRule
            .onAllNodesWithText("Categoría: ELECTRONICA")
            .onFirst()
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithText("Estado: DISPONIBLE")
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun catalogo_muestra_boton_mis_solicitudes() {
        val viewModel = PrestamoViewModel()

        composeTestRule.setContent {
            MaterialTheme {
                CatalogoScreen(
                    viewModel = viewModel,
                    onEquipoClick = {},
                    onMisSolicitudesClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Mis solicitudes")
            .assertIsDisplayed()
    }
}