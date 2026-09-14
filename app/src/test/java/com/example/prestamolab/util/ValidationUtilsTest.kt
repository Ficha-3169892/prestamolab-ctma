package com.example.prestamolab.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun ambienteValido_conCadenaVacia_retornaFalse() {
        assertFalse(ambienteValido(""))
    }

    @Test
    fun ambienteValido_conSoloEspacios_retornaFalse() {
        assertFalse(ambienteValido("     "))
    }

    @Test
    fun ambienteValido_conNombreValido_retornaTrue() {
        assertTrue(ambienteValido("Laboratorio de Electrónica"))
    }

    @Test
    fun propositoValido_con9Caracteres_retornaFalse() {
        assertFalse(propositoValido("123456789"))
    }

    @Test
    fun propositoValido_con10Caracteres_retornaTrue() {
        assertTrue(propositoValido("1234567890"))
    }

    @Test
    fun propositoValido_con180Caracteres_retornaTrue() {
        assertTrue(propositoValido("a".repeat(180)))
    }

    @Test
    fun propositoValido_con181Caracteres_retornaFalse() {
        assertFalse(propositoValido("a".repeat(181)))
    }

    @Test
    fun duracionValida_conHorasMenoresOIgualesACero_retornaFalse() {
        assertFalse(duracionValida(0))
        assertFalse(duracionValida(-2))
    }

    @Test
    fun duracionValida_conValoresRangoPermitidoYExcedido_retornaCorrectamente() {
        assertTrue(duracionValida(1))
        assertTrue(duracionValida(5))
        assertTrue(duracionValida(8))
        assertFalse(duracionValida(9))
        assertFalse(duracionValida(24))
    }
}
