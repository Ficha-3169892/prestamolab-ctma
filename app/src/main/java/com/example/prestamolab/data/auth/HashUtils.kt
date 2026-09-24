package com.example.prestamolab.data.auth

import java.security.MessageDigest

object HashUtils {
    /** SHA-256 en hexadecimal minúscula, el mismo formato que users.password_hash. */
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
