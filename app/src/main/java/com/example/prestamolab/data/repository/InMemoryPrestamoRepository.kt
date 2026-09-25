package com.example.prestamolab.data.repository

import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryPrestamoRepository : PrestamoRepository {

    private val _equipos = MutableStateFlow(listOf(
        Equipo(1, "Multímetro Digital", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Kit de Electrónica", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Cámara Fotográfica", CategoriaEquipo.PERIFERICOS, EstadoEquipo.RESERVADO)
    ))

    private val _solicitudes = MutableStateFlow < List < SolicitudPrestamo > > (emptyList())
    private var siguienteSolicitudId = 1
    private var siguienteEquipoId = 4

    override fun obtenerEquipos(): Flow < List < Equipo > > = _equipos.asStateFlow()

    override fun obtenerEquipo(id: Int): Flow < Equipo? > {
        return _equipos.map { lista -> lista.find { it.id == id } }
    }

    override fun obtenerSolicitudes(): Flow < List < SolicitudPrestamo > > = _solicitudes.asStateFlow()

    override fun obtenerSolicitud(id: Int): Flow < SolicitudPrestamo? > {
        return _solicitudes.map { lista -> lista.find { it.id == id } }
    }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result < Unit > {
        val listaEquipos = _equipos.value.toMutableList()
        val equipoIndex = listaEquipos.indexOfFirst { it.id == solicitud.equipoId }

        if (equipoIndex == -1) return Result.failure(Exception("Equipo no encontrado"))

        val equipo = listaEquipos[equipoIndex]
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(Exception("El equipo no está disponible"))
        }

        val nuevaSolicitud = solicitud.copy(id = siguienteSolicitudId++)
        _solicitudes.update { it + nuevaSolicitud }

        listaEquipos[equipoIndex] = equipo.copy(estado = EstadoEquipo.RESERVADO)
        _equipos.value = listaEquipos

        return Result.success(Unit)
    }

    override suspend fun cancelarSolicitud(id: Int): Result < Unit > {
        val listaSolicitudes = _solicitudes.value.toMutableList()
        val index = listaSolicitudes.indexOfFirst { it.id == id }

        if (index == -1) return Result.failure(Exception("Solicitud no encontrada"))

        val sol = listaSolicitudes[index]
        if (sol.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(Exception("Solo se pueden cancelar solicitudes en estado SOLICITADA"))
        }

        listaSolicitudes[index] = sol.copy(estado = EstadoSolicitud.CANCELADA)
        _solicitudes.value = listaSolicitudes

        val listaEquipos = _equipos.value.toMutableList()
        val equipoIndex = listaEquipos.indexOfFirst { it.id == sol.equipoId }
        if (equipoIndex != -1) {
            listaEquipos[equipoIndex] = listaEquipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
            _equipos.value = listaEquipos
        }

        return Result.success(Unit)
    }

    override suspend fun agregarEquipo(equipo: Equipo): Result < Unit > {
        val nuevoEquipo = equipo.copy(id = siguienteEquipoId++)
        _equipos.update { it + nuevoEquipo }
        return Result.success(Unit)
    }

    override suspend fun editarEquipo(equipo: Equipo): Result < Unit > {
        val lista = _equipos.value.toMutableList()
        val idx = lista.indexOfFirst { it.id == equipo.id }
        if (idx == -1) return Result.failure(Exception("Equipo no encontrado"))
        lista[idx] = equipo
        _equipos.value = lista
        return Result.success(Unit)
    }

    override suspend fun eliminarEquipo(id: Int): Result < Unit > {
        _equipos.update { lista -> lista.filterNot { it.id == id } }
        return Result.success(Unit)
    }

    override suspend fun adjuntarEvidencia(
        solicitudId: Int,
        imagenBytes: ByteArray,
        extension: String
    ): Result < String > {
        val urlPublica = "https://mock-supabase.storage/evidencias/solicitud_$solicitudId.$extension"
        val lista = _solicitudes.value.toMutableList()
        val idx = lista.indexOfFirst { it.id == solicitudId }
        if (idx == -1) return Result.failure(Exception("Solicitud no encontrada"))
        lista[idx] = lista[idx].copy(evidenciaUrl = urlPublica)
        _solicitudes.value = lista
        return Result.success(urlPublica)
    }

    override suspend fun sincronizarEquipos() {
        // No-op
    }

    override suspend fun sincronizarSolicitudes() {
        // No-op
    }
}
