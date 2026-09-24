package com.example.prestamolab.testutil

import com.example.prestamolab.data.auth.SessionStore
import com.example.prestamolab.model.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** SessionStore en memoria para pruebas JVM (sin DataStore ni Context). */
class FakeSessionStore(inicial: Sesion? = null) : SessionStore {
    private val _sesion = MutableStateFlow(inicial)
    override val sesion: StateFlow<Sesion?> = _sesion

    override suspend fun guardar(sesion: Sesion) {
        _sesion.value = sesion
    }

    override suspend fun limpiar() {
        _sesion.value = null
    }
}
