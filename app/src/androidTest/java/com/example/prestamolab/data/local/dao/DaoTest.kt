package com.example.prestamolab.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.local.database.PrestamoDatabase
import com.example.prestamolab.data.local.entity.EquipoEntity
import com.example.prestamolab.data.local.entity.SolicitudEntity
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: PrestamoDatabase
    private lateinit var equipoDao: EquipoDao
    private lateinit var solicitudDao: SolicitudDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PrestamoDatabase::class.java).build()
        equipoDao = db.equipoDao()
        solicitudDao = db.solicitudDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadEquipos() = runBlocking {
        val equipos = listOf(
            EquipoEntity(1, "Laptop", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE),
            EquipoEntity(2, "Cámara", CategoriaEquipo.AUDIOVISUAL, EstadoEquipo.DISPONIBLE)
        )
        equipoDao.insertEquipos(equipos)

        val result = equipoDao.getAllEquipos().first()
        assertEquals(2, result.size)
        assertEquals("Laptop", result[0].nombre)
    }

    @Test
    fun writeAndReadSolicitud() = runBlocking {
        val solicitud = SolicitudEntity(
            id = 1,
            equipoId = 1,
            ambienteDestino = "Lab A",
            proposito = "Clase",
            duracionHoras = 4,
            estado = EstadoSolicitud.SOLICITADA
        )
        solicitudDao.insertSolicitud(solicitud)

        val result = solicitudDao.getAllSolicitudes().first()
        assertEquals(1, result.size)
        assertEquals("Lab A", result[0].ambienteDestino)
    }
}
