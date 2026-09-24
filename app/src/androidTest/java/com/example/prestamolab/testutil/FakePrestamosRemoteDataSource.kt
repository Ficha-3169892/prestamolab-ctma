package com.example.prestamolab.testutil

import com.example.prestamolab.data.remote.DevolucionRemota
import com.example.prestamolab.data.remote.EquipoRemoto
import com.example.prestamolab.data.remote.PrestamoRemoto
import com.example.prestamolab.data.remote.PrestamosRemoteDataSource

/** Tablas remotas en memoria; [error] simula un fallo de red o una respuesta HTTP de error. */
class FakePrestamosRemoteDataSource : PrestamosRemoteDataSource {
    val equipos = mutableListOf<EquipoRemoto>()
    val prestamos = mutableListOf<PrestamoRemoto>()
    val devoluciones = mutableListOf<DevolucionRemota>()

    /** Si no es null, cualquier llamada lanza esta excepción. */
    var error: Exception? = null

    /** Rechaza solo el préstamo con este id, para simular el error de un registro concreto. */
    var prestamoRechazado: String? = null
    var errorDeRechazo: Exception? = null

    val filtrosDePrestamos = mutableListOf<String?>()

    override suspend fun equipos(): List<EquipoRemoto> {
        lanzarSiHayError()
        return equipos.toList()
    }

    override suspend fun prestamos(usuarioId: String?): List<PrestamoRemoto> {
        lanzarSiHayError()
        filtrosDePrestamos += usuarioId
        return prestamos.filter { usuarioId == null || it.usuarioId == usuarioId }
    }

    override suspend fun devoluciones(usuarioId: String?): List<DevolucionRemota> {
        lanzarSiHayError()
        val suyos = prestamos.filter { usuarioId == null || it.usuarioId == usuarioId }.map { it.id }.toSet()
        return devoluciones.filter { it.prestamoId in suyos }
    }

    override suspend fun guardarEquipo(equipo: EquipoRemoto) {
        lanzarSiHayError()
        equipos.removeAll { it.id == equipo.id }
        equipos += equipo
    }

    override suspend fun guardarPrestamo(prestamo: PrestamoRemoto) {
        lanzarSiHayError()
        if (prestamo.id == prestamoRechazado) errorDeRechazo?.let { throw it }
        prestamos.removeAll { it.id == prestamo.id }
        prestamos += prestamo
    }

    override suspend fun guardarDevolucion(devolucion: DevolucionRemota) {
        lanzarSiHayError()
        devoluciones.removeAll { it.id == devolucion.id }
        devoluciones += devolucion
    }

    private fun lanzarSiHayError() {
        error?.let { throw it }
    }
}
