package com.example.prestamolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.prestamolab.ui.navigation.AppNavHost
import com.example.prestamolab.ui.theme.PrestamoLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrestamoLabTheme {
                AppNavHost()
            }
        }
    }
}
