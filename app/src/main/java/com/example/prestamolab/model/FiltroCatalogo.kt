package com.example.prestamolab.model

/** Filtro del catálogo (CA-HU01-03); se conserva entre sesiones de la app (CA-HU01-04). */
data class FiltroCatalogo(
    val soloDisponibles: Boolean = false,
    /** null muestra todas las categorías. */
    val categoria: String? = null
) {
    val activo: Boolean get() = soloDisponibles || categoria != null

    fun aplicar(equipos: List<Equipo>): List<Equipo> = equipos.filter { equipo ->
        (!soloDisponibles || equipo.estado == EstadoEquipo.DISPONIBLE) &&
            (categoria == null || equipo.categoria == categoria)
    }

    companion object {
        /** Categorías que ofrece el filtro, en orden alfabético y sin repetir. */
        fun categorias(equipos: List<Equipo>): List<String> =
            equipos.map { it.categoria }.filter { it.isNotBlank() }.distinct().sorted()
    }
}
