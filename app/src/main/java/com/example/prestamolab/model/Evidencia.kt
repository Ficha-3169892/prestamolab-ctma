package com.example.prestamolab.model

/** Mismos valores que el CHECK de evidences.stage en Supabase. */
enum class EtapaEvidencia(val etiqueta: String) {
    ENTREGA("al recibir el equipo"),
    DEVOLUCION("al devolver el equipo")
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
    val fecha: String
)
