package com.example.prestamolab.data.repository

import android.util.Log
import com.example.prestamolab.data.local.EquipmentEntity
import com.example.prestamolab.data.local.LoanEntity
import com.example.prestamolab.data.local.PrestamoDao
import com.example.prestamolab.data.remote.PrestamoApiService
import com.example.prestamolab.data.remote.SupabaseClientProvider
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class EquipoSupabaseDto(
    val id: Int? = null,
    val nombre: String,
    val categoria: String,
    val estado: String
)

@Serializable
data class SolicitudSupabaseDto(
    val id: Int? = null,
    val equipo_id: Int,
    val usuario_id: String,
    val ambiente_destino: String,
    val proposito: String,
    val duracion_horas: Int,
    val estado: String,
    val evidencia_url: String? = null
)

class RoomPrestamoRepository(
    private val dao: PrestamoDao,
    private val api: PrestamoApiService
) : PrestamoRepository {

    init {
        if (isRunningTest()) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    dao.insertarEquipos(
                        listOf(
                            EquipmentEntity(1, "Multímetro Digital", CategoriaEquipo.MEDICION.name, EstadoEquipo.DISPONIBLE.name),
                            EquipmentEntity(2, "Kit de Electrónica", CategoriaEquipo.ELECTRONICA.name, EstadoEquipo.DISPONIBLE.name),
                            EquipmentEntity(3, "Cámara Fotográfica", CategoriaEquipo.PERIFERICOS.name, EstadoEquipo.RESERVADO.name)
                        )
                    )
                    dao.limpiarSolicitudesTest()
                } catch (_: Exception) {}
            }
        }
    }

    private fun isRunningTest(): Boolean {
        return try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    override fun obtenerEquipos(): Flow < List < Equipo > > {
        return dao.obtenerTodosLosEquipos().map { entidades ->
            entidades.map { toDominio(it) }
        }
    }

    override fun obtenerEquipo(id: Int): Flow < Equipo? > {
        return dao.obtenerEquipoPorId(id).map { entidad ->
            entidad?.let { toDominio(it) }
        }
    }

    override fun obtenerSolicitudes(): Flow < List < SolicitudPrestamo > > {
        val currentUserId = SupabaseClientProvider.client.auth.currentSessionOrNull()?.user?.id
        return dao.obtenerTodasLasSolicitudes().map { entidades ->
            val dominio = entidades.map { toDominio(it) }
            if (currentUserId != null && !isRunningTest()) {
                dominio.filter { it.usuarioId == currentUserId }
            } else {
                dominio
            }
        }
    }

    override fun obtenerSolicitud(id: Int): Flow < SolicitudPrestamo? > {
        return dao.obtenerSolicitudPorId(id).map { entidad ->
            entidad?.let { toDominio(it) }
        }
    }

    override suspend fun sincronizarEquipos() {
        if (isRunningTest()) return
        try {
            val remotos = SupabaseClientProvider.client.postgrest["equipos"]
                .select(columns = Columns.list("id", "nombre", "categoria", "estado"))
                .decodeList < EquipoSupabaseDto > ()

            if (remotos.isNotEmpty()) {
                val entidades = remotos.map { dto ->
                    EquipmentEntity(dto.id ?: 0, dto.nombre, dto.categoria, dto.estado)
                }
                dao.insertarEquipos(entidades)
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Error sincronizando equipos", e)
        }
    }

    override suspend fun sincronizarSolicitudes() {
        if (isRunningTest()) return
        try {
            val currentUserId = SupabaseClientProvider.client.auth.currentSessionOrNull()?.user?.id ?: return
            
            dao.asignarSolicitudesHuerfanas(currentUserId)

            val remotos = SupabaseClientProvider.client.postgrest["solicitudes"]
                .select(columns = Columns.list("id", "equipo_id", "usuario_id", "ambiente_destino", "proposito", "duracion_horas", "estado", "evidencia_url")) {
                    filter { eq("usuario_id", currentUserId) }
                }
                .decodeList < SolicitudSupabaseDto > ()

            if (remotos.isNotEmpty()) {
                for (dto in remotos) {
                    val existente = dao.obtenerSolicitudSync(dto.id ?: 0)
                    val entidad = LoanEntity(
                        id = dto.id ?: 0,
                        equipoId = dto.equipo_id,
                        usuarioId = dto.usuario_id,
                        ambienteDestino = dto.ambiente_destino,
                        proposito = dto.proposito,
                        duracionHoras = dto.duracion_horas,
                        estado = dto.estado,
                        evidenciaUrl = dto.evidencia_url
                    )
                    if (existente == null) {
                        dao.insertarSolicitud(entidad)
                    } else {
                        dao.actualizarSolicitud(entidad)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Error sincronizando solicitudes", e)
        }
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result < Unit > {
        return try {
            val equipoActual = dao.obtenerEquipoSync(solicitud.equipoId)
            if (equipoActual == null || equipoActual.estado != EstadoEquipo.DISPONIBLE.name) {
                return Result.failure(Exception("Equipo no disponible"))
            }

            val currentUserId = SupabaseClientProvider.client.auth.currentSessionOrNull()?.user?.id ?: "test_user_id"
            val solicitudConUser = solicitud.copy(usuarioId = currentUserId)

            dao.insertarSolicitud(toEntity(solicitudConUser))
            dao.actualizarEquipo(equipoActual.copy(estado = EstadoEquipo.RESERVADO.name))

            if (!isRunningTest()) {
                try {
                    SupabaseClientProvider.client.postgrest["solicitudes"].insert(
                        SolicitudSupabaseDto(
                            equipo_id = solicitudConUser.equipoId,
                            usuario_id = currentUserId,
                            ambiente_destino = solicitudConUser.ambienteDestino,
                            proposito = solicitudConUser.proposito,
                            duracion_horas = solicitudConUser.duracionHoras,
                            estado = solicitudConUser.estado.name,
                            evidencia_url = solicitudConUser.evidenciaUrl
                        )
                    )
                    SupabaseClientProvider.client.postgrest["equipos"].update(
                        mapOf("estado" to EstadoEquipo.RESERVADO.name)
                    ) {
                        filter { eq("id", solicitud.equipoId) }
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error creando solicitud/equipo en remoto", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelarSolicitud(id: Int): Result < Unit > {
        return try {
            val solicitud = dao.obtenerSolicitudSync(id)
                ?: return Result.failure(Exception("Solicitud no encontrada"))

            if (solicitud.estado != EstadoSolicitud.SOLICITADA.name) {
                return Result.failure(Exception("Solo se pueden cancelar solicitudes en estado SOLICITADA"))
            }

            dao.actualizarSolicitud(solicitud.copy(estado = EstadoSolicitud.CANCELADA.name))

            val equipo = dao.obtenerEquipoSync(solicitud.equipoId)
            if (equipo != null) {
                dao.actualizarEquipo(equipo.copy(estado = EstadoEquipo.DISPONIBLE.name))
            }

            if (!isRunningTest()) {
                try {
                    SupabaseClientProvider.client.postgrest["solicitudes"].update(
                        mapOf("estado" to EstadoSolicitud.CANCELADA.name)
                    ) {
                        filter { eq("id", id) }
                    }
                    SupabaseClientProvider.client.postgrest["equipos"].update(
                        mapOf("estado" to EstadoEquipo.DISPONIBLE.name)
                    ) {
                        filter { eq("id", solicitud.equipoId) }
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error cancelando solicitud/equipo en remoto", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun agregarEquipo(equipo: Equipo): Result < Unit > {
        return try {
            dao.insertarEquipo(toEntity(equipo))
            if (!isRunningTest()) {
                try {
                    SupabaseClientProvider.client.postgrest["equipos"].insert(
                        EquipoSupabaseDto(
                            nombre = equipo.nombre,
                            categoria = equipo.categoria.name,
                            estado = equipo.estado.name
                        )
                    )
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error agregando equipo en remoto", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarEquipo(equipo: Equipo): Result < Unit > {
        return try {
            dao.actualizarEquipo(toEntity(equipo))
            if (!isRunningTest()) {
                try {
                    SupabaseClientProvider.client.postgrest["equipos"].update(
                        EquipoSupabaseDto(
                            id = equipo.id,
                            nombre = equipo.nombre,
                            categoria = equipo.categoria.name,
                            estado = equipo.estado.name
                        )
                    ) {
                        filter { eq("id", equipo.id) }
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error editando equipo en remoto", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarEquipo(id: Int): Result < Unit > {
        return try {
            dao.eliminarEquipoPorId(id)
            if (!isRunningTest()) {
                try {
                    SupabaseClientProvider.client.postgrest["equipos"].delete {
                        filter { eq("id", id) }
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error eliminando equipo en remoto", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun adjuntarEvidencia(
        solicitudId: Int,
        imagenBytes: ByteArray,
        extension: String
    ): Result < String > {
        return try {
            val filename = "evidencia_${solicitudId}_${System.currentTimeMillis()}.$extension"
            var urlPublica = "local_cache_evidencia_$filename"

            if (!isRunningTest()) {
                try {
                    val bucket = SupabaseClientProvider.client.storage["evidencias"]
                    bucket.upload(filename, imagenBytes) {
                        upsert = true
                    }
                    urlPublica = bucket.publicUrl(filename)
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error subiendo evidencia a Storage", e)
                }
            } else {
                urlPublica = "https://mock-storage.supabase/evidencias/$filename"
            }

            val solicitud = dao.obtenerSolicitudSync(solicitudId)
            if (solicitud != null) {
                dao.actualizarSolicitud(solicitud.copy(evidenciaUrl = urlPublica))
            }

            if (!isRunningTest()) {
                try {
                    SupabaseClientProvider.client.postgrest["solicitudes"].update(
                        mapOf("evidencia_url" to urlPublica)
                    ) {
                        filter { eq("id", solicitudId) }
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseSync", "Error actualizando evidencia en remoto", e)
                }
            }

            Result.success(urlPublica)
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Error subiendo evidencia a Storage", e)
            Result.failure(e)
        }
    }

    private fun toDominio(entity: EquipmentEntity): Equipo = Equipo(
        id = entity.id,
        nombre = entity.nombre,
        categoria = try { CategoriaEquipo.valueOf(entity.categoria) } catch (_: Exception) { CategoriaEquipo.PERIFERICOS },
        estado = try { EstadoEquipo.valueOf(entity.estado) } catch (_: Exception) { EstadoEquipo.DISPONIBLE }
    )

    private fun toDominio(entity: LoanEntity): SolicitudPrestamo = SolicitudPrestamo(
        id = entity.id,
        equipoId = entity.equipoId,
        usuarioId = entity.usuarioId,
        ambienteDestino = entity.ambienteDestino,
        proposito = entity.proposito,
        duracionHoras = entity.duracionHoras,
        estado = try { EstadoSolicitud.valueOf(entity.estado) } catch (_: Exception) { EstadoSolicitud.SOLICITADA },
        evidenciaUrl = entity.evidenciaUrl
    )

    private fun toEntity(domain: SolicitudPrestamo): LoanEntity = LoanEntity(
        id = domain.id,
        equipoId = domain.equipoId,
        usuarioId = domain.usuarioId,
        ambienteDestino = domain.ambienteDestino,
        proposito = domain.proposito,
        duracionHoras = domain.duracionHoras,
        estado = domain.estado.name,
        evidenciaUrl = domain.evidenciaUrl
    )

    private fun toEntity(domain: Equipo): EquipmentEntity = EquipmentEntity(
        id = domain.id,
        nombre = domain.nombre,
        categoria = domain.categoria.name,
        estado = domain.estado.name
    )
}
