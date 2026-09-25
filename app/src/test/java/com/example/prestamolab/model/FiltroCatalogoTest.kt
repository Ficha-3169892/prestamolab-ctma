package com.example.prestamolab.model

import org.junit.Assert.*
import org.junit.Test

class FiltroCatalogoTest {

    private val equipos = listOf(
        Equipo(1, "Multímetro Digital", "Herramienta", EstadoEquipo.DISPONIBLE),
        Equipo(2, "Osciloscopio 100MHz", "Laboratorio", EstadoEquipo.RESERVADO),
        Equipo(3, "Cautín", "Herramienta", EstadoEquipo.PRESTADO),
        Equipo(4, "Fuente de Poder DC", "Laboratorio", EstadoEquipo.DISPONIBLE)
    )

    @Test
    fun SinFiltro_MuestraTodos() {
        assertEquals(equipos, FiltroCatalogo().aplicar(equipos))
        assertFalse(FiltroCatalogo().activo)
    }

    @Test
    fun TC_HU01_03_SoloDisponibles() {
        assertEquals(listOf(1, 4), FiltroCatalogo(soloDisponibles = true).aplicar(equipos).map { it.id })
    }

    @Test
    fun TC_HU01_03_PorCategoria() {
        assertEquals(listOf(1, 3), FiltroCatalogo(categoria = "Herramienta").aplicar(equipos).map { it.id })
    }

    @Test
    fun AmbosFiltrosSeCombinan() {
        val filtro = FiltroCatalogo(soloDisponibles = true, categoria = "Laboratorio")

        assertEquals(listOf(4), filtro.aplicar(equipos).map { it.id })
        assertTrue(filtro.activo)
    }

    @Test
    fun LasCategoriasSalenOrdenadasYSinRepetir() {
        assertEquals(listOf("Herramienta", "Laboratorio"), FiltroCatalogo.categorias(equipos))
    }
}
