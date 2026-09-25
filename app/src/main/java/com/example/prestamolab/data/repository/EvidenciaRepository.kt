package com.example.prestamolab.data.repository

import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EvidenceEntity
import com.example.prestamolab.data.local.entity.aDominio
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.EtapaEvidencia
import com.example.prestamolab.model.Evidencia
import com.example.prestamolab.model.Ubicacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Evidencias fotográficas de un préstamo (HU-08). */
interface EvidenciaRepository {
    fun evidencias(solicitudId: Int): Flow<List<Evidencia>>

    /** CA-HU08-02: asocia la foto ya guardada al préstamo. Solo un préstamo PRESTADO admite evidencias. */
    suspend fun registrar(solicitudId: Int, etapa: EtapaEvidencia, uriLocal: String): Result<Evidencia>

    /** Agrega el GPS a una evidencia ya guardada: la ubicación puede tardar varios segundos. */
    suspend fun agregarUbicacion(evidenciaId: Int, ubicacion: Ubicacion)
}

class RoomEvidenciaRepository(
    db: PrestamoLabDatabase,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val generarRemoteId: () -> String = { UUID.randomUUID().toString() },
    private val alCambiarLocalmente: () -> Unit = {}
) : EvidenciaRepository {

    private val dao = db.evidenceDao()
    private val loanDao = db.loanDao()

    override fun evidencias(solicitudId: Int): Flow<List<Evidencia>> =
        dao.observarPorPrestamo(solicitudId).map { lista -> lista.map(EvidenceEntity::aDominio) }

    override suspend fun registrar(solicitudId: Int, etapa: EtapaEvidencia, uriLocal: String): Result<Evidencia> {
        val prestamo = loanDao.obtener(solicitudId)
            ?: return Result.failure(NoSuchElementException("Préstamo no encontrado"))
        if (prestamo.status != EstadoSolicitud.PRESTADO) {
            return Result.failure(IllegalStateException("Solo se adjuntan evidencias a un préstamo entregado"))
        }
        val evidencia = EvidenceEntity(
            remoteId = generarRemoteId(),
            loanId = solicitudId,
            stage = etapa,
            localUri = uriLocal,
            takenAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(reloj()))
        )
        val id = dao.insertar(evidencia).toInt()
        alCambiarLocalmente()
        return Result.success(evidencia.copy(id = id).aDominio())
    }

    override suspend fun agregarUbicacion(evidenciaId: Int, ubicacion: Ubicacion) {
        dao.guardarUbicacion(evidenciaId, ubicacion.latitud, ubicacion.longitud)
        alCambiarLocalmente()
    }
}
