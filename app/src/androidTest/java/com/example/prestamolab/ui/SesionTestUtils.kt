package com.example.prestamolab.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.auth.DemoAuthRepository
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import kotlinx.coroutines.runBlocking

private val app: PrestamoLabApp
    get() = ApplicationProvider.getApplicationContext()

/** Restaura los datos semilla: el repositorio vive en la Application y se comparte entre pruebas. */
fun reiniciarDatosSemilla() {
    (app.container.prestamoRepository as InMemoryPrestamoRepository).reiniciar()
}

fun ComposeTestRule.iniciarSesionComoEstudiante() = iniciarSesion(
    DemoAuthRepository.CORREO_ESTUDIANTE, DemoAuthRepository.CONTRASENA_ESTUDIANTE
)

fun ComposeTestRule.iniciarSesionComoInstructor() = iniciarSesion(
    DemoAuthRepository.CORREO_INSTRUCTOR, DemoAuthRepository.CONTRASENA_INSTRUCTOR
)

/** Inicia sesión directamente en el repositorio y espera a que se muestre el área autenticada. */
private fun ComposeTestRule.iniciarSesion(correo: String, contrasena: String) {
    runBlocking {
        app.container.authRepository.cerrarSesion()
        app.container.authRepository.iniciarSesion(correo, contrasena).getOrThrow()
    }
    waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Salir").fetchSemanticsNodes().isNotEmpty() }
}

fun ComposeTestRule.cerrarSesionYEsperarLogin() {
    runBlocking { app.container.authRepository.cerrarSesion() }
    waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty() }
}
