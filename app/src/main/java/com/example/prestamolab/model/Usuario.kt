package com.example.prestamolab.model

enum class Rol { INSTRUCTOR, ESTUDIANTE }

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: Rol
)

/** Sesión activa: el usuario autenticado contra la tabla `users` de Supabase. */
data class Sesion(val usuario: Usuario)
