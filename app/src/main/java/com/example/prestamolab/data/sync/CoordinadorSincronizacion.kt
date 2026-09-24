package com.example.prestamolab.data.sync

import com.example.prestamolab.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/** Mensaje de sincronización para mostrar en la UI; null cuando no hay nada que avisar. */
class AvisosSincronizacion {
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun informar(mensaje: String) {
        _mensaje.value = mensaje
    }

    fun descartar() {
        _mensaje.value = null
    }
}

enum class AccionTrasSincronizar { EXITO, REINTENTAR, FALLO }

/** Decide qué hacer con cada resultado; el Worker solo traduce la acción a WorkManager. */
class CoordinadorSincronizacion(
    private val authRepository: AuthRepository,
    private val sincronizador: Sincronizador,
    private val avisos: AvisosSincronizacion
) {
    suspend fun ejecutar(): AccionTrasSincronizar {
        // Sin sesión no hay a quién sincronizar; los pendientes esperan al próximo login
        val usuario = authRepository.sesion.first()?.usuario ?: return AccionTrasSincronizar.EXITO

        return when (val resultado = sincronizador.sincronizar(usuario)) {
            is ResultadoSincronizacion.Exito -> {
                if (resultado.rechazados > 0) {
                    avisos.informar(
                        "El servidor rechazó ${resultado.rechazados} cambio(s). Se conservan en el teléfono."
                    )
                } else {
                    avisos.descartar()
                }
                AccionTrasSincronizar.EXITO
            }
            ResultadoSincronizacion.NoAutorizado -> {
                // CA-HU07-03: una credencial rechazada cierra la sesión y la raíz vuelve al login
                authRepository.cerrarSesion()
                AccionTrasSincronizar.FALLO
            }
            is ResultadoSincronizacion.RecursoNoEncontrado -> {
                avisos.informar("No se encontró un recurso en el servidor. Tus datos locales se conservan.")
                AccionTrasSincronizar.FALLO
            }
            is ResultadoSincronizacion.Rechazado -> {
                avisos.informar("El servidor rechazó la sincronización (HTTP ${resultado.codigo}). Tus datos locales se conservan.")
                AccionTrasSincronizar.FALLO
            }
            // Los registros siguen PENDIENTE; WorkManager reintenta con espera exponencial
            is ResultadoSincronizacion.ErrorTemporal -> AccionTrasSincronizar.REINTENTAR
        }
    }
}
