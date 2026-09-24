package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Sesion
import kotlinx.coroutines.flow.Flow

class CredencialesInvalidasException : Exception("Correo o contraseña incorrectos")

interface AuthRepository {
    /** Sesión actual; null cuando no hay usuario autenticado. */
    val sesion: Flow<Sesion?>

    /** Falla con [CredencialesInvalidasException] si el correo o la contraseña no coinciden. */
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Sesion>

    suspend fun cerrarSesion()
}
