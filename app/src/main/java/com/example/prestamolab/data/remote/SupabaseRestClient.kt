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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    /** Token de la sesión activa (R-04): las políticas RLS del servidor lo leen de la cabecera x-sesion. */
    private val tokenSesion: suspend () -> String? = { null }
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(tiempoEsperaMs, TimeUnit.MILLISECONDS)
        .readTimeout(tiempoEsperaMs, TimeUnit.MILLISECONDS)
        .writeTimeout(tiempoEsperaMs, TimeUnit.MILLISECONDS)
        .build()

    private val httpArchivos = http.newBuilder()
        .readTimeout(TIEMPO_ARCHIVOS_S, TimeUnit.SECONDS)
        .writeTimeout(TIEMPO_ARCHIVOS_S, TimeUnit.SECONDS)
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

    /** Llama una función de la base (`/rest/v1/rpc/<funcion>`) con sus parámetros en JSON; devuelve el JSON crudo. */
    suspend fun rpc(funcion: String, json: String = "{}"): String =
        enviar("rpc/$funcion") { post(json.toRequestBody(JSON)) }

    /** Borra las filas que cumplen el filtro de [recurso]; si no hay ninguna, no es un error. */
    suspend fun delete(recurso: String) {
        enviar(recurso) {
            header("Prefer", "return=minimal")
            delete()
        }
    }

    /**
     * Sube un archivo a Supabase Storage (HU-08) y devuelve su URL pública. x-upsert permite
     * repetir la subida interrumpida sin error por archivo duplicado. Una foto de cámara pesa varios MB:
     * se da más tiempo que a una consulta.
     */
    suspend fun subirArchivo(bucket: String, ruta: String, bytes: ByteArray, tipo: String): String {
        enviarA("storage/v1/object/$bucket/$ruta", httpArchivos) {
            header("x-upsert", "true")
            post(bytes.toRequestBody(tipo.toMediaType()))
        }
        return urlPublica(bucket, ruta)
    }

    fun urlPublica(bucket: String, ruta: String) = "${baseUrl.trimEnd('/')}/storage/v1/object/public/$bucket/$ruta"

    private suspend fun enviar(recurso: String, configurar: Request.Builder.() -> Request.Builder): String =
        enviarA("rest/v1/$recurso", http, configurar)

    private suspend fun enviarA(
        ruta: String,
        cliente: OkHttpClient,
        configurar: Request.Builder.() -> Request.Builder
    ): String {
        val token = tokenSesion()
        return withContext(ioDispatcher) {
            val url = "${baseUrl.trimEnd('/')}/$ruta".toHttpUrlOrNull()
                ?: throw IOException("SUPABASE_URL no está configurada en local.properties")
            val peticion = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Accept", "application/json")
                .apply { if (token != null) header(CABECERA_SESION, token) }
                .configurar()
                .build()
            cliente.newCall(peticion).execute().use { respuesta ->
                val cuerpo = respuesta.body?.string().orEmpty()
                if (!respuesta.isSuccessful) throw SupabaseHttpException(respuesta.code, cuerpo)
                cuerpo
            }
        }
    }

    companion object {
        /** Debe coincidir con la que lee usuario_actual() en docs/supabase/009_seguridad.sql. */
        const val CABECERA_SESION = "x-sesion"
        private const val TIEMPO_ARCHIVOS_S = 60L
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
