package com.example.prestamolab.ui.auth

import com.example.prestamolab.data.auth.AuthRepository
import com.example.prestamolab.data.auth.DemoAuthRepository
import com.example.prestamolab.model.Rol
import com.example.prestamolab.testutil.FakeSessionStore
import com.example.prestamolab.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: FakeSessionStore
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        store = FakeSessionStore()
        viewModel = LoginViewModel(DemoAuthRepository(store))
    }

    private fun escribir(correo: String, contrasena: String) {
        viewModel.onCorreoChanged(correo)
        viewModel.onContrasenaChanged(contrasena)
    }

    @Test
    fun `TC-HU10-01 - Credenciales validas inician sesion con el rol del usuario`() {
        escribir(DemoAuthRepository.CORREO_ESTUDIANTE, DemoAuthRepository.CONTRASENA_ESTUDIANTE)

        viewModel.iniciarSesion()

        assertEquals(Rol.ESTUDIANTE, store.sesion.value?.usuario?.rol)
        val state = viewModel.uiState.value
        assertFalse(state.cargando)
        assertNull(state.mensajeError)
        assertEquals("", state.contrasena) // no se conserva la contraseña en memoria
    }

    @Test
    fun `TC-HU10-02 - Credenciales incorrectas muestran mensaje y no inician sesion`() {
        escribir(DemoAuthRepository.CORREO_ESTUDIANTE, "incorrecta")

        viewModel.iniciarSesion()

        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.mensajeError)
        assertNull(store.sesion.value)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `TC-HU10-03 - Campos vacios o correo invalido se validan sin llamar al servidor`() {
        val auth = mockk<AuthRepository>(relaxed = true)
        val vm = LoginViewModel(auth)

        vm.iniciarSesion()
        assertEquals("El correo es obligatorio.", vm.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria.", vm.uiState.value.errorContrasena)

        vm.onCorreoChanged("correo-sin-arroba")
        vm.onContrasenaChanged("clave")
        vm.iniciarSesion()
        assertEquals("Ingresa un correo válido.", vm.uiState.value.errorCorreo)
        assertNull(vm.uiState.value.errorContrasena)

        coVerify(exactly = 0) { auth.iniciarSesion(any(), any()) }
    }

    @Test
    fun `Editar un campo limpia su error`() {
        viewModel.iniciarSesion()
        viewModel.onCorreoChanged("a")

        assertNull(viewModel.uiState.value.errorCorreo)
        assertNotNull(viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun `Doble pulsacion en Ingresar hace una sola llamada`() = runTest {
        val respuesta = CompletableDeferred<Result<com.example.prestamolab.model.Sesion>>()
        val auth = mockk<AuthRepository> {
            every { sesion } returns MutableStateFlow(null)
            coEvery { iniciarSesion(any(), any()) } coAnswers { respuesta.await() }
        }
        val vm = LoginViewModel(auth)
        vm.onCorreoChanged("a@b.co")
        vm.onContrasenaChanged("clave")

        vm.iniciarSesion()
        assertTrue(vm.uiState.value.cargando)
        vm.iniciarSesion()

        respuesta.complete(Result.failure(IOException()))
        coVerify(exactly = 1) { auth.iniciarSesion(any(), any()) }
    }

    @Test
    fun `Error de red muestra mensaje generico`() = runTest {
        val auth = mockk<AuthRepository> {
            every { sesion } returns MutableStateFlow(null)
            coEvery { iniciarSesion(any(), any()) } returns Result.failure(IOException("timeout"))
        }
        val vm = LoginViewModel(auth)
        vm.onCorreoChanged("a@b.co")
        vm.onContrasenaChanged("clave")

        vm.iniciarSesion()

        assertEquals("No se pudo iniciar sesión. Intenta de nuevo.", vm.uiState.value.mensajeError)
    }
}
