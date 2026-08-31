package com.example.prestamolab.model

enum class CategoriaEquipo {
    ELECTRONICA,
    COMPUTO,
    HERRAMIENTAS,
    AUDIOVISUAL
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
