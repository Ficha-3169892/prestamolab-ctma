package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.ui.equipo.EquipoDetalleScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EquipoDetalleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // TC-03: EquipoId inexistente
    @Test
    fun equipoInexistenteMuestraEstadoRecuperable() {
        composeTestRule.setContent {
            EquipoDetalleScreen(
                equipo = null,
                onSolicitarClick = {},
                onVolver = {}
            )
        }

        composeTestRule
            .onNodeWithText("Equipo no encontrado o ID inexistente.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Volver")
            .assertIsDisplayed()
    }
}
