package com.example.prestamolab.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.model.FiltroCatalogo
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.runBlocking

private val app: PrestamoLabApp
    get() = ApplicationProvider.getApplicationContext()

/** Restaura los datos semilla: el repositorio vive en la Application y se comparte entre pruebas. */
fun reiniciarDatosSemilla() {
    DatosSemilla.reiniciar(app.container.database)
    runBlocking { app.container.preferenciasCatalogo.guardar(FiltroCatalogo()) }
}

fun ComposeTestRule.iniciarSesionComoEstudiante() = iniciarSesion(
    FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA
)

fun ComposeTestRule.iniciarSesionComoInstructor() = iniciarSesion(
    FakeUsuariosDataSource.CORREO_INSTRUCTOR, FakeUsuariosDataSource.CONTRASENA
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

/**
 * Toca un botón de Mis Solicitudes. Las tarjetas muestran ambiente y fechas (CA-HU04-02): en pantallas pequeñas
 * los botones del segundo préstamo quedan bajo el borde y hay que desplazarse, como haría el usuario.
 */
fun ComposeTestRule.tocarEnMisSolicitudes(texto: String) {
    onNode(hasScrollAction()).performScrollToNode(hasText(texto))
    onNodeWithText(texto).performClick()
}
