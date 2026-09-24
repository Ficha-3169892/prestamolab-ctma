package com.example.prestamolab.data.remote

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Respuesta HTTP fuera de 2xx. Los errores de red o de tiempo de espera llegan como IOException. */
class SupabaseHttpException(val codigo: Int, val cuerpo: String) :
    Exception("Supabase respondió HTTP $codigo: ${cuerpo.take(200)}")

/** Cliente mínimo de la API REST de Supabase (PostgREST) autenticado con la anon key. */
class SupabaseRestClient(
    private val baseUrl: String,
    private val anonKey: String,
    tiempoEsperaMs: Long = 10_000,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(tiempoEsperaMs, TimeUnit.MILLISECONDS)
        .readTimeout(tiempoEsperaMs, TimeUnit.MILLISECONDS)
        .writeTimeout(tiempoEsperaMs, TimeUnit.MILLISECONDS)
        .build()

    /** [recurso] es la tabla con su consulta, p. ej. `users?select=id&email=eq.x`. Devuelve el JSON crudo. */
    suspend fun get(recurso: String): String = enviar(recurso) { get() }

    /**
     * Inserta o actualiza por clave primaria: reenviar el mismo registro no lo duplica.
     * Como es un INSERT ... ON CONFLICT, el JSON debe traer todas las columnas NOT NULL.
     */
    suspend fun upsert(tabla: String, json: String) {
        enviar(tabla) {
            header("Prefer", "resolution=merge-duplicates,return=minimal")
            post(json.toRequestBody(JSON))
        }
    }

    /** Actualiza solo las columnas del JSON en las filas que cumplen el filtro de [recurso]. */
    suspend fun patch(recurso: String, json: String) {
        enviar(recurso) {
            header("Prefer", "return=minimal")
            patch(json.toRequestBody(JSON))
        }
    }

    private suspend fun enviar(recurso: String, configurar: Request.Builder.() -> Request.Builder): String =
        withContext(ioDispatcher) {
            val url = "${baseUrl.trimEnd('/')}/rest/v1/$recurso".toHttpUrlOrNull()
                ?: throw IOException("SUPABASE_URL no está configurada en local.properties")
            val peticion = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Accept", "application/json")
                .configurar()
                .build()
            http.newCall(peticion).execute().use { respuesta ->
                val cuerpo = respuesta.body?.string().orEmpty()
                if (!respuesta.isSuccessful) throw SupabaseHttpException(respuesta.code, cuerpo)
                cuerpo
            }
        }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
