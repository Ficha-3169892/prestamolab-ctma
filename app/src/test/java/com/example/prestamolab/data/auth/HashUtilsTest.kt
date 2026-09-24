package com.example.prestamolab.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `sha256 coincide con el hash sembrado en Supabase`() {
        assertEquals(
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
            HashUtils.sha256("123456")
        )
    }

    @Test
    fun `sha256 de la cadena vacia es el vector de referencia`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            HashUtils.sha256("")
        )
    }

    @Test
    fun `sha256 distingue mayusculas`() {
        assertNotEquals(HashUtils.sha256("Clave"), HashUtils.sha256("clave"))
    }
}
