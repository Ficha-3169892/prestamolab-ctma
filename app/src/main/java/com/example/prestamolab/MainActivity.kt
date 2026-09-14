package com.example.prestamolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.prestamolab.data.local.FormacionDatabase
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.navigation.AppNavigation
import com.example.prestamolab.viewmodel.PrestamoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            FormacionDatabase::class.java,
            "prestamolab.db"
        ).fallbackToDestructiveMigration().build()

        val repository = RoomPrestamoRepository(database.prestamoDao())
        
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PrestamoViewModel(repository) as T
            }
        }
        val viewModel = ViewModelProvider(this, viewModelFactory)[PrestamoViewModel::class.java]

        setContent {
            AppNavigation(viewModel = viewModel)
        }
    }
}