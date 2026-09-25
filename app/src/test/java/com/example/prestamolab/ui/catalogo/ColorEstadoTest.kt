package com.example.prestamolab.ui.catalogo

import com.example.prestamolab.model.EstadoEquipo
import org.junit.Assert.*
import org.junit.Test

class ColorEstadoTest {

    @Test
    fun TC_HU01_02_LosNoDisponiblesTienenUnColorDistintoAlDeLosDisponibles() {
        val disponible = colorEstado(EstadoEquipo.DISPONIBLE)

        assertNotEquals(disponible, colorEstado(EstadoEquipo.RESERVADO))
        assertNotEquals(disponible, colorEstado(EstadoEquipo.PRESTADO))
        // Cada estado se distingue también de los demás
        assertEquals(3, EstadoEquipo.entries.map(::colorEstado).distinct().size)
    }
}
