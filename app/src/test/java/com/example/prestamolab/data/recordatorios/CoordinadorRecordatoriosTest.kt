package com.example.prestamolab.data.recordatorios

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.model.CondicionEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.NuevaDevolucion
import com.example.prestamolab.model.Recordatorio
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class CoordinadorRecordatoriosTest {

    private class ProgramadorFalso : ProgramadorRecordatorios {
        var programados: List<Recordatorio>? = null
        var cancelacionesTotales = 0

        override suspend fun sincronizar(recordatorios: List<Recordatorio>) {
            programados = recordatorios
        }

        override suspend fun cancelarTodos() {
            cancelacionesTotales++
            programados = emptyList()
        }
    }

    private fun instante(texto: String) = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(texto)!!.time

    private val estudiante = Usuario("u-estudiante", "Estudiante CTMA", "e@sena.edu.co", Rol.ESTUDIANTE)
    private val instructor = Usuario("u-instructor", "Instructor CTMA", "i@sena.edu.co", Rol.INSTRUCTOR)

    private fun prestamo(id: Int, estado: EstadoSolicitud, equipoId: Int) = SolicitudPrestamo(
        id, equipoId, estudiante.id, estudiante.nombre, "Lab", "Proposito valido", 2,
        "2026-09-24 08:00", "2026-09-24 10:00", estado
    )

    private val repository = InMemoryPrestamoRepository(
        solicitudes = listOf(prestamo(1, EstadoSolicitud.SOLICITADA, 2), prestamo(2, EstadoSolicitud.PRESTADO, 5))
    )
    private val sesion = MutableStateFlow<Usuario?>(estudiante)
    private val programador = ProgramadorFalso()

    /** Corre [prueba] con el coordinador activo y lo detiene al final, para que runTest pueda terminar. */
    private fun conCoordinador(prueba: suspend TestScope.() -> Unit) = runTest {
        val coordinador = CoordinadorRecordatorios(sesion, repository, programador) { instante("2026-09-24 08:00") }
            .iniciar(this)
        advanceUntilIdle()
        prueba()
        coordinador.cancel()
    }

    @Test
    fun TC_HU09_01_ElPrestamoEntregadoTieneSuRecordatorio() = conCoordinador {
        val recordatorio = programador.programados!!.single()
        assertEquals(2, recordatorio.solicitudId)
        assertEquals("Kit Arduino Uno", recordatorio.nombreEquipo)
        assertEquals(instante("2026-09-24 09:30"), recordatorio.instante)
    }

    @Test
    fun TC_HU09_01_AlAprobarseUnaSolicitudSeProgramaSuRecordatorio() = conCoordinador {
        repository.aprobarSolicitud(1, instructor.id).getOrThrow()
        advanceUntilIdle()

        assertEquals(listOf(1, 2), programador.programados!!.map { it.solicitudId }.sorted())
    }

    @Test
    fun TC_HU09_03_AlDevolverSeCancelaElRecordatorio() = conCoordinador {
        repository.registrarDevolucion(NuevaDevolucion(2, CondicionEquipo.BUENO, "", null)).getOrThrow()
        advanceUntilIdle()

        assertEquals(emptyList<Recordatorio>(), programador.programados)
    }

    @Test
    fun AlCerrarSesionSeCancelanTodos() = conCoordinador {
        sesion.value = null
        advanceUntilIdle()

        assertEquals(1, programador.cancelacionesTotales)
        assertEquals(emptyList<Recordatorio>(), programador.programados)
    }

    @Test
    fun ElInstructorNoRecibeRecordatorios() {
        sesion.value = instructor

        conCoordinador { }

        assertEquals(1, programador.cancelacionesTotales)
        assertNull(programador.programados?.firstOrNull())
    }
}
