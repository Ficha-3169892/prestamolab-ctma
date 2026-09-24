package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/** Valida correo o documento y contraseña contra la tabla `users`, sin Supabase Auth. */
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
            remoto.buscarPorCredenciales(campo, consulta, contrasena)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
            ?: return Result.failure(CredencialesInvalidasException())

        // Un rol fuera de INSTRUCTOR/ESTUDIANTE no debe abrir ninguna ruta protegida
        val rol = Rol.entries.find { it.name == encontrado.rol }
            ?: return Result.failure(IllegalStateException("Rol no reconocido: ${encontrado.rol}"))

        val sesion = Sesion(Usuario(encontrado.id, encontrado.nombre, encontrado.email, rol))
        sessionStore.guardar(sesion)
        return Result.success(sesion)
    }

    override suspend fun cerrarSesion() = sessionStore.limpiar()
}
