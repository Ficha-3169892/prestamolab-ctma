package com.example.prestamolab.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminAuthValidatorTest {

    @Test
    fun emailInvalido_retornaFalse() {
        assertFalse(AdminAuthValidator.isValidEmail("admin"))
        assertFalse(AdminAuthValidator.isValidEmail("admin@"))
        assertFalse(AdminAuthValidator.isValidEmail(""))
    }

    @Test
    fun emailValido_retornaTrue() {
        assertTrue(AdminAuthValidator.isValidEmail("admin@gmail.com"))
        assertTrue(AdminAuthValidator.isValidEmail("test.user@domain.co"))
    }

    @Test
    fun passwordCorta_retornaFalse() {
        assertFalse(AdminAuthValidator.isValidPassword("12345"))
    }

    @Test
    fun passwordLongitudAdecuada_retornaTrue() {
        assertTrue(AdminAuthValidator.isValidPassword("admin123"))
    }

    @Test
    fun credencialesCorrectas_autenticaAdmin() {
        assertTrue(AdminAuthValidator.validateAdminCredentials("admin@gmail.com", "admin123"))
        assertTrue(AdminAuthValidator.validateAdminCredentials("ADMIN@GMAIL.COM ", "admin123"))
    }

    @Test
    fun credencialesIncorrectas_fallaAutenticacion() {
        assertFalse(AdminAuthValidator.validateAdminCredentials("usuario@gmail.com", "admin123"))
        assertFalse(AdminAuthValidator.validateAdminCredentials("admin@gmail.com", "wrongpass"))
    }
}
