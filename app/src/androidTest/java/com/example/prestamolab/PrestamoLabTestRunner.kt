package com.example.prestamolab

import android.app.Application
import androidx.test.runner.AndroidJUnitRunner
import com.example.prestamolab.testutil.FakePrestamosRemoteDataSource
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import com.example.prestamolab.testutil.SinProgramacion

/** Runner de las pruebas instrumentadas: sin red, sin Supabase y sin WorkManager automático. */
class PrestamoLabTestRunner : AndroidJUnitRunner() {
    override fun callApplicationOnCreate(app: Application) {
        // Antes de onCreate, que ya usa la sesión y el programador de sincronización
        (app as PrestamoLabApp).container.apply {
            usuariosRemoteDataSource = FakeUsuariosDataSource()
            prestamosRemoteDataSource = FakePrestamosRemoteDataSource()
            programadorSincronizacion = SinProgramacion()
        }
        super.callApplicationOnCreate(app)
    }
}
