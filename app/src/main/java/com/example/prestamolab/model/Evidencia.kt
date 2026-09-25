package com.example.prestamolab.model

/** Mismos valores que el CHECK de evidences.stage en Supabase. */
enum class EtapaEvidencia(val etiqueta: String) {
    ENTREGA("al recibir el equipo"),
    DEVOLUCION("al devolver el equipo")
}

/** Dónde está la evidencia frente a Supabase (actividad 32 de la guía). */
enum class EstadoEvidencia { LOCAL, SINCRONIZADA, FALLIDA }

/**
 * Texto que ve el estudiante. "Subiendo" no es un estado guardado: es una evidencia LOCAL mientras la
 * sincronización está en curso.
 */
fun etiquetaEstadoEvidencia(estado: EstadoEvidencia, sincronizando: Boolean): String = when (estado) {
    EstadoEvidencia.LOCAL -> if (sincronizando) "Subiendo…" else "Local: pendiente de subir"
    EstadoEvidencia.SINCRONIZADA -> "Sincronizada"
    EstadoEvidencia.FALLIDA -> "Fallida: el servidor no la aceptó"
}

/** Foto que deja constancia del estado del equipo (HU-08). */
data class Evidencia(
    val id: Int,
    val solicitudId: Int,
    val etapa: EtapaEvidencia,
    /** content:// del FileProvider de la app. */
    val uriLocal: String,
    /** URL pública en Supabase Storage; null mientras no se sube (CA-HU08-05). */
    val urlRemota: String?,
    val fecha: String,
    /** Dónde se tomó; null si el estudiante no ha concedido la ubicación. */
    val latitud: Double? = null,
    val longitud: Double? = null,
    val estado: EstadoEvidencia = EstadoEvidencia.LOCAL
)
