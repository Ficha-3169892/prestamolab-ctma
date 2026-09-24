package com.example.prestamolab.data.remote

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Respuesta HTTP fuera de 2xx. Los errores de red o de tiempo de espera llegan como IOException. */
class SupabaseHttpException(val codigo: Int, val cuerpo: String) :
    Exception("Supabase respondió HTTP $codigo: ${cuerpo.take(200)}")

/** Cliente mínimo de la API REST de Supabase (PostgREST) autenticado con la anon key. */
class SupabaseRestClient(
    private val baseUrl: String,
    private val anonKey: String,
    private val tiempoEsperaMs: Int = 10_000,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /** [recurso] es la tabla con su consulta, p. ej. `users?select=id&email=eq.x`. Devuelve el JSON crudo. */
    suspend fun get(recurso: String): String = enviar("GET", recurso, cuerpo = null, prefer = null)

    /** Inserta o actualiza por clave primaria: reenviar el mismo registro no lo duplica. */
    suspend fun upsert(tabla: String, json: String) {
        enviar("POST", tabla, json, prefer = "resolution=merge-duplicates,return=minimal")
    }

    private suspend fun enviar(metodo: String, recurso: String, cuerpo: String?, prefer: String?): String =
        withContext(ioDispatcher) {
            if (baseUrl.isBlank()) throw IOException("SUPABASE_URL no está configurada en local.properties")

            val conexion = URL("${baseUrl.trimEnd('/')}/rest/v1/$recurso").openConnection() as HttpURLConnection
            try {
                conexion.requestMethod = metodo
                conexion.connectTimeout = tiempoEsperaMs
                conexion.readTimeout = tiempoEsperaMs
                conexion.setRequestProperty("apikey", anonKey)
                conexion.setRequestProperty("Authorization", "Bearer $anonKey")
                conexion.setRequestProperty("Accept", "application/json")
                prefer?.let { conexion.setRequestProperty("Prefer", it) }
                if (cuerpo != null) {
                    conexion.doOutput = true
                    conexion.setRequestProperty("Content-Type", "application/json")
                    conexion.outputStream.use { it.write(cuerpo.toByteArray(Charsets.UTF_8)) }
                }

                val codigo = conexion.responseCode
                if (codigo !in 200..299) {
                    val error = conexion.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    throw SupabaseHttpException(codigo, error)
                }
                conexion.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conexion.disconnect()
            }
        }
}
