package com.example.prestamolab.data.auth

import com.example.prestamolab.data.remote.SupabaseRestClient
import org.json.JSONArray
import java.net.URLEncoder

enum class CampoIdentificador(val columna: String) { CORREO("email"), DOCUMENTO("document") }

/** Fila de `public.users` sin password_hash, que nunca se descarga. */
data class UsuarioRemoto(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: String
)

interface UsuariosRemoteDataSource {
    /**
     * Devuelve el usuario cuyo [campo] y hash SHA-256 de la contraseña coinciden, o null si no existe.
     * Lanza IOException sin red y SupabaseHttpException ante respuestas fuera de 2xx.
     */
    suspend fun buscarPorCredenciales(campo: CampoIdentificador, valor: String, contrasenaHash: String): UsuarioRemoto?
}

/** Consulta la tabla `users` mediante la API REST de Supabase (PostgREST). */
class SupabaseUsuariosDataSource(private val cliente: SupabaseRestClient) : UsuariosRemoteDataSource {

    override suspend fun buscarPorCredenciales(
        campo: CampoIdentificador,
        valor: String,
        contrasenaHash: String
    ): UsuarioRemoto? {
        // El hash solo se usa como filtro: el select no lo incluye en la respuesta
        val filas = JSONArray(
            cliente.get(
                "users?select=id,email,full_name,role" +
                    "&${campo.columna}=eq.${codificar(valor)}" +
                    "&password_hash=eq.${codificar(contrasenaHash)}" +
                    "&limit=1"
            )
        )
        if (filas.length() == 0) return null
        val fila = filas.getJSONObject(0)
        return UsuarioRemoto(
            id = fila.getString("id"),
            email = fila.optString("email"),
            nombre = fila.optString("full_name"),
            rol = fila.optString("role")
        )
    }

    private fun codificar(valor: String) = URLEncoder.encode(valor, "UTF-8")
}
