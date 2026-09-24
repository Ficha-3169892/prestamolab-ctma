package com.example.prestamolab

import android.app.Application
import androidx.test.runner.AndroidJUnitRunner
import com.example.prestamolab.testutil.FakeUsuariosDataSource

/** Runner de las pruebas instrumentadas: el login usa usuarios falsos en lugar de Supabase. */
class PrestamoLabTestRunner : AndroidJUnitRunner() {
    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        (app as PrestamoLabApp).container.usuariosRemoteDataSource = FakeUsuariosDataSource()
    }
}
