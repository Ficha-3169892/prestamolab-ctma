package com.example.prestamolab.data.recordatorios

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Préstamo que pidió abrir una notificación (CA-HU09-02); el área autenticada lo consume al navegar. */
class AperturasPendientes {
    private val _solicitudId = MutableStateFlow<Int?>(null)
    val solicitudId: StateFlow<Int?> = _solicitudId.asStateFlow()

    fun abrir(solicitudId: Int) {
        _solicitudId.value = solicitudId
    }

    fun consumir() {
        _solicitudId.value = null
    }
}
