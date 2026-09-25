package com.example.prestamolab.data.remote

import com.example.prestamolab.model.Rol
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
    /** Revisión del instructor (HU-14): users.id de quien revisó y motivo si rechazó. */
    val revisadoPor: String? = null,
    val motivoRechazo: String? = null,
    /** CA-HU13-06: dónde se solicitó. */
    val latitud: Double? = null,
    val longitud: Double? = null,
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

/** Fila de `public.activities` (HU-11); la fecha va en ISO-8601. */
data class ActividadRemota(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val ambiente: String,
    val fecha: String,
    val instructorId: String
)

/** Fila de `public.evidences` (HU-08); la foto ya está en Storage. */
data class EvidenciaRemota(
    val id: String,
    val prestamoId: String,
    val etapa: String,
    val urlFoto: String,
    val fecha: String,
    val latitud: Double? = null,
    val longitud: Double? = null
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

    /** Cambio de estado al reservar o devolver: el estudiante no envía nombre ni categoría. */
    suspend fun actualizarEstadoEquipo(id: String, estado: String)

    /** HU-12: el instructor crea o edita el equipo completo. */
    suspend fun guardarEquipo(equipo: EquipoRemoto)

    suspend fun eliminarEquipo(id: String)
    suspend fun guardarPrestamo(prestamo: PrestamoRemoto)
    suspend fun guardarDevolucion(devolucion: DevolucionRemota)

    /** HU-11: todos reciben las actividades; solo el instructor las crea, edita o elimina. */
    suspend fun actividades(): List<ActividadRemota>
    suspend fun guardarActividad(actividad: ActividadRemota)
    suspend fun eliminarActividad(id: String)

    /** HU-08: sube la foto al bucket de evidencias y devuelve su URL pública. */
    suspend fun subirFoto(ruta: String, bytes: ByteArray): String
    suspend fun guardarEvidencia(evidencia: EvidenciaRemota)
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
            "environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name)&order=request_date" +
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
                revisadoPor = it.textoONulo("reviewed_by"),
                motivoRechazo = it.textoONulo("rejection_reason"),
                latitud = it.decimalONulo("latitude"),
                longitud = it.decimalONulo("longitude"),
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

    // PATCH y no upsert: equipments tiene columnas NOT NULL (p. ej. title) que la app no maneja
    override suspend fun actualizarEstadoEquipo(id: String, estado: String) = cliente.patch(
        "equipments?id=eq.${codificar(id)}",
        JSONObject().put("status", estado).toString()
    )

    override suspend fun guardarEquipo(equipo: EquipoRemoto) = cliente.upsert(
        "equipments",
        JSONObject()
            .put("id", equipo.id)
            .put("name", equipo.nombre)
            // equipments.title es NOT NULL y el upsert es un INSERT: se envía igual al nombre
            .put("title", equipo.nombre)
            .put("category", equipo.categoria)
            .put("status", equipo.estado)
            .toString()
    )

    override suspend fun eliminarEquipo(id: String) = cliente.delete("equipments?id=eq.${codificar(id)}")

    override suspend fun guardarPrestamo(prestamo: PrestamoRemoto) = cliente.upsert(
        "loans",
        JSONObject()
            .put("id", prestamo.id)
            .put("user_id", prestamo.usuarioId)
            // loans.user_role es NOT NULL; solo el estudiante solicita préstamos (CA-HU03-08)
            .put("user_role", Rol.ESTUDIANTE.name)
            .put("equipment_id", prestamo.equipoId)
            .put("status", prestamo.estado)
            .put("request_date", prestamo.fechaSolicitud)
            .put("return_date", prestamo.fechaLimite)
            .put("environment", prestamo.ambiente)
            .put("purpose", prestamo.proposito)
            .put("duration_hours", prestamo.duracionHoras)
            .put("reviewed_by", prestamo.revisadoPor ?: JSONObject.NULL)
            .put("rejection_reason", prestamo.motivoRechazo ?: JSONObject.NULL)
            .put("latitude", prestamo.latitud ?: JSONObject.NULL)
            .put("longitude", prestamo.longitud ?: JSONObject.NULL)
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

    override suspend fun actividades(): List<ActividadRemota> =
        filas(cliente.get("activities?select=id,title,description,location,scheduled_at,instructor_id&order=scheduled_at")).map {
            ActividadRemota(
                id = it.getString("id"),
                titulo = it.getString("title"),
                descripcion = it.textoONulo("description").orEmpty(),
                ambiente = it.textoONulo("location").orEmpty(),
                fecha = it.getString("scheduled_at"),
                instructorId = it.getString("instructor_id")
            )
        }

    override suspend fun guardarActividad(actividad: ActividadRemota) = cliente.upsert(
        "activities",
        JSONObject()
            .put("id", actividad.id)
            .put("title", actividad.titulo)
            .put("description", actividad.descripcion)
            .put("location", actividad.ambiente)
            .put("scheduled_at", actividad.fecha)
            .put("instructor_id", actividad.instructorId)
            .toString()
    )

    override suspend fun eliminarActividad(id: String) = cliente.delete("activities?id=eq.${codificar(id)}")

    override suspend fun subirFoto(ruta: String, bytes: ByteArray): String =
        cliente.subirArchivo(BUCKET_EVIDENCIAS, ruta, bytes, "image/jpeg")

    override suspend fun guardarEvidencia(evidencia: EvidenciaRemota) = cliente.upsert(
        "evidences",
        JSONObject()
            .put("id", evidencia.id)
            .put("loan_id", evidencia.prestamoId)
            .put("stage", evidencia.etapa)
            .put("photo_url", evidencia.urlFoto)
            .put("taken_at", evidencia.fecha)
            .put("latitude", evidencia.latitud ?: JSONObject.NULL)
            .put("longitude", evidencia.longitud ?: JSONObject.NULL)
            .toString()
    )

    private fun filas(json: String): List<JSONObject> {
        val arreglo = JSONArray(json)
        return List(arreglo.length()) { arreglo.getJSONObject(it) }
    }

    private fun codificar(valor: String) = URLEncoder.encode(valor, "UTF-8")

    companion object {
        /** Lo crea docs/supabase/008_evidencias.sql. */
        const val BUCKET_EVIDENCIAS = "evidencias"
    }
}

// optString devuelve "null" para JSON null; estas variantes devuelven null de Kotlin
internal fun JSONObject.textoONulo(campo: String): String? = if (isNull(campo)) null else getString(campo)

internal fun JSONObject.decimalONulo(campo: String): Double? = if (isNull(campo)) null else getDouble(campo)
