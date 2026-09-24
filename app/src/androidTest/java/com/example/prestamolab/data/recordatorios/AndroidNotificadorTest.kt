package com.example.prestamolab.data.recordatorios

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.model.Recordatorio
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** La notificación real del sistema (CA-HU09-02); el runner concede POST_NOTIFICATIONS. */
@RunWith(AndroidJUnit4::class)
class AndroidNotificadorTest {

    private val contexto = ApplicationProvider.getApplicationContext<Context>()
    private val manager = contexto.getSystemService(NotificationManager::class.java)
    private val notificador = AndroidNotificador(contexto)

    // Id alto para no chocar con avisos reales del teléfono
    private val recordatorio = Recordatorio(903, "Kit Arduino Uno", "2030-01-01 12:00", 0)

    @After
    fun limpiar() {
        manager.cancel(903)
    }

    @Test
    fun TC_HU09_02_PublicaElAvisoConEquipoHoraYAccionDeApertura() {
        assertTrue(notificador.puedeNotificar())

        notificador.mostrar(recordatorio)

        val publicada = manager.activeNotifications.single { it.id == 903 }.notification
        assertEquals("Devuelve Kit Arduino Uno", publicada.extras.getString(Notification.EXTRA_TITLE))
        assertTrue(publicada.extras.getCharSequence(Notification.EXTRA_TEXT).toString().contains("2030-01-01 12:00"))
        assertNotNull("al tocarla abre el préstamo", publicada.contentIntent)
        assertEquals(AndroidNotificador.CANAL, publicada.channelId)
    }
}
