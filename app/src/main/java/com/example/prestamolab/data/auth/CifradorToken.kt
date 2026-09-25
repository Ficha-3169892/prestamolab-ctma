package com.example.prestamolab.data.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Cifra el token de sesión antes de guardarlo (regla de la guía: ningún token en texto plano).
 * [descifrar] devuelve null si el valor no se puede recuperar, p. ej. si la clave ya no existe.
 */
interface CifradorToken {
    fun cifrar(texto: String): String
    fun descifrar(cifrado: String): String?
}

/**
 * AES-256-GCM con una clave del Android Keystore: la clave no sale del almacén seguro del teléfono, así que
 * copiar el archivo de DataStore a otro dispositivo no revela el token.
 */
class KeystoreCifradorToken(private val alias: String = ALIAS) : CifradorToken {

    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance(PROVEEDOR).apply { load(null) }
        (almacen.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVEEDOR).apply {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    override fun cifrar(texto: String): String {
        val cifrador = Cipher.getInstance(TRANSFORMACION).apply { init(Cipher.ENCRYPT_MODE, clave()) }
        // El IV (12 bytes) viaja delante del texto cifrado; lo genera el Keystore en cada cifrado
        val datos = cifrador.iv + cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(datos, Base64.NO_WRAP)
    }

    override fun descifrar(cifrado: String): String? = try {
        val datos = Base64.decode(cifrado, Base64.NO_WRAP)
        val cifrador = Cipher.getInstance(TRANSFORMACION).apply {
            init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, datos, 0, LARGO_IV))
        }
        String(cifrador.doFinal(datos, LARGO_IV, datos.size - LARGO_IV), Charsets.UTF_8)
    } catch (e: Exception) {
        // Clave perdida o valor alterado: la sesión se descarta y se vuelve a iniciar
        null
    }

    private companion object {
        const val PROVEEDOR = "AndroidKeyStore"
        const val ALIAS = "prestamolab-token-sesion"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
        const val LARGO_IV = 12
    }
}
