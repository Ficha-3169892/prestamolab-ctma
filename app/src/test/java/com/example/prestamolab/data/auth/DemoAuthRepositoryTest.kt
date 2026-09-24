package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Rol
import com.example.prestamolab.testutil.FakeSessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DemoAuthRepositoryTest {

    private lateinit var store: FakeSessionStore
    private lateinit var repository: DemoAuthRepository

    @Before
    fun setup() {
        store = FakeSessionStore()
        repository = DemoAuthRepository(store, generarToken = { "token-fijo" })
    }

    @Test
    fun `credenciales validas guardan la sesion con rol y token`() = runTest {
        val sesion = repository.iniciarSesion(
            DemoAuthRepository.CORREO_INSTRUCTOR, DemoAuthRepository.CONTRASENA_INSTRUCTOR
        ).getOrThrow()

        assertEquals(Rol.INSTRUCTOR, sesion.usuario.rol)
        assertEquals("token-fijo", sesion.token)
        assertEquals(sesion, store.sesion.value)
    }

    @Test
    fun `el correo no distingue mayusculas ni espacios`() = runTest {
        val resultado = repository.iniciarSesion(
            "  Estudiante@CTMA.edu.co ", DemoAuthRepository.CONTRASENA_ESTUDIANTE
        )

        assertEquals(Rol.ESTUDIANTE, resultado.getOrThrow().usuario.rol)
    }

    @Test
    fun `contrasena incorrecta falla y no guarda sesion`() = runTest {
        val resultado = repository.iniciarSesion(DemoAuthRepository.CORREO_ESTUDIANTE, "otra")

        assertTrue(resultado.exceptionOrNull() is CredencialesInvalidasException)
        assertNull(store.sesion.value)
    }

    @Test
    fun `la contrasena distingue mayusculas`() = runTest {
        val resultado = repository.iniciarSesion(
            DemoAuthRepository.CORREO_ESTUDIANTE, DemoAuthRepository.CONTRASENA_ESTUDIANTE.lowercase()
        )

        assertTrue(resultado.isFailure)
    }

    @Test
    fun `cerrar sesion limpia el almacenamiento`() = runTest {
        repository.iniciarSesion(DemoAuthRepository.CORREO_ESTUDIANTE, DemoAuthRepository.CONTRASENA_ESTUDIANTE)

        repository.cerrarSesion()

        assertNull(store.sesion.value)
    }
}
