package com.example.prestamolab.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.MainActivity
import com.example.prestamolab.data.auth.DemoAuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun empezarSinSesion() {
        reiniciarDatosSemilla()
        composeTestRule.cerrarSesionYEsperarLogin()
    }

    private fun escribirCredenciales(correo: String, contrasena: String) {
        composeTestRule.onNodeWithText("Correo institucional").performTextInput(correo)
        composeTestRule.onNodeWithText("Contraseña").performTextInput(contrasena)
        composeTestRule.onNodeWithText("Ingresar").performClick()
    }

    @Test
    fun TC_HU10_01_LoginValido_MuestraCatalogo() {
        escribirCredenciales(DemoAuthRepository.CORREO_ESTUDIANTE, DemoAuthRepository.CONTRASENA_ESTUDIANTE)

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Multímetro Digital").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Salir").assertIsDisplayed()
    }

    @Test
    fun TC_HU10_02_LoginInvalido_MuestraErrorYSigueEnLogin() {
        escribirCredenciales(DemoAuthRepository.CORREO_ESTUDIANTE, "incorrecta")

        composeTestRule.onNodeWithText("Correo o contraseña incorrectos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun TC_HU10_03_CamposVacios_MuestranErrores() {
        composeTestRule.onNodeWithText("Ingresar").performClick()

        composeTestRule.onNodeWithText("El correo es obligatorio.").assertIsDisplayed()
        composeTestRule.onNodeWithText("La contraseña es obligatoria.").assertIsDisplayed()
    }

    @Test
    fun TC_HU10_05_CerrarSesion_VuelveAlLogin() {
        composeTestRule.iniciarSesionComoEstudiante()

        composeTestRule.onNodeWithText("Salir").performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Correo institucional").assertIsDisplayed()
    }

    @Test
    fun TC_HU10_06_Estudiante_NoVeGestion() {
        composeTestRule.iniciarSesionComoEstudiante()

        composeTestRule.onNodeWithText("Gestión").assertDoesNotExist()
        composeTestRule.onAllNodesWithText("Mis Solicitudes")[0].assertIsDisplayed()
    }

    @Test
    fun TC_HU10_07_Instructor_VeYAbreGestion() {
        composeTestRule.iniciarSesionComoInstructor()

        composeTestRule.onNodeWithText("Gestión").performClick()

        composeTestRule.onNodeWithText("Gestión (Instructor)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inventario de equipos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Actividades formativas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Revisar solicitudes").assertIsDisplayed()
    }

    @Test
    fun TC_HU03_08_Instructor_NoVeSolicitarPrestamo() {
        composeTestRule.iniciarSesionComoInstructor()

        composeTestRule.onNodeWithText("Multímetro Digital").performClick()

        composeTestRule.onNodeWithText("Detalle del Equipo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solicitar Préstamo").assertDoesNotExist()
        composeTestRule.onNodeWithText("Mis Solicitudes").assertDoesNotExist()
    }
}
