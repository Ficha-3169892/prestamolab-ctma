package com.example.prestamolab.data.recordatorios

import com.example.prestamolab.data.repository.PrestamoRepository
import com.example.prestamolab.model.PlanRecordatorios
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Mantiene los recordatorios al día con los préstamos del estudiante (HU-09): basta con que un
 * préstamo pase a PRESTADO (al aprobarlo, o al recibirlo por sincronización) para programar su aviso,
 * y con que deje de estarlo (devolución) para cancelarlo. Al cerrar sesión se cancelan todos.
 */
class CoordinadorRecordatorios(
    private val usuario: Flow<Usuario?>,
    private val repository: PrestamoRepository,
    private val programador: ProgramadorRecordatorios,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun iniciar(scope: CoroutineScope): Job = scope.launch {
        usuario.distinctUntilChanged()
            .flatMapLatest { actual ->
                // Solo el estudiante devuelve equipos; el instructor no recibe estos avisos
                if (actual == null || actual.rol != Rol.ESTUDIANTE) flowOf(null)
                else combine(repository.solicitudes, repository.equipos) { solicitudes, equipos ->
                    PlanRecordatorios.calcular(solicitudes, equipos, actual.id, reloj())
                }
            }
            .distinctUntilChanged()
            .collect { recordatorios ->
                if (recordatorios == null) programador.cancelarTodos() else programador.sincronizar(recordatorios)
            }
    }
}

