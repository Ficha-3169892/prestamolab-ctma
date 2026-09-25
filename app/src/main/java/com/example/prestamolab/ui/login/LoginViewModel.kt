package com.example.prestamolab.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolab.data.local.UserSessionManager
import com.example.prestamolab.data.remote.SupabaseClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val access_token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String
)

@Serializable
data class PerfilDto(
    val id: String,
    val rol: String
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val userRole: String = "usuario"
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = UserSessionManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun login(onSuccess: (String) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val url = "${SupabaseClient.supabaseUrl}/auth/v1/token?grant_type=password"
                val response = SupabaseClient.httpClient.post(url) {
                    header("apikey", SupabaseClient.supabaseKey)
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, password))
                }

                if (response.status.isSuccess()) {
                    val authResp = response.body<AuthResponse>()
                    val userId = authResp.user.id
                    val token = authResp.access_token

                    val profileUrl = "${SupabaseClient.supabaseUrl}/rest/v1/perfiles?id=eq.$userId"
                    val profileResponse = SupabaseClient.httpClient.get(profileUrl) {
                        header("apikey", SupabaseClient.supabaseKey)
                        header("Authorization", "Bearer $token")
                    }

                    val roles = if (profileResponse.status.isSuccess()) {
                        profileResponse.body<List<PerfilDto>>()
                    } else {
                        emptyList()
                    }

                    val rol = roles.firstOrNull()?.rol ?: "usuario"
                    sessionManager.saveSession(userId, rol, email)

                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true, userRole = rol)
                    onSuccess(rol)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error al iniciar sesión")
            }
        }
    }
}
