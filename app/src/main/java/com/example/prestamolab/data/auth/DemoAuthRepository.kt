package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Autenticación local con cuentas de demostración, mientras se integra Supabase Auth.
 * Las credenciales son solo para el prototipo y no deben llegar a producción.
 */
class DemoAuthRepository(
    private val sessionStore: SessionStore,
    private val generarToken: () -> String = { UUID.randomUUID().toString() }
) : AuthRepository {

    override val sesion: Flow<Sesion?> = sessionStore.sesion

    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Sesion> {
        val cuenta = CUENTAS[correo.trim().lowercase()]
        if (cuenta == null || cuenta.contrasena != contrasena) {
            return Result.failure(CredencialesInvalidasException())
        }
        val sesion = Sesion(cuenta.usuario, generarToken())
        sessionStore.guardar(sesion)
        return Result.success(sesion)
    }

    override suspend fun cerrarSesion() = sessionStore.limpiar()

    private class Cuenta(val usuario: Usuario, val contrasena: String)

    companion object {
        const val CORREO_ESTUDIANTE = "estudiante@ctma.edu.co"
        const val CONTRASENA_ESTUDIANTE = "Estudiante123"
        const val CORREO_INSTRUCTOR = "instructor@ctma.edu.co"
        const val CONTRASENA_INSTRUCTOR = "Instructor123"

        private val CUENTAS = listOf(
            Cuenta(Usuario("u-estudiante", "Andrés Vargas", CORREO_ESTUDIANTE, Rol.ESTUDIANTE), CONTRASENA_ESTUDIANTE),
            Cuenta(Usuario("u-instructor", "Instructor CTMA", CORREO_INSTRUCTOR, Rol.INSTRUCTOR), CONTRASENA_INSTRUCTOR)
        ).associateBy { it.usuario.correo }
    }
}
