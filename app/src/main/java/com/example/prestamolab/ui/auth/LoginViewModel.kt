package com.example.prestamolab.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.auth.AuthRepository
import com.example.prestamolab.data.auth.CredencialesInvalidasException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChanged(correo: String) {
        _uiState.update { it.copy(correo = correo, errorCorreo = null, mensajeError = null) }
    }

    fun onContrasenaChanged(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorContrasena = null, mensajeError = null) }
    }

    /**
     * Valida los campos y, si son válidos, intenta iniciar sesión. La navegación no ocurre aquí:
     * la dispara el cambio de sesión que observa la raíz de la app.
     */
    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        val correo = estado.correo.trim()
        val errorCorreo = when {
            correo.isEmpty() -> "El correo es obligatorio."
            !FORMATO_CORREO.matches(correo) -> "Ingresa un correo válido."
            else -> null
        }
        val errorContrasena = if (estado.contrasena.isEmpty()) "La contraseña es obligatoria." else null
        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
            return
        }

        _uiState.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            val resultado = authRepository.iniciarSesion(correo, estado.contrasena)
            _uiState.update { actual ->
                resultado.fold(
                    onSuccess = { actual.copy(cargando = false, contrasena = "") },
                    onFailure = { error ->
                        val mensaje = if (error is CredencialesInvalidasException) {
                            error.message
                        } else {
                            "No se pudo iniciar sesión. Intenta de nuevo."
                        }
                        actual.copy(cargando = false, mensajeError = mensaje)
                    }
                )
            }
        }
    }

    companion object {
        private val FORMATO_CORREO = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrestamoLabApp
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}
