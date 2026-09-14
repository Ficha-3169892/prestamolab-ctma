package com.tu_paquete

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tu_paquete.data.local.ActividadDao
import com.tu_paquete.data.local.ActividadEntity
import com.tu_paquete.data.local.CompetenciaEntity
import com.tu_paquete.data.local.FormacionDatabase
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
    private lateinit var dao: ActividadDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FormacionDatabase::class.java).build()
        dao = db.actividadDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertarYObtenerActividad() = runBlocking {
        val compId = dao.insertarCompetencia(CompetenciaEntity(nombre = "ADSO"))
        val actividad = ActividadEntity(
            titulo = "Prueba Room",
            descripcion = "Descripción de prueba",
            fechaLimite = "2026-10-01",
            prioridad = 1,
            competenciaId = compId,
            completada = false
        )
        val id = dao.insertar(actividad)
        val resultado = dao.obtenerPorId(id)

        assertNotNull(resultado)
        assertEquals("Prueba Room", resultado?.titulo)
    }
}