package com.example.prestamolab.model

import org.junit.Assert.*
import org.junit.Test

class RevisionSolicitudTest {

    @Test
    fun TC_HU14_02_AprobarSolicitada_PasaAPrestadoYEntregaElEquipo() {
        val resultado = RevisionSolicitud.aprobar(EstadoSolicitud.SOLICITADA).getOrThrow()

        assertEquals(EstadoSolicitud.PRESTADO, resultado.estadoSolicitud)
        assertEquals(EstadoEquipo.PRESTADO, resultado.estadoEquipo)
        assertNull(resultado.motivoRechazo)
    }

    @Test
    fun TC_HU14_03_RechazarSolicitada_GuardaElMotivoYLiberaElEquipo() {
        val resultado = RevisionSolicitud.rechazar(EstadoSolicitud.SOLICITADA, "  Equipo en calibración  ").getOrThrow()

        assertEquals(EstadoSolicitud.RECHAZADA, resultado.estadoSolicitud)
        assertEquals(EstadoEquipo.DISPONIBLE, resultado.estadoEquipo)
        assertEquals("Equipo en calibración", resultado.motivoRechazo)
    }

    @Test
    fun TC_HU14_03_RechazarSinMotivo_SeRechaza() {
        val resultado = RevisionSolicitud.rechazar(EstadoSolicitud.SOLICITADA, "   ")

        assertTrue(resultado.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun TC_HU14_03_MotivoDemasiadoLargo_SeRechaza() {
        val motivo = "a".repeat(RevisionSolicitud.MOTIVO_MAXIMO + 1)

        assertTrue(RevisionSolicitud.rechazar(EstadoSolicitud.SOLICITADA, motivo).isFailure)
        assertTrue(RevisionSolicitud.rechazar(EstadoSolicitud.SOLICITADA, motivo.drop(1)).isSuccess)
    }

    @Test
    fun TC_HU14_04_AprobarCanceladaORechazada_SeRechazaSinCambios() {
        listOf(EstadoSolicitud.CANCELADA, EstadoSolicitud.RECHAZADA).forEach { estado ->
            assertTrue("aprobar $estado", RevisionSolicitud.aprobar(estado).exceptionOrNull() is IllegalStateException)
        }
    }

    @Test
    fun TC_HU14_04_SoloSeRevisaUnaSolicitudPendiente() {
        (EstadoSolicitud.entries - EstadoSolicitud.SOLICITADA).forEach { estado ->
            assertTrue("aprobar $estado", RevisionSolicitud.aprobar(estado).isFailure)
            assertTrue("rechazar $estado", RevisionSolicitud.rechazar(estado, "Motivo válido").isFailure)
        }
    }
}
