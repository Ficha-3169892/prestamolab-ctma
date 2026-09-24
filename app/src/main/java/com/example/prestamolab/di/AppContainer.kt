package com.example.prestamolab.di

import com.example.prestamolab.data.repository.InMemoryPrestamoRepository
import com.example.prestamolab.data.repository.PrestamoRepository

/** Contenedor manual de dependencias con alcance de aplicación. */
class AppContainer {
    val prestamoRepository: PrestamoRepository by lazy { InMemoryPrestamoRepository() }
}
