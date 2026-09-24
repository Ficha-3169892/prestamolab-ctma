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
import com.example.prestamolab.data.remote.PrestamosRemoteDataSource
import com.example.prestamolab.data.remote.SupabasePrestamosDataSource
import com.example.prestamolab.data.remote.SupabaseRestClient
import com.example.prestamolab.data.repository.ActividadRepository
import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.data.repository.RoomActividadRepository
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.data.sync.AvisosSincronizacion
import com.example.prestamolab.data.sync.CoordinadorSincronizacion
import com.example.prestamolab.data.sync.ProgramadorSincronizacion
import com.example.prestamolab.data.sync.SincronizadorPrestamos
import com.example.prestamolab.data.sync.WorkManagerProgramador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Contenedor manual de dependencias con alcance de aplicación. Las propiedades `var` se
 * reemplazan en el runner de pruebas instrumentadas antes de que la app las use, para no
 * depender de la red, de Supabase, del GPS ni de WorkManager.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val supabase by lazy { SupabaseRestClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) }

    val database: PrestamoLabDatabase by lazy { PrestamoLabDatabase.construir(appContext) }
    val sessionStore: SessionStore by lazy { DataStoreSessionStore(appContext.sesionDataStore) }

    var usuariosRemoteDataSource: UsuariosRemoteDataSource = SupabaseUsuariosDataSource(supabase)
    val authRepository: AuthRepository by lazy { UsuariosAuthRepository(usuariosRemoteDataSource, sessionStore) }

    var prestamosRemoteDataSource: PrestamosRemoteDataSource = SupabasePrestamosDataSource(supabase)
    var programadorSincronizacion: ProgramadorSincronizacion = WorkManagerProgramador(appContext)
    val avisosSincronizacion = AvisosSincronizacion()
    val coordinadorSincronizacion by lazy {
        CoordinadorSincronizacion(
            authRepository,
            SincronizadorPrestamos(database, prestamosRemoteDataSource),
            avisosSincronizacion
        )
    }

    val prestamoRepository: PrestamoRepository by lazy {
        RoomPrestamoRepository(database, alCambiarLocalmente = { programadorSincronizacion.sincronizarAhora() })
    }

    val actividadRepository: ActividadRepository by lazy {
        RoomActividadRepository(database, alCambiarLocalmente = { programadorSincronizacion.sincronizarAhora() })
    }

    var locationProvider: LocationProvider = FusedLocationProvider(appContext)

    /** Sincroniza al iniciar sesión (y cada 15 minutos mientras dure); al cerrarla, cancela. */
    fun iniciarSincronizacion(scope: CoroutineScope) {
        scope.launch {
            authRepository.sesion
                .map { it?.usuario?.id }
                .distinctUntilChanged()
                .collect { usuarioId ->
                    if (usuarioId != null) {
                        programadorSincronizacion.sincronizarAhora()
                        programadorSincronizacion.programarPeriodica()
                    } else {
                        programadorSincronizacion.cancelar()
                    }
                }
        }
    }
}
