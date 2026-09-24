package com.example.prestamolab.data.remote

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/** Fila de `public.equipments`. */
data class EquipoRemoto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val estado: String
)

/** Fila de `public.loans`; las fechas van en ISO-8601. */
data class PrestamoRemoto(
    val id: String,
    val usuarioId: String,
    val equipoId: String,
    val estado: String,
    val fechaSolicitud: String,
    /** Fecha límite pactada (loans.return_date). */
    val fechaLimite: String,
    val ambiente: String,
    val proposito: String,
    val duracionHoras: Int,
    /** users.full_name; solo se recibe, no se envía. */
    val nombreSolicitante: String? = null
)

/** Fila de `public.returns`. */
data class DevolucionRemota(
    val id: String,
    val prestamoId: String,
    val condicion: String,
    val notas: String,
    val fechaDevolucion: String,
    val latitud: Double?,
    val longitud: Double?
)

/**
 * Acceso a las tablas remotas. Lanza [SupabaseHttpException] ante respuestas fuera de 2xx
 * e IOException ante fallos de red o tiempo de espera agotado.
 */
interface PrestamosRemoteDataSource {
    suspend fun equipos(): List<EquipoRemoto>

    /** [usuarioId] null trae los préstamos de todos (instructor); si no, solo los de ese usuario. */
    suspend fun prestamos(usuarioId: String?): List<PrestamoRemoto>

    suspend fun devoluciones(usuarioId: String?): List<DevolucionRemota>

    suspend fun guardarEquipo(equipo: EquipoRemoto)
    suspend fun guardarPrestamo(prestamo: PrestamoRemoto)
    suspend fun guardarDevolucion(devolucion: DevolucionRemota)
}

class SupabasePrestamosDataSource(private val cliente: SupabaseRestClient) : PrestamosRemoteDataSource {

    override suspend fun equipos(): List<EquipoRemoto> =
        filas(cliente.get("equipments?select=id,name,category,status&order=name")).map {
            EquipoRemoto(
                id = it.getString("id"),
                nombre = it.getString("name"),
                categoria = it.textoONulo("category").orEmpty(),
                estado = it.getString("status")
            )
        }

    override suspend fun prestamos(usuarioId: String?): List<PrestamoRemoto> {
        // El nombre del solicitante se trae en la misma consulta mediante la FK loans.user_id → users.id
        val consulta = "loans?select=id,user_id,equipment_id,status,request_date,return_date," +
            "environment,purpose,duration_hours,solicitante:users!loans_user_id_fkey(full_name)&order=request_date" +
            (usuarioId?.let { "&user_id=eq.${codificar(it)}" } ?: "")
        return filas(cliente.get(consulta)).map {
            PrestamoRemoto(
                id = it.getString("id"),
                usuarioId = it.getString("user_id"),
                equipoId = it.getString("equipment_id"),
                estado = it.getString("status"),
                fechaSolicitud = it.getString("request_date"),
                fechaLimite = it.getString("return_date"),
                ambiente = it.textoONulo("environment").orEmpty(),
                proposito = it.textoONulo("purpose").orEmpty(),
                duracionHoras = it.getInt("duration_hours"),
                nombreSolicitante = it.optJSONObject("solicitante")?.textoONulo("full_name")
            )
        }
    }

    override suspend fun devoluciones(usuarioId: String?): List<DevolucionRemota> {
        val consulta = "returns?select=id,loan_id,equipment_condition,notes,return_date,latitude,longitude" +
            // loans!inner filtra las devoluciones por el dueño del préstamo
            (usuarioId?.let { ",loans!inner(user_id)&loans.user_id=eq.${codificar(it)}" } ?: "")
        return filas(cliente.get(consulta)).map {
            DevolucionRemota(
                id = it.getString("id"),
                prestamoId = it.getString("loan_id"),
                condicion = it.getString("equipment_condition"),
                notas = it.textoONulo("notes").orEmpty(),
                fechaDevolucion = it.getString("return_date"),
                latitud = it.decimalONulo("latitude"),
                longitud = it.decimalONulo("longitude")
            )
        }
    }

    override suspend fun guardarEquipo(equipo: EquipoRemoto) = cliente.upsert(
        "equipments",
        JSONObject()
            .put("id", equipo.id)
            .put("name", equipo.nombre)
            .put("category", equipo.categoria)
            .put("status", equipo.estado)
            .toString()
    )

    override suspend fun guardarPrestamo(prestamo: PrestamoRemoto) = cliente.upsert(
        "loans",
        JSONObject()
            .put("id", prestamo.id)
            .put("user_id", prestamo.usuarioId)
            .put("equipment_id", prestamo.equipoId)
            .put("status", prestamo.estado)
            .put("request_date", prestamo.fechaSolicitud)
            .put("return_date", prestamo.fechaLimite)
            .put("environment", prestamo.ambiente)
            .put("purpose", prestamo.proposito)
            .put("duration_hours", prestamo.duracionHoras)
            .toString()
    )

    override suspend fun guardarDevolucion(devolucion: DevolucionRemota) = cliente.upsert(
        "returns",
        JSONObject()
            .put("id", devolucion.id)
            .put("loan_id", devolucion.prestamoId)
            .put("equipment_condition", devolucion.condicion)
            .put("notes", devolucion.notas)
            .put("return_date", devolucion.fechaDevolucion)
            .put("latitude", devolucion.latitud ?: JSONObject.NULL)
            .put("longitude", devolucion.longitud ?: JSONObject.NULL)
            .toString()
    )

    private fun filas(json: String): List<JSONObject> {
        val arreglo = JSONArray(json)
        return List(arreglo.length()) { arreglo.getJSONObject(it) }
    }

    private fun codificar(valor: String) = URLEncoder.encode(valor, "UTF-8")
}

// optString devuelve "null" para JSON null; estas variantes devuelven null de Kotlin
internal fun JSONObject.textoONulo(campo: String): String? = if (isNull(campo)) null else getString(campo)

internal fun JSONObject.decimalONulo(campo: String): Double? = if (isNull(campo)) null else getDouble(campo)
