package com.example.prestamolab.ui.auth

import com.example.prestamolab.data.auth.AuthRepository
import com.example.prestamolab.data.auth.UsuariosAuthRepository
import com.example.prestamolab.model.Rol
import com.example.prestamolab.testutil.FakeSessionStore
import com.example.prestamolab.testutil.FakeUsuariosDataSource
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
        viewModel = LoginViewModel(UsuariosAuthRepository(FakeUsuariosDataSource(), store))
    }

    private fun escribir(identificador: String, contrasena: String) {
        viewModel.onIdentificadorChanged(identificador)
        viewModel.onContrasenaChanged(contrasena)
    }

    @Test
    fun `TC-HU10-01 - Credenciales validas inician sesion con el rol del usuario`() {
        escribir(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)

        viewModel.iniciarSesion()

        assertEquals(Rol.ESTUDIANTE, store.sesion.value?.usuario?.rol)
        val state = viewModel.uiState.value
        assertFalse(state.cargando)
        assertNull(state.mensajeError)
        assertEquals("", state.contrasena) // no se conserva la contraseña en memoria
    }

    @Test
    fun `Login con documento inicia sesion con el rol del usuario`() {
        escribir(FakeUsuariosDataSource.DOCUMENTO_INSTRUCTOR, FakeUsuariosDataSource.CONTRASENA)

        viewModel.iniciarSesion()

        assertEquals(Rol.INSTRUCTOR, store.sesion.value?.usuario?.rol)
        assertNull(viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `TC-HU10-02 - Credenciales incorrectas muestran mensaje y no inician sesion`() {
        escribir(FakeUsuariosDataSource.CORREO_ESTUDIANTE, "incorrecta")

        viewModel.iniciarSesion()

        assertEquals("Usuario o contraseña incorrectos", viewModel.uiState.value.mensajeError)
        assertNull(store.sesion.value)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `TC-HU10-03 - Campos vacios, correo o documento invalidos se validan sin llamar al servidor`() {
        val auth = mockk<AuthRepository>(relaxed = true)
        val vm = LoginViewModel(auth)

        vm.iniciarSesion()
        assertEquals("El correo o documento es obligatorio.", vm.uiState.value.errorIdentificador)
        assertEquals("La contraseña es obligatoria.", vm.uiState.value.errorContrasena)

        vm.onIdentificadorChanged("correo@sin-dominio")
        vm.onContrasenaChanged("clave")
        vm.iniciarSesion()
        assertEquals("Ingresa un correo válido.", vm.uiState.value.errorIdentificador)
        assertNull(vm.uiState.value.errorContrasena)

        vm.onIdentificadorChanged("12-3")
        vm.iniciarSesion()
        assertEquals("El documento debe tener entre 5 y 20 letras o números.", vm.uiState.value.errorIdentificador)

        coVerify(exactly = 0) { auth.iniciarSesion(any(), any()) }
    }

    @Test
    fun `Editar un campo limpia su error`() {
        viewModel.iniciarSesion()
        viewModel.onIdentificadorChanged("a")

        assertNull(viewModel.uiState.value.errorIdentificador)
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
        vm.onIdentificadorChanged("a@b.co")
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
        vm.onIdentificadorChanged("a@b.co")
        vm.onContrasenaChanged("clave")

        vm.iniciarSesion()

        assertEquals("No se pudo iniciar sesión. Intenta de nuevo.", vm.uiState.value.mensajeError)
    }
}
