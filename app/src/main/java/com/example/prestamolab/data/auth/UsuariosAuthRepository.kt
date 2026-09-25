package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Valida correo o documento y contraseña contra la tabla `users`, sin Supabase Auth.
 * La contraseña en texto plano no sale de aquí: se envía su hash SHA-256.
 */
class UsuariosAuthRepository(
    private val remoto: UsuariosRemoteDataSource,
    private val sessionStore: SessionStore
) : AuthRepository {

    override val sesion: Flow<Sesion?> = sessionStore.sesion

    override suspend fun iniciarSesion(identificador: String, contrasena: String): Result<Sesion> {
        val valor = identificador.trim()
        val campo = if ('@' in valor) CampoIdentificador.CORREO else CampoIdentificador.DOCUMENTO
        val consulta = if (campo == CampoIdentificador.CORREO) valor.lowercase() else valor

        val encontrado = try {
            remoto.buscarPorCredenciales(campo, consulta, HashUtils.sha256(contrasena))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
            ?: return Result.failure(CredencialesInvalidasException())

        // Un rol fuera de INSTRUCTOR/ESTUDIANTE no debe abrir ninguna ruta protegida
        val rol = Rol.entries.find { it.name == encontrado.rol }
            ?: return Result.failure(IllegalStateException("Rol no reconocido: ${encontrado.rol}"))

        val sesion = Sesion(Usuario(encontrado.id, encontrado.nombre, encontrado.email, rol), encontrado.token)
        sessionStore.guardar(sesion)
        return Result.success(sesion)
    }

    override suspend fun cerrarSesion() {
        // Mejor esfuerzo: sin red el token vence solo (7 días); la sesión local se borra igual
        try {
            remoto.cerrarSesion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Se ignora a propósito
        }
        sessionStore.limpiar()
    }
}
