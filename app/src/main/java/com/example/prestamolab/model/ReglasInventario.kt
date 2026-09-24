package com.example.prestamolab.model

/** Errores por campo del formulario de equipo; null significa que el campo es válido. */
data class ErroresEquipo(val nombre: String? = null, val categoria: String? = null) {
    val hayErrores: Boolean get() = nombre != null || categoria != null
}

/** Reglas del inventario que mantiene el instructor (HU-12). */
object ReglasInventario {
    const val NOMBRE_MAXIMO = 80
    const val CATEGORIA_MAXIMA = 40

    /** CA-HU12-02: nombre y categoría son obligatorios. */
    fun validar(nombre: String, categoria: String) = ErroresEquipo(
        nombre = when {
            nombre.isBlank() -> "El nombre es obligatorio."
            nombre.trim().length > NOMBRE_MAXIMO -> "Máximo $NOMBRE_MAXIMO caracteres"
            else -> null
        },
        categoria = when {
            categoria.isBlank() -> "La categoría es obligatoria."
            categoria.trim().length > CATEGORIA_MAXIMA -> "Máximo $CATEGORIA_MAXIMA caracteres"
            else -> null
        }
    )

    /**
     * CA-HU12-04: un equipo con préstamo activo no se elimina. Tampoco uno con préstamos cerrados:
     * su historial debe conservarse (y en Supabase loans.equipment_id lo referencia).
     */
    fun validarEliminacion(prestamos: List<EstadoSolicitud>): Result<Unit> = when {
        prestamos.any { it == EstadoSolicitud.SOLICITADA || it == EstadoSolicitud.PRESTADO } ->
            Result.failure(IllegalStateException("No se puede eliminar: el equipo tiene un préstamo activo."))
        prestamos.isNotEmpty() ->
            Result.failure(IllegalStateException("No se puede eliminar: el equipo tiene préstamos registrados y se conserva su historial."))
        else -> Result.success(Unit)
    }
}
