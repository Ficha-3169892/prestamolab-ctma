package com.example.prestamolab.testutil

import com.example.prestamolab.data.sync.ProgramadorSincronizacion

/** Las pruebas de UI no disparan WorkManager: la sincronización se prueba por separado. */
class SinProgramacion : ProgramadorSincronizacion {
    var solicitudesInmediatas = 0

    override fun sincronizarAhora() {
        solicitudesInmediatas++
    }

    override fun programarPeriodica() = Unit
    override fun cancelar() = Unit

    override val sincronizando = kotlinx.coroutines.flow.MutableStateFlow(false)
}
