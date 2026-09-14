package com.example.prestamolab.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun testAmbienteValido() {
        assertFalse(ambienteValido(""))
        assertFalse(ambienteValido("   "))
        assertTrue(ambienteValido("Laboratorio de Redes"))
        assertTrue(ambienteValido("A"))
    }

    @Test
    fun testPropositoValido() {
        // Menos de 10 caracteres
        assertFalse(propositoValido("Corto"))
        assertFalse(propositoValido("123459"))
        
        // Entre 10 y 180 caracteres
        assertTrue(propositoValido("Desarrollo de laboratorio autónomo"))
        assertTrue(propositoValido("Diez charss"))
        
        // Más de 180 caracteres
        val textoLargo = "a".repeat(181)
        assertFalse(propositoValido(textoLargo))
    }

    @Test
    fun testDuracionValida() {
        // Menores a 1
        assertFalse(duracionValida(0))
        assertFalse(duracionValida(-5))
        
        // Entre 1 y 8 horas
        assertTrue(duracionValida(1))
        assertTrue(duracionValida(4))
        assertTrue(duracionValida(8))
        
        // Mayores a 8
        assertFalse(duracionValida(9))
        assertFalse(duracionValida(24))
    }
}
