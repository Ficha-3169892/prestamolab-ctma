package com.example.prestamolab.testutil

import com.example.prestamolab.data.auth.CampoIdentificador
import com.example.prestamolab.data.auth.UsuarioRemoto
import com.example.prestamolab.data.auth.UsuariosRemoteDataSource

/** Tabla `users` en memoria con los mismos usuarios de prueba que docs/supabase/002_credenciales_usuarios.sql. */
class FakeUsuariosDataSource(
    private val filas: List<Fila> = listOf(INSTRUCTOR, ESTUDIANTE)
) : UsuariosRemoteDataSource {

    data class Fila(val usuario: UsuarioRemoto, val documento: String, val contrasenaHash: String)

    /** Si no es null, la siguiente consulta lanza este error (p. ej. IOException sin red). */
    var error: Exception? = null
    val consultas = mutableListOf<Pair<CampoIdentificador, String>>()
    val hashes = mutableListOf<String>()

    override suspend fun buscarPorCredenciales(
        campo: CampoIdentificador,
        valor: String,
        contrasenaHash: String
    ): UsuarioRemoto? {
        error?.let { throw it }
        consultas += campo to valor
        hashes += contrasenaHash
        return filas.find { fila ->
            val coincide = when (campo) {
                CampoIdentificador.CORREO -> fila.usuario.email == valor
                CampoIdentificador.DOCUMENTO -> fila.documento == valor
            }
            coincide && fila.contrasenaHash == contrasenaHash
        }?.usuario?.copy(token = "token-${++sesionesEmitidas}")
    }

    /** Tokens emitidos y sesiones cerradas en el "servidor". */
    var sesionesEmitidas = 0
    var sesionesCerradas = 0

    override suspend fun cerrarSesion() {
        error?.let { throw it }
        sesionesCerradas++
    }

    companion object {
        const val CONTRASENA = "123456"
        // SHA-256 de "123456", el valor sembrado en Supabase
        const val HASH_CONTRASENA = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
        const val CORREO_INSTRUCTOR = "instructor@sena.edu.co"
        const val DOCUMENTO_INSTRUCTOR = "12345"
        const val CORREO_ESTUDIANTE = "estudiante@sena.edu.co"
        const val DOCUMENTO_ESTUDIANTE = "67890"

        val INSTRUCTOR = Fila(
            UsuarioRemoto("uuid-instructor", CORREO_INSTRUCTOR, "Instructor CTMA", "INSTRUCTOR"),
            DOCUMENTO_INSTRUCTOR, HASH_CONTRASENA
        )
        val ESTUDIANTE = Fila(
            UsuarioRemoto("uuid-estudiante", CORREO_ESTUDIANTE, "Estudiante CTMA", "ESTUDIANTE"),
            DOCUMENTO_ESTUDIANTE, HASH_CONTRASENA
        )
    }
}
