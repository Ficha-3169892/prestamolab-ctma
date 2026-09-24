package com.example.prestamolab.data.auth

import com.example.prestamolab.model.Sesion
import kotlinx.coroutines.flow.Flow

class CredencialesInvalidasException : Exception("Usuario o contraseña incorrectos")

interface AuthRepository {
    /** Sesión actual; null cuando no hay usuario autenticado. */
    val sesion: Flow<Sesion?>

    /**
     * [identificador] es el correo o el documento del usuario. Falla con
     * [CredencialesInvalidasException] si no coincide con la contraseña, o con
     * [java.io.IOException] si no hay conexión con el servidor.
     */
    suspend fun iniciarSesion(identificador: String, contrasena: String): Result<Sesion>

    suspend fun cerrarSesion()
}
