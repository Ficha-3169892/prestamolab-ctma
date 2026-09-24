package com.example.prestamolab.di

import android.content.Context
import com.example.prestamolab.BuildConfig
import com.example.prestamolab.data.auth.AuthRepository
import com.example.prestamolab.data.auth.DataStoreSessionStore
import com.example.prestamolab.data.auth.SessionStore
import com.example.prestamolab.data.auth.SupabaseUsuariosDataSource
import com.example.prestamolab.data.auth.UsuariosAuthRepository
import com.example.prestamolab.data.auth.UsuariosRemoteDataSource
import com.example.prestamolab.data.auth.sesionDataStore
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.location.FusedLocationProvider
import com.example.prestamolab.data.location.LocationProvider
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.data.repository.RoomPrestamoRepository

/** Contenedor manual de dependencias con alcance de aplicación. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: PrestamoLabDatabase by lazy { PrestamoLabDatabase.construir(appContext) }
    val prestamoRepository: PrestamoRepository by lazy { RoomPrestamoRepository(database) }
    val sessionStore: SessionStore by lazy { DataStoreSessionStore(appContext.sesionDataStore) }

    /**
     * Reemplazable antes del primer uso de [authRepository]: el runner de pruebas instrumentadas
     * lo cambia por uno falso para no depender de la red ni de los usuarios de Supabase.
     */
    var usuariosRemoteDataSource: UsuariosRemoteDataSource =
        SupabaseUsuariosDataSource(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY)
    val authRepository: AuthRepository by lazy { UsuariosAuthRepository(usuariosRemoteDataSource, sessionStore) }

    /** Reemplazable en pruebas instrumentadas para no depender del GPS real. */
    var locationProvider: LocationProvider = FusedLocationProvider(appContext)
}
