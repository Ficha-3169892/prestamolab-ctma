package com.example.prestamolab.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class FechasSupabaseTest {

    private val fechas = FechasSupabase(TimeZone.getTimeZone("America/Bogota"))

    @Test
    fun `la fecha local se envia en ISO-8601 con la zona del dispositivo`() {
        assertEquals("2026-09-24T08:00:00-05:00", fechas.aIso("2026-09-24 08:00"))
    }

    @Test
    fun `las fechas sin hora de la version 1 se envian a medianoche`() {
        assertEquals("2026-09-02T00:00:00-05:00", fechas.aIso("2026-09-02"))
    }

    @Test
    fun `un timestamptz de Supabase en UTC se muestra en hora local`() {
        assertEquals("2026-09-24 08:00", fechas.desdeIso("2026-09-24T13:00:00+00:00"))
    }

    @Test
    fun `se ignoran los microsegundos que agrega PostgREST`() {
        assertEquals("2026-09-24 08:00", fechas.desdeIso("2026-09-24T13:00:00.123456+00:00"))
    }

    @Test
    fun `ida y vuelta conserva el valor`() {
        assertEquals("2026-12-31 23:59", fechas.desdeIso(fechas.aIso("2026-12-31 23:59")))
    }
}
