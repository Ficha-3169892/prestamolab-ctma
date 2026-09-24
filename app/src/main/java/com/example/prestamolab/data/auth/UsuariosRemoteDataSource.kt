package com.example.prestamolab.data.auth

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

enum class CampoIdentificador(val columna: String) { CORREO("email"), DOCUMENTO("document") }

/** Fila de `public.users` sin la contraseña, que nunca se descarga. */
data class UsuarioRemoto(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: String
)

interface UsuariosRemoteDataSource {
    /** Devuelve el usuario cuyo [campo] y contraseña coinciden, o null si no existe. Lanza IOException sin red. */
    suspend fun buscarPorCredenciales(campo: CampoIdentificador, valor: String, contrasena: String): UsuarioRemoto?
}

/** Consulta la tabla `users` mediante la API REST de Supabase (PostgREST). */
class SupabaseUsuariosDataSource(
    private val baseUrl: String,
    private val anonKey: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UsuariosRemoteDataSource {

    override suspend fun buscarPorCredenciales(
        campo: CampoIdentificador,
        valor: String,
        contrasena: String
    ): UsuarioRemoto? = withContext(ioDispatcher) {
        if (baseUrl.isBlank()) throw IOException("SUPABASE_URL no está configurada en local.properties")

        // La contraseña solo se usa como filtro: el select no la incluye en la respuesta
        val url = URL(
            "${baseUrl.trimEnd('/')}/rest/v1/users" +
                "?select=id,email,full_name,role" +
                "&${campo.columna}=eq.${codificar(valor)}" +
                "&password=eq.${codificar(contrasena)}" +
                "&limit=1"
        )
        val conexion = url.openConnection() as HttpURLConnection
        try {
            conexion.connectTimeout = TIEMPO_ESPERA_MS
            conexion.readTimeout = TIEMPO_ESPERA_MS
            conexion.setRequestProperty("apikey", anonKey)
            conexion.setRequestProperty("Authorization", "Bearer $anonKey")
            conexion.setRequestProperty("Accept", "application/json")

            val codigo = conexion.responseCode
            if (codigo !in 200..299) throw IOException("Supabase respondió HTTP $codigo")

            val filas = JSONArray(conexion.inputStream.bufferedReader().use { it.readText() })
            if (filas.length() == 0) return@withContext null
            val fila = filas.getJSONObject(0)
            UsuarioRemoto(
                id = fila.getString("id"),
                email = fila.optString("email"),
                nombre = fila.optString("full_name"),
                rol = fila.optString("role")
            )
        } finally {
            conexion.disconnect()
        }
    }

    private fun codificar(valor: String) = URLEncoder.encode(valor, "UTF-8")

    private companion object {
        const val TIEMPO_ESPERA_MS = 10_000
    }
}
