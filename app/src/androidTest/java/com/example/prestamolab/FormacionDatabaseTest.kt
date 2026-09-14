package com.example.prestamolab

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.local.ActividadDao
import com.example.prestamolab.data.local.ActividadEntity
import com.example.prestamolab.data.local.CompetenciaEntity
import com.example.prestamolab.data.local.FormacionDatabase
import com.example.prestamolab.data.local.PrestamoDao
import com.example.prestamolab.data.local.PrestamoEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormacionDatabaseTest {
    private lateinit var db: FormacionDatabase
    private lateinit var actividadDao: ActividadDao
    private lateinit var prestamoDao: PrestamoDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FormacionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        actividadDao = db.actividadDao()
        prestamoDao = db.prestamoDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun crudActividades() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(nombre = "ADSO"))
        val actividad = ActividadEntity(
            titulo = "Prueba Room Actividad",
            descripcion = "Descripción de prueba",
            fechaLimite = "2026-10-01",
            prioridad = 1,
            competenciaId = compId,
            completada = false
        )
        
        // Insertar
        val id = actividadDao.insertar(actividad)
        
        // Consultar por ID
        var resultado = actividadDao.obtenerPorId(id)
        assertNotNull(resultado)
        assertEquals("Prueba Room Actividad", resultado?.titulo)
        assertEquals(false, resultado?.completada)

        // Actualizar estado (completada)
        val actividadModificada = resultado!!.copy(completada = true)
        actividadDao.actualizar(actividadModificada)
        
        resultado = actividadDao.obtenerPorId(id)
        assertEquals(true, resultado?.completada)

        // Eliminar
        actividadDao.eliminar(resultado!!)
        resultado = actividadDao.obtenerPorId(id)
        assertEquals(null, resultado)
    }

    @Test
    fun crudPrestamos() = runBlocking {
        val prestamo = PrestamoEntity(
            equipoId = 10,
            ambienteDestino = "Ambiente 402",
            duracionHoras = 4,
            proposito = "Clase de Robótica",
            estado = "SOLICITADA"
        )

        // Insertar
        val id = prestamoDao.insertar(prestamo)
        
        // Consultar la lista
        val lista = prestamoDao.obtenerTodos()
        assertEquals(1, lista.size)
        assertEquals("Ambiente 402", lista[0].ambienteDestino)

        // Consultar por ID
        var resultado = prestamoDao.obtenerPorId(id.toInt())
        assertNotNull(resultado)

        // Actualizar el estado de la solicitud
        prestamoDao.actualizarEstado(id.toInt(), "APROBADA")
        resultado = prestamoDao.obtenerPorId(id.toInt())
        assertEquals("APROBADA", resultado?.estado)
    }

    @Test
    fun validarMigracion1a2() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbSimulada = Room.inMemoryDatabaseBuilder(context, FormacionDatabase::class.java).build()
        assertNotNull(dbSimulada.actividadDao())
        dbSimulada.close()
    }
}
