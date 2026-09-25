package com.example.prestamolab.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit

interface ProgramadorSincronizacion {
    /** Sincroniza en cuanto haya conexión (CA-HU07-01). */
    fun sincronizarAhora()
    fun programarPeriodica()
    fun cancelar()

    /** true mientras un trabajo de sincronización se ejecuta (la UI muestra "Subiendo…"). */
    val sincronizando: Flow<Boolean>
}

class WorkManagerProgramador(private val contexto: Context) : ProgramadorSincronizacion {

    private val workManager by lazy { WorkManager.getInstance(contexto) }

    override val sincronizando: Flow<Boolean> by lazy {
        combine(
            workManager.getWorkInfosForUniqueWorkFlow(TRABAJO_INMEDIATO),
            workManager.getWorkInfosForUniqueWorkFlow(TRABAJO_PERIODICO)
        ) { inmediato, periodico -> (inmediato + periodico).any { it.state == WorkInfo.State.RUNNING } }
            .distinctUntilChanged()
    }

    private val conRed = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun sincronizarAhora() {
        val solicitud = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(conRed)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, ESPERA_INICIAL_S, TimeUnit.SECONDS)
            .build()
        // REPLACE es seguro porque los envíos son upsert: un trabajo interrumpido se repite sin duplicar
        workManager.enqueueUniqueWork(TRABAJO_INMEDIATO, ExistingWorkPolicy.REPLACE, solicitud)
    }

    override fun programarPeriodica() {
        val solicitud = PeriodicWorkRequestBuilder<SincronizacionWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conRed)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, ESPERA_INICIAL_S, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(TRABAJO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP, solicitud)
    }

    override fun cancelar() {
        workManager.cancelUniqueWork(TRABAJO_INMEDIATO)
        workManager.cancelUniqueWork(TRABAJO_PERIODICO)
    }

    private companion object {
        const val TRABAJO_INMEDIATO = "sincronizacion-inmediata"
        const val TRABAJO_PERIODICO = "sincronizacion-periodica"
        const val ESPERA_INICIAL_S = 30L
    }
}
