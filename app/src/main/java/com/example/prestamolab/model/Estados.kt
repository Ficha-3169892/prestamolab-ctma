package com.example.prestamolab.model

enum class CategoriaEquipo {
    ELECTRONICA,
    HERRAMIENTAS,
    MEDICION,
    COMPUTO,
    PERIFERICOS
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
