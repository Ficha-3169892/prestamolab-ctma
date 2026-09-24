package com.example.prestamolab.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HU-12: inventario de equipos del instructor sobre los datos semilla. */
@RunWith(AndroidJUnit4::class)
class InventarioUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepararDatos() {
        reiniciarDatosSemilla()
    }

    private fun abrirInventarioComoInstructor() {
        composeTestRule.iniciarSesionComoInstructor()
        composeTestRule.onNodeWithText("Inventario de equipos").performClick()
    }

    /** En pantallas pequeñas la tarjeta puede quedar bajo la barra inferior: primero se desplaza. */
    private fun tocarEnLaLista(etiqueta: String) {
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag(etiqueta))
        composeTestRule.onNodeWithTag(etiqueta).performClick()
    }

    private fun abrirEnElCatalogo(nombre: String) {
        composeTestRule.onNodeWithText("Catálogo").performClick()
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText(nombre))
        composeTestRule.onNodeWithText(nombre).performClick()
    }

    @Test
    fun TC_HU12_01_RegistrarEquipo_ApareceDisponibleEnElCatalogo() {
        abrirInventarioComoInstructor()
        composeTestRule.onNodeWithText("Nuevo equipo").performClick()
        composeTestRule.onNodeWithText("Nombre del equipo").performTextInput("Proyector Epson")
        composeTestRule.onNodeWithText("Categoría").performTextInput("Audiovisual")
        composeTestRule.onNodeWithText("Guardar equipo").performClick()

        composeTestRule.onNodeWithText("Inventario (6)").assertIsDisplayed()
        abrirEnElCatalogo("Proyector Epson")
        composeTestRule.onNodeWithText("Estado: DISPONIBLE").assertIsDisplayed()
    }

    @Test
    fun TC_HU12_02_CamposVacios_MuestranErroresYNoSeGuarda() {
        abrirInventarioComoInstructor()
        composeTestRule.onNodeWithText("Nuevo equipo").performClick()
        composeTestRule.onNodeWithText("Guardar equipo").performClick()

        composeTestRule.onNodeWithText("El nombre es obligatorio.").assertIsDisplayed()
        composeTestRule.onNodeWithText("La categoría es obligatoria.").assertIsDisplayed()
        composeTestRule.onNodeWithText("← Inventario").performClick()
        composeTestRule.onNodeWithText("Inventario (5)").assertIsDisplayed()
    }

    @Test
    fun TC_HU12_03_EditarEquipo_SeVeEnElCatalogoYEnElDetalle() {
        abrirInventarioComoInstructor()
        tocarEnLaLista("editar-1")
        composeTestRule.onNodeWithText("Editar equipo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Multímetro Digital").performTextReplacement("Multímetro Fluke")
        composeTestRule.onNodeWithText("Guardar equipo").performClick()

        composeTestRule.onNodeWithText("Multímetro Fluke").assertIsDisplayed()
        abrirEnElCatalogo("Multímetro Fluke")
        composeTestRule.onNodeWithText("Detalle del Equipo").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Multímetro Fluke", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun TC_HU12_04_EliminarEquipoConPrestamoActivo_MuestraElMotivo() {
        abrirInventarioComoInstructor()
        tocarEnLaLista("eliminar-2")
        composeTestRule.onNodeWithText("Confirmar eliminación").performClick()

        composeTestRule.onNodeWithText("No se puede eliminar: el equipo tiene un préstamo activo.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inventario (5)").assertIsDisplayed()
    }

    @Test
    fun EliminarEquipoSinPrestamos_LoQuitaDelInventario() {
        abrirInventarioComoInstructor()
        tocarEnLaLista("eliminar-3")
        composeTestRule.onNodeWithText("Confirmar eliminación").performClick()

        composeTestRule.onNodeWithText("Inventario (4)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cautín Estación de Soldadura").assertDoesNotExist()
    }

    @Test
    fun CancelarLaEliminacion_ConservaElEquipo() {
        abrirInventarioComoInstructor()
        tocarEnLaLista("eliminar-3")
        composeTestRule.onNodeWithText("Cancelar").performClick()

        composeTestRule.onNodeWithText("Inventario (5)").assertIsDisplayed()
    }

    @Test
    fun TC_HU12_05_ElEstudianteNoVeOpcionesDeInventario() {
        composeTestRule.iniciarSesionComoEstudiante()

        composeTestRule.onNodeWithText("Multímetro Digital").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nuevo equipo").assertDoesNotExist()
        composeTestRule.onNodeWithText("Editar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Eliminar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Gestión").assertDoesNotExist()
    }
}
