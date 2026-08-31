package com.example.prestamolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.navigation.AppNavigation
import com.example.prestamolab.viewmodel.PrestamoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = InMemoryPrestamoRepository()
        val viewModel = PrestamoViewModel(repository)

        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
