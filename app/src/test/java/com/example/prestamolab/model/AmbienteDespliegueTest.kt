package com.example.prestamolab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmbienteDespliegueTest {

    @Test
    fun `El nombre de BuildConfig se traduce sin importar mayusculas`() {
        assertEquals(AmbienteDespliegue.DEV, AmbienteDespliegue.desde("dev"))
        assertEquals(AmbienteDespliegue.STAGE, AmbienteDespliegue.desde("Stage"))
        assertEquals(AmbienteDespliegue.PROD, AmbienteDespliegue.desde(" PROD "))
    }

    @Test
    fun `Un nombre desconocido se trata como dev y nunca como prod`() {
        assertEquals(AmbienteDespliegue.DEV, AmbienteDespliegue.desde(""))
        assertEquals(AmbienteDespliegue.DEV, AmbienteDespliegue.desde("produccion"))
    }

    @Test
    fun `Solo dev y stage muestran la etiqueta del ambiente`() {
        assertEquals("Entorno: dev", AmbienteDespliegue.DEV.etiqueta)
        assertEquals("Entorno: stage", AmbienteDespliegue.STAGE.etiqueta)
        assertNull(AmbienteDespliegue.PROD.etiqueta)
    }
}
