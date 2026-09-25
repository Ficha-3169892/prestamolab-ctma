package com.example.prestamolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.prestamolab.data.local.PrestamoDatabase
import com.example.prestamolab.data.local.UserSessionManager
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.navigation.AppNavigation
import com.example.prestamolab.viewmodel.AuthViewModel
import com.example.prestamolab.viewmodel.PrestamoViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Room
        val database = PrestamoDatabase.getDatabase(applicationContext)

        // 2. Retrofit API Service
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://api.ctma-prestamolab.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(com.example.prestamolab.data.remote.PrestamoApiService::class.java)

        // 3. Repository
        val repository = RoomPrestamoRepository(database.prestamoDao(), apiService)

        // Sincronizar catálogo y solicitudes al iniciar app (Local-First)
        lifecycleScope.launch {
            repository.sincronizarEquipos()
            repository.sincronizarSolicitudes()
        }

        // 4. ViewModels y SessionManager
        val viewModel = PrestamoViewModel(repository)
        val userSessionManager = UserSessionManager(applicationContext)
        val authViewModel = AuthViewModel(userSessionManager)

        val darkScheme = darkColorScheme(
            background = Color.Black,
            surface = Color(0xFF121212)
        )

        setContent {
            MaterialTheme(colorScheme = darkScheme) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    AppNavigation(
                        viewModel = viewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
