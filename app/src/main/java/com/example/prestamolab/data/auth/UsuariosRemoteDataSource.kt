package com.example.prestamolab.data.auth

import com.example.prestamolab.data.remote.SupabaseRestClient
import org.json.JSONArray
import org.json.JSONObject

enum class CampoIdentificador(val columna: String) { CORREO("email"), DOCUMENTO("document") }

/** Usuario autenticado; la contraseña nunca se descarga. [token] identifica la sesión en el servidor. */
data class UsuarioRemoto(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: String,
    val token: String? = null
)

interface UsuariosRemoteDataSource {
    /**
     * Devuelve el usuario cuyo [campo] y hash SHA-256 de la contraseña coinciden, o null si no existe.
     * Lanza IOException sin red y SupabaseHttpException ante respuestas fuera de 2xx.
     */
    suspend fun buscarPorCredenciales(campo: CampoIdentificador, valor: String, contrasenaHash: String): UsuarioRemoto?

    /** Invalida en el servidor el token de la sesión activa. */
    suspend fun cerrarSesion() {}
}

/**
 * Login mediante la función iniciar_sesion() de docs/supabase/009_seguridad.sql: el servidor compara el
 * SHA-256 recibido contra bcrypt (R-01, R-02) y la tabla users ya no es legible con la anon key (R-03).
 */
class SupabaseUsuariosDataSource(private val cliente: SupabaseRestClient) : UsuariosRemoteDataSource {

    override suspend fun buscarPorCredenciales(
        campo: CampoIdentificador,
        valor: String,
        contrasenaHash: String
    ): UsuarioRemoto? {
        // El servidor distingue correo y documento con la misma regla que la app ("@")
        val filas = JSONArray(
            cliente.rpc(
                "iniciar_sesion",
                JSONObject().put("p_identificador", valor).put("p_hash", contrasenaHash).toString()
            )
        )
        if (filas.length() == 0) return null
        val fila = filas.getJSONObject(0)
        return UsuarioRemoto(
            id = fila.getString("id"),
            email = fila.optString("email"),
            nombre = fila.optString("full_name"),
            rol = fila.optString("role"),
            token = fila.getString("token")
        )
    }

    override suspend fun cerrarSesion() {
        cliente.rpc("cerrar_sesion")
    }
}
