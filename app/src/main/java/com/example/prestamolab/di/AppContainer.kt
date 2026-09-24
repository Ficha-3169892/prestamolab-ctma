package com.example.prestamolab.di

import android.content.Context
import com.example.prestamolab.data.auth.AuthRepository
import com.example.prestamolab.data.auth.DataStoreSessionStore
import com.example.prestamolab.data.auth.DemoAuthRepository
import com.example.prestamolab.data.auth.SessionStore
import com.example.prestamolab.data.auth.sesionDataStore
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.data.repository.PrestamoRepository

/** Contenedor manual de dependencias con alcance de aplicación. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val prestamoRepository: PrestamoRepository by lazy { InMemoryPrestamoRepository() }
    val sessionStore: SessionStore by lazy { DataStoreSessionStore(appContext.sesionDataStore) }
    val authRepository: AuthRepository by lazy { DemoAuthRepository(sessionStore) }
}
