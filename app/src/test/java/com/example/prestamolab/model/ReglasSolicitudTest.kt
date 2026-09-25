package com.example.prestamolab.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Especificación escrita antes de la regla (TDD: rojo → verde → refactor). Valores límite de HU-03:
 * propósito 9/10/180/181 caracteres y duración 0/1/8/9 horas.
 */
class ReglasSolicitudTest {

    private fun validar(ambiente: String = "Laboratorio 302", proposito: String = "Práctica de sensores", duracion: String = "2") =
        ReglasSolicitud.validar(ambiente, proposito, duracion)

    @Test
    fun DatosValidos_SinErrores() {
        // Arrange: valores dentro de todos los límites. Act: validar. Assert: ningún error.
        val errores = validar()

        assertFalse(errores.hayErrores)
    }

    @Test
    fun AmbienteVacio_EsObligatorio() {
        assertEquals("El ambiente o destino es obligatorio.", validar(ambiente = "   ").ambiente)
    }

    @Test
    fun Proposito_LimitesDe10y180Caracteres() {
        assertEquals("Propósito debe tener mínimo 10 caracteres", validar(proposito = "a".repeat(9)).proposito)
        assertNull(validar(proposito = "a".repeat(10)).proposito)
        assertNull(validar(proposito = "a".repeat(180)).proposito)
        assertEquals("Máximo 180 caracteres", validar(proposito = "a".repeat(181)).proposito)
    }

    @Test
    fun Proposito_LosEspaciosYSaltosDeLineaDeLosExtremosNoCuentan() {
        // BUG-13: nueve letras y un salto de línea no completan los 10 caracteres
        assertNotNull(validar(proposito = "123456789\n").proposito)
    }

    @Test
    fun Duracion_LimitesDe1y8Horas() {
        assertEquals("Duración entre 1 y 8 horas", validar(duracion = "0").duracion)
        assertNull(validar(duracion = "1").duracion)
        assertNull(validar(duracion = "8").duracion)
        assertEquals("Duración entre 1 y 8 horas", validar(duracion = "9").duracion)
    }

    @Test
    fun Duracion_QueNoEsUnNumero_EsInvalida() {
        assertNotNull(validar(duracion = "").duracion)
        assertNotNull(validar(duracion = "dos").duracion)
    }

    @Test
    fun VariosErrores_SeReportanTodosALaVez() {
        val errores = validar(ambiente = "", proposito = "corto", duracion = "0")

        assertNotNull(errores.ambiente)
        assertNotNull(errores.proposito)
        assertNotNull(errores.duracion)
    }
}
