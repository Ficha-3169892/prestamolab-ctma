package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.FakeSessionStore
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class UsuariosAuthRepositoryTest {

    private lateinit var store: FakeSessionStore
    private lateinit var remoto: FakeUsuariosDataSource
    private lateinit var repository: UsuariosAuthRepository

    @Before
    fun setup() {
        store = FakeSessionStore()
        remoto = FakeUsuariosDataSource()
        repository = UsuariosAuthRepository(remoto, store)
    }

    @Test
    fun `correo y contrasena validos guardan id, correo y rol en la sesion`() = runTest {
        val sesion = repository.iniciarSesion(
            FakeUsuariosDataSource.CORREO_INSTRUCTOR, FakeUsuariosDataSource.CONTRASENA
        ).getOrThrow()

        assertEquals(
            Sesion(Usuario("uuid-instructor", "Instructor CTMA", "instructor@sena.edu.co", Rol.INSTRUCTOR)),
            sesion
        )
        assertEquals(sesion, store.sesion.value)
    }

    @Test
    fun `sin arroba se busca por documento`() = runTest {
        val sesion = repository.iniciarSesion(
            " ${FakeUsuariosDataSource.DOCUMENTO_ESTUDIANTE} ", FakeUsuariosDataSource.CONTRASENA
        ).getOrThrow()

        assertEquals(Rol.ESTUDIANTE, sesion.usuario.rol)
        assertEquals(CampoIdentificador.DOCUMENTO to "67890", remoto.consultas.single())
    }

    @Test
    fun `el correo se envia sin espacios y en minusculas`() = runTest {
        val resultado = repository.iniciarSesion("  Estudiante@SENA.edu.co ", FakeUsuariosDataSource.CONTRASENA)

        assertEquals(Rol.ESTUDIANTE, resultado.getOrThrow().usuario.rol)
        assertEquals(CampoIdentificador.CORREO to "estudiante@sena.edu.co", remoto.consultas.single())
    }

    @Test
    fun `contrasena incorrecta falla con credenciales invalidas y no guarda sesion`() = runTest {
        val resultado = repository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, "otra")

        assertTrue(resultado.exceptionOrNull() is CredencialesInvalidasException)
        assertNull(store.sesion.value)
    }

    @Test
    fun `se envia el hash SHA-256 de la contrasena, nunca el texto plano`() = runTest {
        repository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)

        assertEquals(FakeUsuariosDataSource.HASH_CONTRASENA, remoto.hashes.single())
    }

    @Test
    fun `sin conexion devuelve el error de red y no guarda sesion`() = runTest {
        remoto.error = IOException("sin red")

        val resultado = repository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)

        assertTrue(resultado.exceptionOrNull() is IOException)
        assertNull(store.sesion.value)
    }

    @Test
    fun `un rol desconocido en la tabla no abre sesion`() = runTest {
        val admin = FakeUsuariosDataSource.Fila(
            UsuarioRemoto("uuid-admin", "admin@sena.edu.co", "Admin", "ADMIN"), "99999", HashUtils.sha256("123")
        )
        repository = UsuariosAuthRepository(FakeUsuariosDataSource(listOf(admin)), store)

        val resultado = repository.iniciarSesion("admin@sena.edu.co", "123")

        assertTrue(resultado.isFailure)
        assertNull(store.sesion.value)
    }

    @Test
    fun `cerrar sesion limpia el almacenamiento`() = runTest {
        repository.iniciarSesion(FakeUsuariosDataSource.CORREO_ESTUDIANTE, FakeUsuariosDataSource.CONTRASENA)

        repository.cerrarSesion()

        assertNull(store.sesion.value)
    }
}
