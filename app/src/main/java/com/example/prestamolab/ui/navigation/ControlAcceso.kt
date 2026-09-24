package com.example.prestamolab.ui.navigation

import com.example.prestamolab.model.Rol

object Rutas {
    const val ARG_EQUIPO_ID = "equipoId"
    const val ARG_SOLICITUD_ID = "solicitudId"

    const val CATALOGO = "catalogo"
    const val DETALLE_EQUIPO = "equipo/{$ARG_EQUIPO_ID}"
    const val SOLICITUD = "equipo/{$ARG_EQUIPO_ID}/solicitud"
    const val MIS_SOLICITUDES = "mis-solicitudes"
    const val DEVOLUCION = "devolucion/{$ARG_SOLICITUD_ID}"
    const val GESTION = "gestion"
    const val REVISAR_SOLICITUDES = "gestion/solicitudes"

    fun detalleEquipo(equipoId: Int) = "equipo/$equipoId"
    fun solicitud(equipoId: Int) = "equipo/$equipoId/solicitud"
    fun devolucion(solicitudId: Int) = "devolucion/$solicitudId"
}

/** Matriz de permisos por ruta (RBAC). Una ruta que no está aquí se niega a todos. */
object ControlAcceso {
    private val todos = setOf(Rol.ESTUDIANTE, Rol.INSTRUCTOR)

    private val permisos: Map<String, Set<Rol>> = mapOf(
        Rutas.CATALOGO to todos,
        Rutas.DETALLE_EQUIPO to todos,
        // Solicitar, ver las propias solicitudes y devolver son acciones del estudiante
        Rutas.SOLICITUD to setOf(Rol.ESTUDIANTE),
        Rutas.MIS_SOLICITUDES to setOf(Rol.ESTUDIANTE),
        Rutas.DEVOLUCION to setOf(Rol.ESTUDIANTE),
        Rutas.GESTION to setOf(Rol.INSTRUCTOR),
        Rutas.REVISAR_SOLICITUDES to setOf(Rol.INSTRUCTOR)
    )

    fun puedeAcceder(ruta: String, rol: Rol): Boolean = rol in permisos[ruta].orEmpty()

    /** Solicitar, devolver y adjuntar evidencias son acciones exclusivas del estudiante. */
    fun puedeSolicitarPrestamo(rol: Rol): Boolean = puedeAcceder(Rutas.SOLICITUD, rol)

    /** Pantalla de inicio tras el login: el instructor entra a Gestión y el estudiante al Catálogo. */
    fun rutaInicio(rol: Rol): String = when (rol) {
        Rol.INSTRUCTOR -> Rutas.GESTION
        Rol.ESTUDIANTE -> Rutas.CATALOGO
    }

    /** Destinos de la barra inferior según el rol, en orden. */
    fun destinosPrincipales(rol: Rol): List<String> =
        listOf(Rutas.CATALOGO, Rutas.MIS_SOLICITUDES, Rutas.GESTION).filter { puedeAcceder(it, rol) }
}
