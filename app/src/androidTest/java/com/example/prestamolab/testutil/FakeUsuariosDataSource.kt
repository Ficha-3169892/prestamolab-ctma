package com.example.prestamolab.testutil

import com.example.prestamolab.data.auth.CampoIdentificador
import com.example.prestamolab.data.auth.UsuarioRemoto
import com.example.prestamolab.data.auth.UsuariosRemoteDataSource

/** Tabla `users` en memoria con los mismos usuarios de prueba que docs/supabase/002_credenciales_usuarios.sql. */
class FakeUsuariosDataSource(
    private val filas: List<Fila> = listOf(INSTRUCTOR, ESTUDIANTE)
) : UsuariosRemoteDataSource {

    data class Fila(val usuario: UsuarioRemoto, val documento: String, val contrasena: String)

    /** Si no es null, la siguiente consulta lanza este error (p. ej. IOException sin red). */
    var error: Exception? = null
    val consultas = mutableListOf<Pair<CampoIdentificador, String>>()

    override suspend fun buscarPorCredenciales(
        campo: CampoIdentificador,
        valor: String,
        contrasena: String
    ): UsuarioRemoto? {
        error?.let { throw it }
        consultas += campo to valor
        return filas.find { fila ->
            val coincide = when (campo) {
                CampoIdentificador.CORREO -> fila.usuario.email == valor
                CampoIdentificador.DOCUMENTO -> fila.documento == valor
            }
            coincide && fila.contrasena == contrasena
        }?.usuario
    }

    companion object {
        const val CONTRASENA = "123"
        const val CORREO_INSTRUCTOR = "instructor@sena.edu.co"
        const val DOCUMENTO_INSTRUCTOR = "12345"
        const val CORREO_ESTUDIANTE = "estudiante@sena.edu.co"
        const val DOCUMENTO_ESTUDIANTE = "67890"

        val INSTRUCTOR = Fila(
            UsuarioRemoto("uuid-instructor", CORREO_INSTRUCTOR, "Instructor CTMA", "INSTRUCTOR"),
            DOCUMENTO_INSTRUCTOR, CONTRASENA
        )
        val ESTUDIANTE = Fila(
            UsuarioRemoto("uuid-estudiante", CORREO_ESTUDIANTE, "Estudiante CTMA", "ESTUDIANTE"),
            DOCUMENTO_ESTUDIANTE, CONTRASENA
        )
    }
}
