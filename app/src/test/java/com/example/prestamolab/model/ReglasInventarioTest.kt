package com.example.prestamolab.model

import org.junit.Assert.*
import org.junit.Test

class ReglasInventarioTest {

    @Test
    fun TC_HU12_02_NombreVacio_MuestraErrorDelCampo() {
        val errores = ReglasInventario.validar(nombre = "   ", categoria = "Laboratorio")

        assertEquals("El nombre es obligatorio.", errores.nombre)
        assertNull(errores.categoria)
        assertTrue(errores.hayErrores)
    }

    @Test
    fun TC_HU12_02_CategoriaVacia_MuestraErrorDelCampo() {
        val errores = ReglasInventario.validar(nombre = "Proyector", categoria = "")

        assertNull(errores.nombre)
        assertEquals("La categoría es obligatoria.", errores.categoria)
    }

    @Test
    fun DatosValidos_NoTienenErrores() {
        assertFalse(ReglasInventario.validar("Proyector Epson", "Audiovisual").hayErrores)
    }

    @Test
    fun LongitudesMaximas() {
        val nombreLargo = "a".repeat(ReglasInventario.NOMBRE_MAXIMO + 1)
        val categoriaLarga = "a".repeat(ReglasInventario.CATEGORIA_MAXIMA + 1)

        assertNotNull(ReglasInventario.validar(nombreLargo, "Otro").nombre)
        assertNotNull(ReglasInventario.validar("Otro", categoriaLarga).categoria)
        assertFalse(ReglasInventario.validar(nombreLargo.drop(1), categoriaLarga.drop(1)).hayErrores)
    }

    @Test
    fun TC_HU12_04_ConPrestamoActivo_LaEliminacionSeRechaza() {
        listOf(EstadoSolicitud.SOLICITADA, EstadoSolicitud.PRESTADO).forEach { activo ->
            val resultado = ReglasInventario.validarEliminacion(listOf(EstadoSolicitud.DEVUELTO, activo))

            assertEquals(
                "No se puede eliminar: el equipo tiene un préstamo activo.",
                resultado.exceptionOrNull()?.message
            )
        }
    }

    @Test
    fun ConPrestamosCerrados_SeConservaElHistorial() {
        assertTrue(ReglasInventario.validarEliminacion(listOf(EstadoSolicitud.DEVUELTO)).isFailure)
        assertTrue(ReglasInventario.validarEliminacion(listOf(EstadoSolicitud.CANCELADA)).isFailure)
    }

    @Test
    fun SinPrestamos_SePuedeEliminar() {
        assertTrue(ReglasInventario.validarEliminacion(emptyList()).isSuccess)
    }
}
