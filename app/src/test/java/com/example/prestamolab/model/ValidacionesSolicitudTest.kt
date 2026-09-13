package com.example.prestamolab.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesSolicitudTest {

    // TC-04: Propósito con 9 caracteres
    @Test
    fun propositoDe9CaracteresEsInvalido() {
        val proposito = "123456789"

        assertFalse(propositoValido(proposito))
    }

    // TC-05: Propósito con 10 caracteres
    @Test
    fun propositoDe10CaracteresEsValido() {
        val proposito = "1234567890"

        assertTrue(propositoValido(proposito))
    }

    // TC-06: Propósito con 180 caracteres
    @Test
    fun propositoDe180CaracteresEsValido() {
        val proposito = "a".repeat(180)

        assertTrue(propositoValido(proposito))
    }

    // TC-07: Propósito con 181 caracteres
    @Test
    fun propositoDe181CaracteresEsInvalido() {
        val proposito = "a".repeat(181)

        assertFalse(propositoValido(proposito))
    }

    // TC-08: Duración de 0 horas
    @Test
    fun duracionDe0HorasEsInvalida() {
        assertFalse(duracionValida(0))
    }

    // TC-09: Duración de 1 hora
    @Test
    fun duracionDe1HoraEsValida() {
        assertTrue(duracionValida(1))
    }

    // TC-10: Duración de 8 horas
    @Test
    fun duracionDe8HorasEsValida() {
        assertTrue(duracionValida(8))
    }

    // TC-11: Duración de 9 horas
    @Test
    fun duracionDe9HorasEsInvalida() {
        assertFalse(duracionValida(9))
    }
}
