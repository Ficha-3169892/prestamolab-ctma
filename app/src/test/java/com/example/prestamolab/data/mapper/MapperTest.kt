package com.example.prestamolab.data.mapper

import com.example.prestamolab.data.local.entity.EquipoEntity
import com.example.prestamolab.data.local.entity.SolicitudEntity
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `EquipoEntity toDomain maps correctly`() {
        val entity = EquipoEntity(1, "Laptop", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE)
        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.nombre, domain.nombre)
        assertEquals(entity.categoria, domain.categoria)
        assertEquals(entity.estado, domain.estado)
    }

    @Test
    fun `SolicitudEntity toDomain maps correctly`() {
        val entity = SolicitudEntity(
            id = 1,
            equipoId = 10,
            ambienteDestino = "Lab 1",
            proposito = "Práctica",
            duracionHoras = 2,
            estado = EstadoSolicitud.SOLICITADA,
            latitud = 1.0,
            longitud = 2.0,
            fotoUri = "path/to/photo",
            estadoSincronizacion = "SINCRONIZADA"
        )
        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.equipoId, domain.equipoId)
        assertEquals(entity.ambienteDestino, domain.ambienteDestino)
        assertEquals(entity.proposito, domain.proposito)
        assertEquals(entity.duracionHoras, domain.duracionHoras)
        assertEquals(entity.estado, domain.estado)
        assertEquals(entity.latitud, domain.latitud)
        assertEquals(entity.longitud, domain.longitud)
        assertEquals(entity.fotoUri, domain.fotoUri)
        assertEquals(entity.estadoSincronizacion, domain.estadoSincronizacion)
    }
}
