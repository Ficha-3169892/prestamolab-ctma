package com.example.prestamolab.model

import org.junit.Assert.*
import org.junit.Test

class ReglasActividadTest {

    private val ahora = ReglasActividad.aInstante("2026-09-24 08:00")!!

    private fun datos(
        titulo: String = "Práctica de osciloscopio",
        descripcion: String = "Medición de señales",
        ambiente: String = "Laboratorio 302",
        fecha: String = "2026-09-25 10:00"
    ) = DatosActividad(titulo, descripcion, ambiente, fecha)

    @Test
    fun DatosValidos_NoTienenErrores() {
        assertFalse(ReglasActividad.validar(datos(), ahora).hayErrores)
    }

    @Test
    fun TC_HU11_02_TituloVacio_MuestraErrorDelCampo() {
        val errores = ReglasActividad.validar(datos(titulo = "  "), ahora)

        assertEquals("El título es obligatorio.", errores.titulo)
        assertNull(errores.fecha)
    }

    @Test
    fun TC_HU11_02_FechaPasada_MuestraErrorDelCampo() {
        val errores = ReglasActividad.validar(datos(fecha = "2026-09-23 10:00"), ahora)

        assertEquals("La fecha no puede estar en el pasado.", errores.fecha)
        assertNull(errores.titulo)
    }

    @Test
    fun LaHoraActualTodaviaEsValida() {
        assertNull(ReglasActividad.validar(datos(fecha = "2026-09-24 08:00"), ahora).fecha)
    }

    @Test
    fun FechaConFormatoInvalidoOInexistente_MuestraElFormato() {
        listOf("25/09/2026", "2026-09-25", "2026-02-30 10:00", "2026-09-25 25:00").forEach {
            assertEquals(it, "Usa el formato AAAA-MM-DD HH:MM", ReglasActividad.validar(datos(fecha = it), ahora).fecha)
        }
        assertEquals("La fecha es obligatoria.", ReglasActividad.validar(datos(fecha = ""), ahora).fecha)
    }

    @Test
    fun AmbienteObligatorioYDescripcionOpcional() {
        assertEquals("El ambiente es obligatorio.", ReglasActividad.validar(datos(ambiente = ""), ahora).ambiente)
        assertFalse(ReglasActividad.validar(datos(descripcion = ""), ahora).hayErrores)
    }

    @Test
    fun LongitudesMaximas() {
        val errores = ReglasActividad.validar(
            datos(
                titulo = "a".repeat(ReglasActividad.TITULO_MAXIMO + 1),
                descripcion = "a".repeat(ReglasActividad.DESCRIPCION_MAXIMA + 1),
                ambiente = "a".repeat(ReglasActividad.AMBIENTE_MAXIMO + 1)
            ),
            ahora
        )

        assertNotNull(errores.titulo)
        assertNotNull(errores.descripcion)
        assertNotNull(errores.ambiente)
        assertEquals(errores.titulo, errores.primero)
    }
}
