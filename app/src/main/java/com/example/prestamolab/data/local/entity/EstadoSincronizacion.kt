package com.example.prestamolab.data.local.entity

/** Estado de un registro local frente a Supabase (HU-06, HU-07). */
enum class EstadoSincronizacion {
    /** Cambió en el dispositivo y aún no se envía. */
    PENDIENTE,
    /** Coincide con la última versión enviada o recibida. */
    SINCRONIZADO,
    /** El servidor lo rechazó (4xx distinto de 401/404); no se reintenta hasta que vuelva a cambiar. */
    ERROR
}
