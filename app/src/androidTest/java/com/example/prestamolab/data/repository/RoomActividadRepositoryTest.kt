package com.example.prestamolab.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.model.DatosActividad
import com.example.prestamolab.model.ReglasActividad
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Actividades formativas sobre Room en memoria con la actividad semilla (HU-11). */
@RunWith(AndroidJUnit4::class)
class RoomActividadRepositoryTest {

    private lateinit var db: PrestamoLabDatabase
    private lateinit var repository: RoomActividadRepository
    private var cambiosLocales = 0

    private val datos = DatosActividad("Taller de soldadura", " SMD ", "Ambiente de Electrónica", "2026-10-05 14:00")

    @Before
    fun setup() {
        db = PrestamoLabDatabase.construir(ApplicationProvider.getApplicationContext<Context>(), enMemoria = true)
        DatosSemilla.reiniciar(db)
        repository = RoomActividadRepository(
            db,
            reloj = { ReglasActividad.aInstante("2026-09-24 08:00")!! },
            generarRemoteId = { "uuid-actividad" },
            alCambiarLocalmente = { cambiosLocales++ }
        )
    }

    @After
    fun cerrar() {
        db.close()
    }

    @Test
    fun TC_HU11_01_CrearActividad_QuedaGuardadaYApareceEnLaLista() = runTest {
        val creada = repository.crear(datos, "uuid-instructor").getOrThrow()

        assertEquals("SMD", creada.descripcion)
        // Ordenadas por fecha: la nueva (2026) va antes que la semilla (2030)
        assertEquals(listOf("Taller de soldadura", "Práctica de osciloscopio"), repository.actividades.first().map { it.titulo })
        val entidad = db.activityDao().obtener(creada.id)!!
        assertEquals("uuid-actividad", entidad.remoteId)
        assertEquals(EstadoSincronizacion.PENDIENTE, entidad.syncStatus)
        assertEquals(1, cambiosLocales)
    }

    @Test
    fun TC_HU11_02_DatosInvalidos_NoSeGuardan() = runTest {
        assertTrue(repository.crear(datos.copy(titulo = ""), "uuid-instructor").isFailure)
        assertTrue(repository.crear(datos.copy(fecha = "2026-09-20 08:00"), "uuid-instructor").isFailure)

        assertEquals(1, repository.actividades.first().size)
        assertEquals(0, cambiosLocales)
    }

    @Test
    fun TC_HU11_03_EditarActividad_ReflejaLosCambios() = runTest {
        repository.editar(1, datos.copy(titulo = "Osciloscopio avanzado", fecha = "2030-01-16 08:00")).getOrThrow()

        val editada = repository.actividades.first().single()
        assertEquals("Osciloscopio avanzado", editada.titulo)
        assertEquals("2030-01-16 08:00", editada.fecha)
        assertEquals(EstadoSincronizacion.PENDIENTE, db.activityDao().obtener(1)?.syncStatus)
    }

    @Test
    fun TC_HU11_04_EliminarActividad_DesapareceYQuedaPendienteDeEnviar() = runTest {
        repository.eliminar(1).getOrThrow()

        assertTrue(repository.actividades.first().isEmpty())
        assertNull(repository.obtener(1))
        assertTrue(db.activityDao().pendientes().single().deleted)
    }
}
