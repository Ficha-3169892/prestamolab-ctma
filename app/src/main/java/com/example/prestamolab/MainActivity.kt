package com.example.prestamolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prestamolab.ui.PrestamoScreen
import com.example.prestamolab.ui.PrestamoViewModel
import com.example.prestamolab.ui.theme.PrestamoLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrestamoLabTheme {
                val viewModel: PrestamoViewModel = viewModel()
                PrestamoScreen(viewModel = viewModel)
            }
        }
    }
}