package com.example.prestamolab.testutil

import com.example.prestamolab.data.recordatorios.Notificador
import com.example.prestamolab.data.recordatorios.ProgramadorRecordatorios
import com.example.prestamolab.model.Recordatorio

/** Las pruebas de UI no programan WorkManager: los recordatorios se prueban por separado. */
class SinRecordatorios : ProgramadorRecordatorios {
    var programados: List<Recordatorio> = emptyList()

    override suspend fun sincronizar(recordatorios: List<Recordatorio>) {
        programados = recordatorios
    }

    override suspend fun cancelarTodos() {
        programados = emptyList()
    }
}

/** Registra los avisos en vez de publicarlos; [permitido] simula el permiso POST_NOTIFICATIONS. */
class FakeNotificador : Notificador {
    var permitido = true
    val mostrados = mutableListOf<Recordatorio>()

    override fun puedeNotificar() = permitido

    override fun mostrar(recordatorio: Recordatorio) {
        mostrados += recordatorio
    }
}
