package com.example.prestamolab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.repository.PrestamoRepository
import com.example.prestamolab.ui.navigation.AppNavigation
import com.example.prestamolab.viewmodel.PrestamoViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FakeAndroidPrestamoRepository : PrestamoRepository {
    val equipos = mutableListOf(
        Equipo(1, "Kit de electrónica instrumental", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Taladro percutor", CategoriaEquipo.HERRAMIENTA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Osciloscopio", CategoriaEquipo.ELECTRONICA, EstadoEquipo.RESERVADO),
        Equipo(4, "Laptop de diseño", CategoriaEquipo.COMPUTO, EstadoEquipo.PRESTADO)
    )
    val solicitudes = mutableListOf<com.example.prestamolab.model.SolicitudPrestamo>()

    override fun obtenerEquipos() = equipos
    override fun obtenerEquipo(id: Int) = equipos.find { it.id == id }
    override fun obtenerSolicitudes() = solicitudes
    override fun obtenerSolicitud(id: Int) = solicitudes.find { it.id == id }

    override fun crearSolicitud(solicitud: com.example.prestamolab.model.SolicitudPrestamo): Result<Unit> {
        solicitudes.add(solicitud)
        val idx = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (idx != -1) {
            equipos[idx] = equipos[idx].copy(estado = EstadoEquipo.RESERVADO)
        }
        return Result.success(Unit)
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val s = obtenerSolicitud(id)
        if (s != null) {
            val idx = solicitudes.indexOfFirst { it.id == id }
            solicitudes[idx] = s.copy(estado = com.example.prestamolab.model.EstadoSolicitud.CANCELADA)
            val eIdx = equipos.indexOfFirst { it.id == s.equipoId }
            if (eIdx != -1) {
                equipos[eIdx] = equipos[eIdx].copy(estado = EstadoEquipo.DISPONIBLE)
            }
        }
        return Result.success(Unit)
    }
}

class AppNavigationInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setUp() {
        val repo = FakeAndroidPrestamoRepository()
        viewModel = PrestamoViewModel(repo)
    }

    // --- 15 MÉTODOS DE PRUEBA INDEPENDIENTES PARA CUMPLIR EL REQUERIMIENTO EXACTO ---

    @Test
    fun ti01_verificarVisualizacionDeTitulosYTarjetasIniciales() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("PréstamoLab CTMA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").assertIsDisplayed()
    }

    @Test
    fun ti02_verificarQueElEstadoSeVisualiceEnTextoLegible() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        // Al haber múltiples tarjetas con "Estado: DISPONIBLE", usamos onAllNodesWithText
        // para asegurar que al menos uno de ellos existe y es visible en pantalla.
        composeTestRule.onAllNodesWithText("Estado: DISPONIBLE")[0].assertIsDisplayed()
    }

    @Test
    fun ti03_verificarNavegacionHaciaLaPantallaDeDetalle() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Volver").assertIsDisplayed()
    }

    @Test
    fun ti04_verificarVisibilidadDeDatosDelEquipoEnDetalle() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Categoría: ELECTRONICA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado: DISPONIBLE").assertIsDisplayed()
    }

    @Test
    fun ti05_verificarBotonSolicitarDisponibleSiEquipoEstaDisponible() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").assertIsDisplayed()
    }

    @Test
    fun ti06_verificarBotonSolicitudOcultoSiEquipoNoEstaDisponible() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Osciloscopio").performClick()
        composeTestRule.onNodeWithText("Este equipo no está disponible.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solicitar préstamo").assertDoesNotExist()
    }

    @Test
    fun ti07_verificarBotonVolverRegresaAlCatalogoDesdeDetalle() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Volver").performClick()
        composeTestRule.onNodeWithText("Catálogo de equipos").assertIsDisplayed()
    }

    @Test
    fun ti08_verificarValidacionVisualEnVivoContadorDeCaracteres() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()
        
        composeTestRule.onNodeWithText("Propósito").performTextInput("Ayuda")
        // "Ayuda" tiene exactamente 5 caracteres.
        composeTestRule.onNodeWithText("5/180 caracteres").assertIsDisplayed()
    }

    @Test
    fun ti09_verificarMensajeErrorAlEnviarDestinoVacio() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()
        
        composeTestRule.onNode(hasText("Crear préstamo") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("Debes ingresar el ambiente de destino").assertIsDisplayed()
    }

    @Test
    fun ti10_verificarMensajeErrorConPropositoMenorDeDiezCaracteres() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()
        
        composeTestRule.onNodeWithText("Ambiente de destino").performTextInput("Ambiente 201")
        composeTestRule.onNodeWithText("Propósito").performTextInput("Corto")
        composeTestRule.onNode(hasText("Crear préstamo") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("El propósito debe tener entre 10 y 180 caracteres").assertIsDisplayed()
    }

    @Test
    fun ti11_verificarMensajeErrorConDuracionFueraDeRango() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()
        
        composeTestRule.onNodeWithText("Ambiente de destino").performTextInput("Ambiente 201")
        composeTestRule.onNodeWithText("Propósito").performTextInput("Práctica formativa de soldadura")
        composeTestRule.onNodeWithText("Duración en horas").performTextInput("12")
        composeTestRule.onNode(hasText("Crear préstamo") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("La duración debe estar entre 1 y 8 horas").assertIsDisplayed()
    }

    @Test
    fun ti12_verificarRegistroExitosoYRedireccionAMisPrestamos() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()

        composeTestRule.onNodeWithText("Ambiente de destino").performTextInput("Ambiente 502")
        composeTestRule.onNodeWithText("Propósito").performTextInput("Desarrollo de laboratorios de automatización de PLC")
        composeTestRule.onNodeWithText("Duración en horas").performTextInput("5")
        composeTestRule.onNode(hasText("Crear préstamo") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Ambiente: Ambiente 502").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado: SOLICITADA").assertIsDisplayed()
    }

    @Test
    fun ti13_verificarPantallaMisPrestamosVaciaSiNoHayRegistros() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Mis préstamos").performClick()
        composeTestRule.onNodeWithText("No tienes préstamos registrados.").assertIsDisplayed()
    }

    @Test
    fun ti14_verificarCancelacionInteractivaDeLaSolicitud() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()

        composeTestRule.onNodeWithText("Ambiente de destino").performTextInput("Ambiente 502")
        composeTestRule.onNodeWithText("Propósito").performTextInput("Desarrollo de laboratorios de automatización de PLC")
        composeTestRule.onNodeWithText("Duración en horas").performTextInput("5")
        composeTestRule.onNode(hasText("Crear préstamo") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Cancelar solicitud").performClick()
        composeTestRule.onNodeWithText("Estado: CANCELADA").assertIsDisplayed()
    }

    @Test
    fun ti15_verificarBotonCancelarDesapareceUnaVezCanceladaLaSolicitud() {
        composeTestRule.setContent { AppNavigation(viewModel = viewModel) }
        composeTestRule.onNodeWithText("Kit de electrónica instrumental").performClick()
        composeTestRule.onNodeWithText("Solicitar préstamo").performClick()

        composeTestRule.onNodeWithText("Ambiente de destino").performTextInput("Ambiente 502")
        composeTestRule.onNodeWithText("Propósito").performTextInput("Desarrollo de laboratorios de automatización de PLC")
        composeTestRule.onNodeWithText("Duración en horas").performTextInput("5")
        composeTestRule.onNode(hasText("Crear préstamo") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Cancelar solicitud").performClick()
        composeTestRule.onNodeWithText("Cancelar solicitud").assertDoesNotExist()
    }
}
