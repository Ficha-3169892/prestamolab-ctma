package com.example.prestamolab.model

enum class CategoriaEquipo {
    ELECTRONICA,
    HERRAMIENTAS,
    MEDICION,
    COMPUTACION,
    VARIOS
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