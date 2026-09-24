package com.example.prestamolab.ui.navigation

import com.example.prestamolab.model.Rol

object Rutas {
    const val PRESTAMOS = "prestamos"
    const val GESTION = "gestion"
    const val ARG_SOLICITUD_ID = "solicitudId"
    const val DEVOLUCION = "devolucion/{$ARG_SOLICITUD_ID}"

    fun devolucion(solicitudId: Int) = "devolucion/$solicitudId"
}

/** Matriz de permisos por ruta (RBAC). Una ruta que no está aquí se niega a todos. */
object ControlAcceso {
    private val permisos: Map<String, Set<Rol>> = mapOf(
        Rutas.PRESTAMOS to setOf(Rol.ESTUDIANTE, Rol.INSTRUCTOR),
        Rutas.GESTION to setOf(Rol.INSTRUCTOR),
        Rutas.DEVOLUCION to setOf(Rol.ESTUDIANTE)
    )

    fun puedeAcceder(ruta: String, rol: Rol): Boolean = rol in permisos[ruta].orEmpty()

    /** Solicitar, devolver y adjuntar evidencias son acciones exclusivas del estudiante. */
    fun puedeSolicitarPrestamo(rol: Rol): Boolean = rol == Rol.ESTUDIANTE
}
