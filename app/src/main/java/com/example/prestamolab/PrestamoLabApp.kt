package com.example.prestamolab

import android.app.Application
import com.example.prestamolab.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class PrestamoLabApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    /** Vive lo mismo que el proceso; no se cancela. */
    private val appScope: CoroutineScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        container.iniciarSincronizacion(appScope)
    }
}
