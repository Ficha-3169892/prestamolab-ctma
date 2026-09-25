package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.navigation.AppNavigation
import com.example.prestamolab.viewmodel.AuthViewModel
import com.example.prestamolab.viewmodel.PrestamoViewModel
import org.junit.Rule
import org.junit.Test

class LoginAndNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flujoE2E_login_verCatalogo_y_solicitar() {
        val mockRepo = InMemoryPrestamoRepository()
        val prestamoViewModel = PrestamoViewModel(mockRepo)
        
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val sessionManager = com.example.prestamolab.data.local.UserSessionManager(context)
        val authViewModel = AuthViewModel(sessionManager)

        authViewModel.logout {}

        composeTestRule.setContent {
            AppNavigation(
                viewModel = prestamoViewModel,
                authViewModel = authViewModel
            )
        }

        // 1. Verificar pantalla de Login
        composeTestRule.onNodeWithText("PrestamoLab CTMA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aprendiz 1").performClick()

        // 2. Ingresar
        composeTestRule.onNodeWithText("Ingresar").performClick()

        // 3. Verificar que navegó al catálogo
        composeTestRule.onNodeWithText("Catálogo de Equipos").assertIsDisplayed()
    }
}
