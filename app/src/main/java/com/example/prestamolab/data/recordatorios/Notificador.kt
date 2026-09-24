package com.example.prestamolab.data.recordatorios

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.prestamolab.MainActivity
import com.example.prestamolab.model.PlanRecordatorios
import com.example.prestamolab.model.Recordatorio

interface Notificador {
    /** false si el usuario negó POST_NOTIFICATIONS (Android 13+) o desactivó las notificaciones. */
    fun puedeNotificar(): Boolean
    fun mostrar(recordatorio: Recordatorio)
}

class AndroidNotificador(private val contexto: Context) : Notificador {

    override fun puedeNotificar(): Boolean {
        val permiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permiso && NotificationManagerCompat.from(contexto).areNotificationsEnabled()
    }

    override fun mostrar(recordatorio: Recordatorio) {
        crearCanal()
        // CA-HU09-02: al tocarla se abre el préstamo; el requestCode separa los avisos de cada préstamo
        val alTocar = PendingIntent.getActivity(
            contexto,
            recordatorio.solicitudId,
            intentApertura(contexto, recordatorio.solicitudId),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(PlanRecordatorios.titulo(recordatorio))
            .setContentText(PlanRecordatorios.texto(recordatorio))
            .setStyle(NotificationCompat.BigTextStyle().bigText(PlanRecordatorios.texto(recordatorio)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(alTocar)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(contexto).notify(recordatorio.solicitudId, notificacion)
        } catch (e: SecurityException) {
            // El permiso se revocó entre la comprobación y el aviso: se omite (CA-HU09-04)
        }
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(CANAL, "Recordatorios de devolución", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Aviso 30 minutos antes de la hora límite de un préstamo"
        }
        contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    companion object {
        const val CANAL = "recordatorios-devolucion"
        const val EXTRA_SOLICITUD_ID = "com.example.prestamolab.SOLICITUD_ID"

        /** Abre la app sobre la instancia existente, que recibe el préstamo en onNewIntent. */
        fun intentApertura(contexto: Context, solicitudId: Int) = Intent(contexto, MainActivity::class.java)
            .putExtra(EXTRA_SOLICITUD_ID, solicitudId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
