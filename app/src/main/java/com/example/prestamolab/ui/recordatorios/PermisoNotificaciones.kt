package com.example.prestamolab.ui.recordatorios

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * CA-HU09-04: el permiso se pide cuando hace falta un recordatorio (hay un préstamo entregado),
 * no al abrir la app, y una sola vez por sesión. Negarlo no bloquea nada.
 */
fun debePedirPermisoNotificaciones(
    sdk: Int,
    concedido: Boolean,
    hayPrestamoEntregado: Boolean,
    yaSePidio: Boolean
): Boolean = sdk >= Build.VERSION_CODES.TIRAMISU && !concedido && hayPrestamoEntregado && !yaSePidio

@Composable
fun PedirPermisoNotificaciones(hayPrestamoEntregado: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val contexto = LocalContext.current
    var yaSePidio by rememberSaveable { mutableStateOf(false) }
    // El resultado no cambia el flujo: sin permiso el préstamo sigue igual y el aviso se omite
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(hayPrestamoEntregado) {
        val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (debePedirPermisoNotificaciones(Build.VERSION.SDK_INT, concedido, hayPrestamoEntregado, yaSePidio)) {
            yaSePidio = true
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
