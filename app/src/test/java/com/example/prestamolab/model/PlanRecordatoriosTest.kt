package com.example.prestamolab.model

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class PlanRecordatoriosTest {

    private fun instante(texto: String) = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(texto)!!.time

    private val ahora = instante("2026-09-24 08:00")
    private val equipos = listOf(Equipo(5, "Kit Arduino Uno", "Herramienta", EstadoEquipo.PRESTADO))

    private fun prestamo(
        id: Int = 2,
        estado: EstadoSolicitud = EstadoSolicitud.PRESTADO,
        fin: String = "2026-09-24 12:00",
        usuarioId: String = "u1"
    ) = SolicitudPrestamo(id, 5, usuarioId, "Estudiante", "Lab", "Prototipo IoT", 4, "2026-09-24 08:00", fin, estado)

    @Test
    fun TC_HU09_01_PrestamoPrestado_SeProgramaTreintaMinutosAntesDelFin() {
        val recordatorio = PlanRecordatorios.calcular(listOf(prestamo()), equipos, "u1", ahora).single()

        assertEquals(instante("2026-09-24 11:30"), recordatorio.instante)
        assertEquals(2, recordatorio.solicitudId)
        assertEquals("Kit Arduino Uno", recordatorio.nombreEquipo)
        assertEquals("2026-09-24 12:00", recordatorio.horaLimite)
        assertEquals(instante("2026-09-24 11:30") - ahora, PlanRecordatorios.retraso(recordatorio, ahora))
    }

    @Test
    fun TC_HU09_03_PrestamoDevuelto_NoTieneRecordatorio() {
        val estados = EstadoSolicitud.entries - EstadoSolicitud.PRESTADO

        estados.forEach { estado ->
            assertTrue("$estado", PlanRecordatorios.calcular(listOf(prestamo(estado = estado)), equipos, "u1", ahora).isEmpty())
        }
    }

    @Test
    fun SoloLosPrestamosDelUsuario() {
        val lista = listOf(prestamo(id = 1, usuarioId = "otro"), prestamo(id = 2))

        assertEquals(listOf(2), PlanRecordatorios.calcular(lista, equipos, "u1", ahora).map { it.solicitudId })
    }

    @Test
    fun ConLaFechaLimiteVencida_NoSeProgramaNada() {
        assertTrue(PlanRecordatorios.calcular(listOf(prestamo(fin = "2026-09-24 07:59")), equipos, "u1", ahora).isEmpty())
    }

    @Test
    fun SiYaPasoElMomentoDelAviso_SeAvisaDeInmediato() {
        val recordatorio = PlanRecordatorios.calcular(listOf(prestamo(fin = "2026-09-24 08:10")), equipos, "u1", ahora).single()

        assertEquals(0, PlanRecordatorios.retraso(recordatorio, ahora))
    }

    @Test
    fun TC_HU09_02_LaNotificacionIndicaElEquipoYLaHoraLimite() {
        val recordatorio = PlanRecordatorios.calcular(listOf(prestamo()), equipos, "u1", ahora).single()

        assertEquals("Devuelve Kit Arduino Uno", PlanRecordatorios.titulo(recordatorio))
        assertTrue(PlanRecordatorios.texto(recordatorio).contains("Hora límite: 2026-09-24 12:00"))
    }
}
