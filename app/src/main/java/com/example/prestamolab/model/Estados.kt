package com.example.prestamolab.model

enum class CategoriaEquipo {
    ELECTRONICA,
    HERRAMIENTA,
    COMPUTO,
    OTRO
}

enum class EstadoEquipo {
    DISPONIBLE,
    RESERVADO,
    PRESTADO
}

enum class EstadoSolicitud {
    SOLICITADA,
    APROBADA,
    ENTREGADA,
    DEVUELTA,
    CANCELADA,
    RECHAZADA
}