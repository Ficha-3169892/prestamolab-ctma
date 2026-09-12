package com.example.prestamolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.navigation.AppNavigation
import com.example.prestamolab.viewmodel.PrestamoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        val darkScheme = darkColorScheme(
            background = Color.Black,
            surface = Color(0xFF121212)
        )

        setContent {
            MaterialTheme(colorScheme = darkScheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
