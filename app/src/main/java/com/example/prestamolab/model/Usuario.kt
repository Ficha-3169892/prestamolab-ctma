package com.example.prestamolab.model

enum class Rol { INSTRUCTOR, ESTUDIANTE }

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: Rol
)

/**
 * Sesión activa: el usuario autenticado contra la tabla `users` de Supabase. [token] lo emite
 * iniciar_sesion() en el servidor (R-04) y viaja en la cabecera x-sesion; null en sesiones anteriores a
 * docs/supabase/009_seguridad.sql, que el servidor rechaza y obligan a iniciar sesión de nuevo.
 */
data class Sesion(val usuario: Usuario, val token: String? = null)
