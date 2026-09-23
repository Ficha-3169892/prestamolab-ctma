package com.example.prestamolab.util

object AdminAuthValidator {

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
        return emailRegex.matches(email.trim())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateAdminCredentials(email: String, password: String): Boolean {
        return isValidEmail(email) &&
                email.trim().equals("admin@gmail.com", ignoreCase = true) &&
                password == "admin123"
    }
}
