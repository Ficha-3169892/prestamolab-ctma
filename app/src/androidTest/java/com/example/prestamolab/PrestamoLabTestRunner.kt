package com.example.prestamolab

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import com.example.prestamolab.testutil.FakeNotificador
import com.example.prestamolab.testutil.FakePrestamosRemoteDataSource
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import com.example.prestamolab.testutil.SinProgramacion
import com.example.prestamolab.testutil.SinRecordatorios

/** Runner de las pruebas instrumentadas: sin red, sin Supabase y sin WorkManager automático. */
class PrestamoLabTestRunner : AndroidJUnitRunner() {
    override fun callApplicationOnCreate(app: Application) {
        // Antes de onCreate, que ya usa la sesión y los programadores de sincronización y recordatorios
        (app as PrestamoLabApp).container.apply {
            usuariosRemoteDataSource = FakeUsuariosDataSource()
            prestamosRemoteDataSource = FakePrestamosRemoteDataSource()
            programadorSincronizacion = SinProgramacion()
            programadorRecordatorios = SinRecordatorios()
            notificador = FakeNotificador()
        }
        // El estudiante semilla tiene un préstamo entregado: sin esto, el diálogo del sistema que pide
        // el permiso (CA-HU09-04) taparía la app en las pruebas de UI. Revocarlo mataría el proceso.
        // PermisoNotificacionesUiTest necesita el permiso sin conceder: se ejecuta con -e concederNotificaciones false
        val concederNotificaciones = InstrumentationRegistry.getArguments().getString("concederNotificaciones") != "false"
        if (concederNotificaciones && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uiAutomation.grantRuntimePermission(app.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        super.callApplicationOnCreate(app)
    }
}
