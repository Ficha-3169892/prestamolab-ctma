package com.example.prestamolab.data.local

import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.local.entity.EvidenceEntity
import com.example.prestamolab.data.local.entity.aDominio
import com.example.prestamolab.model.EstadoEvidencia
import com.example.prestamolab.model.EtapaEvidencia
import org.junit.Assert.assertEquals
import org.junit.Test

class EvidenceEntityTest {

    private fun evidencia(estado: EstadoSincronizacion) = EvidenceEntity(
        remoteId = "e1", loanId = 2, stage = EtapaEvidencia.ENTREGA, localUri = "content://x/1.jpg",
        takenAt = "2026-09-25 10:00", syncStatus = estado
    )

    @Test
    fun ElEstadoDeSincronizacionSeTraduceAlEstadoDeLaEvidencia() {
        assertEquals(EstadoEvidencia.LOCAL, evidencia(EstadoSincronizacion.PENDIENTE).aDominio().estado)
        assertEquals(EstadoEvidencia.SINCRONIZADA, evidencia(EstadoSincronizacion.SINCRONIZADO).aDominio().estado)
        assertEquals(EstadoEvidencia.FALLIDA, evidencia(EstadoSincronizacion.ERROR).aDominio().estado)
    }
}
