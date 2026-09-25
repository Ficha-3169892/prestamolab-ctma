package com.example.prestamolab.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Actividad 32 de la guía: estados Local, Subiendo, Sincronizada y Fallida de una evidencia. */
class EstadoEvidenciaTest {

    @Test
    fun CadaEstadoTieneSuEtiqueta() {
        assertEquals("Local: pendiente de subir", etiquetaEstadoEvidencia(EstadoEvidencia.LOCAL, sincronizando = false))
        assertEquals("Subiendo…", etiquetaEstadoEvidencia(EstadoEvidencia.LOCAL, sincronizando = true))
        assertEquals("Sincronizada", etiquetaEstadoEvidencia(EstadoEvidencia.SINCRONIZADA, sincronizando = false))
        assertEquals("Fallida: el servidor no la aceptó", etiquetaEstadoEvidencia(EstadoEvidencia.FALLIDA, sincronizando = false))
    }

    @Test
    fun SoloLasLocalesSeMuestranSubiendo() {
        assertEquals("Sincronizada", etiquetaEstadoEvidencia(EstadoEvidencia.SINCRONIZADA, sincronizando = true))
        assertEquals("Fallida: el servidor no la aceptó", etiquetaEstadoEvidencia(EstadoEvidencia.FALLIDA, sincronizando = true))
    }
}
