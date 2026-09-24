package com.example.prestamolab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.prestamolab.data.recordatorios.AndroidNotificador
import com.example.prestamolab.ui.navigation.AppNavHost
import com.example.prestamolab.ui.theme.PrestamoLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Al recrearse (p. ej. al rotar) el préstamo ya se abrió
        if (savedInstanceState == null) abrirPrestamoDeLaNotificacion(intent)
        setContent {
            PrestamoLabTheme {
                AppNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        abrirPrestamoDeLaNotificacion(intent)
    }

    /** CA-HU09-02: tocar el recordatorio abre el préstamo. */
    private fun abrirPrestamoDeLaNotificacion(intent: Intent?) {
        val solicitudId = intent?.getIntExtra(AndroidNotificador.EXTRA_SOLICITUD_ID, -1) ?: -1
        if (solicitudId > 0) (application as PrestamoLabApp).container.aperturas.abrir(solicitudId)
    }
}
