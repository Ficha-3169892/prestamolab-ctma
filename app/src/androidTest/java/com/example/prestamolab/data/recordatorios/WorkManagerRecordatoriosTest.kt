package com.example.prestamolab.data.recordatorios

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.prestamolab.model.Recordatorio
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Programación real en WorkManager (CA-HU09-01 y CA-HU09-03). Las horas quedan en el futuro, así que
 * ningún aviso llega a mostrarse durante la prueba.
 */
@RunWith(AndroidJUnit4::class)
class WorkManagerRecordatoriosTest {

    private val contexto = ApplicationProvider.getApplicationContext<Context>()
    private val workManager = WorkManager.getInstance(contexto)
    private val ahora = System.currentTimeMillis()
    private val programador = WorkManagerRecordatorios(contexto) { ahora }

    // Ids altos para no chocar con préstamos reales del teléfono
    private fun recordatorio(id: Int = 901, enHoras: Long = 2) =
        Recordatorio(id, "Kit Arduino Uno", "2030-01-01 12:00", ahora + TimeUnit.HOURS.toMillis(enHoras))

    private fun trabajos(id: Int) = workManager.getWorkInfosForUniqueWork(WorkManagerRecordatorios.nombreTrabajo(id)).get()

    @After
    fun limpiar() {
        workManager.cancelAllWorkByTag(WorkManagerRecordatorios.ETIQUETA).result.get()
        workManager.pruneWork().result.get()
    }

    @Test
    fun TC_HU09_01_SeProgramaUnTrabajoParaLaHoraDelAviso() = runTest {
        programador.sincronizar(listOf(recordatorio()))

        val trabajo = trabajos(901).single()
        assertEquals(WorkInfo.State.ENQUEUED, trabajo.state)
        assertTrue(WorkManagerRecordatorios.ETIQUETA in trabajo.tags)
        // El aviso se programa para la hora calculada (con margen por el tiempo de la prueba)
        assertEquals(recordatorio().instante.toDouble(), trabajo.nextScheduleTimeMillis.toDouble(), 60_000.0)
    }

    @Test
    fun SincronizarDosVecesNoDuplicaNiReprograma() = runTest {
        programador.sincronizar(listOf(recordatorio()))
        val primero = trabajos(901).single().id

        programador.sincronizar(listOf(recordatorio()))

        assertEquals(listOf(primero), trabajos(901).map { it.id })
    }

    @Test
    fun SiCambiaLaHoraSeReprograma() = runTest {
        programador.sincronizar(listOf(recordatorio(enHoras = 2)))

        programador.sincronizar(listOf(recordatorio(enHoras = 3)))

        val activo = trabajos(901).single { it.state == WorkInfo.State.ENQUEUED }
        assertEquals(recordatorio(enHoras = 3).instante.toDouble(), activo.nextScheduleTimeMillis.toDouble(), 60_000.0)
    }

    @Test
    fun TC_HU09_03_ElPrestamoQueSaleDeLaListaQuedaCancelado() = runTest {
        programador.sincronizar(listOf(recordatorio(901), recordatorio(902)))

        // El préstamo 901 se devolvió: ya no está entre los recordatorios deseados
        programador.sincronizar(listOf(recordatorio(902)))

        assertEquals(WorkInfo.State.CANCELLED, trabajos(901).single().state)
        assertEquals(WorkInfo.State.ENQUEUED, trabajos(902).single().state)
    }

    @Test
    fun AlCerrarSesionSeCancelanTodos() = runTest {
        programador.sincronizar(listOf(recordatorio(901), recordatorio(902)))

        programador.cancelarTodos()

        assertEquals(WorkInfo.State.CANCELLED, trabajos(901).single().state)
        assertEquals(WorkInfo.State.CANCELLED, trabajos(902).single().state)
    }
}
