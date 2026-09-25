package com.example.prestamolab.ui.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.auth.AuthRepository
import com.example.prestamolab.data.recordatorios.AperturasPendientes
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Autenticado(val usuario: Usuario) : EstadoSesion
}

/** Estado de sesión que decide entre la pantalla de login y el área autenticada. */
class SesionViewModel(
    private val authRepository: AuthRepository,
    private val aperturas: AperturasPendientes = AperturasPendientes()
) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = authRepository.sesion
        .map { sesion -> if (sesion == null) EstadoSesion.SinSesion else EstadoSesion.Autenticado(sesion.usuario) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    /** Préstamo que pidió abrir una notificación de recordatorio (CA-HU09-02). */
    val aperturaPendiente: StateFlow<Int?> = aperturas.solicitudId

    fun consumirApertura() = aperturas.consumir()

    fun cerrarSesion() {
        viewModelScope.launch { authRepository.cerrarSesion() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                SesionViewModel(app.container.authRepository, app.container.aperturas)
            }
        }
    }
}
