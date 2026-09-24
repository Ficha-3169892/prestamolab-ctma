package com.example.prestamolab.data.recordatorios

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.testutil.FakeNotificador
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** El Worker muestra el aviso cuando llega la hora (CA-HU09-02) y nunca falla (CA-HU09-04). */
@RunWith(AndroidJUnit4::class)
class RecordatorioWorkerTest {

    private val app = ApplicationProvider.getApplicationContext<PrestamoLabApp>()
    private val notificador get() = app.container.notificador as FakeNotificador

    // Préstamo semilla #2: Kit Arduino Uno, PRESTADO
    private fun worker(solicitudId: Int = 2) = TestListenableWorkerBuilder<RecordatorioWorker>(app)
        .setInputData(
            workDataOf(
                RecordatorioWorker.SOLICITUD_ID to solicitudId,
                RecordatorioWorker.EQUIPO to "Kit Arduino Uno",
                RecordatorioWorker.HORA_LIMITE to "2026-09-03 12:00",
                RecordatorioWorker.INSTANTE to 0L
            )
        )
        .build()

    @Before
    fun preparar() {
        DatosSemilla.reiniciar(app.container.database)
        notificador.permitido = true
        notificador.mostrados.clear()
    }

    @Test
    fun TC_HU09_02_AlLlegarLaHora_MuestraElEquipoYLaHoraLimite() = runTest {
        assertEquals(ListenableWorker.Result.success(), worker().doWork())

        val aviso = notificador.mostrados.single()
        assertEquals(2, aviso.solicitudId)
        assertEquals("Kit Arduino Uno", aviso.nombreEquipo)
        assertEquals("2026-09-03 12:00", aviso.horaLimite)
    }

    @Test
    fun TC_HU09_04_SinPermiso_NoMuestraNadaYNoFalla() = runTest {
        notificador.permitido = false

        assertEquals(ListenableWorker.Result.success(), worker().doWork())
        assertTrue(notificador.mostrados.isEmpty())
    }

    @Test
    fun SiElPrestamoYaNoEstaEntregado_NoAvisa() = runTest {
        // Solicitud #1: SOLICITADA
        assertEquals(ListenableWorker.Result.success(), worker(solicitudId = 1).doWork())
        assertTrue(notificador.mostrados.isEmpty())
    }
}
