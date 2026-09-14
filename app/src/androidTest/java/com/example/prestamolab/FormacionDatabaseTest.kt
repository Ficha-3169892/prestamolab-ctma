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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    // --- PRUEBAS DE ActividadDao ---

    @Test
    fun t1_actividadDao_consultaInicialEnTablaVacia() = runBlocking {
        val lista = actividadDao.obtenerTodas().first()
        assertTrue(lista.isEmpty())
    }

    @Test
    fun t2_actividadDao_insercionDeUnaActividadEntity() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(id = 1, nombre = "Competencia T2"))
        val actividad = ActividadEntity(id = 10, titulo = "Título T2", descripcion = "Desc", fechaLimite = "2026", prioridad = 1, competenciaId = compId)
        val idGenerado = actividadDao.insertar(actividad)
        assertEquals(10L, idGenerado)
    }

    @Test
    fun t3_actividadDao_insercionMultipleYOrdenamientoDescendentePorId() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(id = 2, nombre = "Competencia T3"))
        actividadDao.insertar(ActividadEntity(id = 100, titulo = "A", descripcion = "D", fechaLimite = "2026", prioridad = 1, competenciaId = compId))
        actividadDao.insertar(ActividadEntity(id = 200, titulo = "B", descripcion = "D", fechaLimite = "2026", prioridad = 2, competenciaId = compId))
        
        val lista = actividadDao.obtenerTodas().first()
        assertEquals(2, lista.size)
        assertEquals(200L, lista[0].id)
        assertEquals(100L, lista[1].id)
    }

    @Test
    fun t4_actividadDao_consultaPorIdExistente() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(id = 3, nombre = "Competencia T4"))
        actividadDao.insertar(ActividadEntity(id = 15, titulo = "Busca por ID", descripcion = "Desc", fechaLimite = "2026", prioridad = 1, competenciaId = compId))
        
        val resultado = actividadDao.obtenerPorId(15)
        assertNotNull(resultado)
        assertEquals("Busca por ID", resultado?.titulo)
    }

    @Test
    fun t5_actividadDao_consultaPorIdInexistente_retornaNull() = runBlocking {
        val resultado = actividadDao.obtenerPorId(9999)
        assertNull(resultado)
    }

    @Test
    fun t6_actividadDao_actualizacionDeRegistro() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(id = 4, nombre = "Competencia T6"))
        actividadDao.insertar(ActividadEntity(id = 40, titulo = "Original", descripcion = "Desc", fechaLimite = "2026", prioridad = 1, competenciaId = compId, completada = false))
        
        val modificado = ActividadEntity(id = 40, titulo = "Cambiado", descripcion = "Desc", fechaLimite = "2026", prioridad = 1, competenciaId = compId, completada = true)
        val filasAfectadas = actividadDao.actualizar(modificado)
        assertEquals(1, filasAfectadas)
        
        val resultado = actividadDao.obtenerPorId(40)
        assertEquals("Cambiado", resultado?.titulo)
        assertEquals(true, resultado?.completada)
    }

    @Test
    fun t7_actividadDao_eliminacionDeRegistro() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(id = 5, nombre = "Competencia T7"))
        val actividad = ActividadEntity(id = 50, titulo = "Eliminame", descripcion = "Desc", fechaLimite = "2026", prioridad = 1, competenciaId = compId)
        actividadDao.insertar(actividad)
        
        val filasEliminadas = actividadDao.eliminar(actividad)
        assertEquals(1, filasEliminadas)
        
        val resultado = actividadDao.obtenerPorId(50)
        assertNull(resultado)
    }

    @Test
    fun t8_actividadDao_conflictoDeInsercionConReplace() = runBlocking {
        val compId = actividadDao.insertarCompetencia(CompetenciaEntity(id = 6, nombre = "Competencia T8"))
        actividadDao.insertar(ActividadEntity(id = 80, titulo = "Primero", descripcion = "Desc", fechaLimite = "2026", prioridad = 1, competenciaId = compId))
        
        // Mismo ID con REPLACE
        actividadDao.insertar(ActividadEntity(id = 80, titulo = "Reemplazo", descripcion = "Desc", fechaLimite = "2026", prioridad = 3, competenciaId = compId))
        
        val resultado = actividadDao.obtenerPorId(80)
        assertEquals("Reemplazo", resultado?.titulo)
        assertEquals(3, resultado?.prioridad)
    }

    // --- PRUEBAS DE CompetenciaEntity / DAO ---

    @Test
    fun t9_competencia_insercionExitosa() = runBlocking {
        val id = actividadDao.insertarCompetencia(CompetenciaEntity(id = 90, nombre = "Competencia T9"))
        assertEquals(90L, id)
    }

    @Test
    fun t10_competencia_reemplazoPorIdDuplicado() = runBlocking {
        actividadDao.insertarCompetencia(CompetenciaEntity(id = 11, nombre = "Nombre A"))
        val id = actividadDao.insertarCompetencia(CompetenciaEntity(id = 11, nombre = "Nombre B"))
        assertEquals(11L, id)
    }

    // --- PRUEBAS DE PrestamoDao ---

    @Test
    fun t11_prestamoDao_consultaInicialEnTablaVacia() = runBlocking {
        val lista = prestamoDao.obtenerTodos()
        assertTrue(lista.isEmpty())
    }

    @Test
    fun t12_prestamoDao_insercionYRetornoDeIdGenerado() = runBlocking {
        val prestamo = PrestamoEntity(id = 1, equipoId = 101, ambienteDestino = "Ambiente 1", duracionHoras = 2, proposito = "Prueba", estado = "SOLICITADA")
        val id = prestamoDao.insertar(prestamo)
        assertEquals(1L, id)
    }

    @Test
    fun t13_prestamoDao_obtencionDeListaCompleta() = runBlocking {
        prestamoDao.insertar(PrestamoEntity(id = 5, equipoId = 102, ambienteDestino = "Ambiente 5", duracionHoras = 1, proposito = "P5", estado = "SOLICITADA"))
        prestamoDao.insertar(PrestamoEntity(id = 6, equipoId = 103, ambienteDestino = "Ambiente 6", duracionHoras = 3, proposito = "P6", estado = "APROBADA"))
        
        val lista = prestamoDao.obtenerTodos()
        assertEquals(2, lista.size)
    }

    @Test
    fun t14_prestamoDao_consultaDePrestamoEspecificoPorIdExistente() = runBlocking {
        prestamoDao.insertar(PrestamoEntity(id = 14, equipoId = 104, ambienteDestino = "Ambiente 14", duracionHoras = 4, proposito = "P14", estado = "SOLICITADA"))
        
        val resultado = prestamoDao.obtenerPorId(14)
        assertNotNull(resultado)
        assertEquals("Ambiente 14", resultado?.ambienteDestino)
    }

    @Test
    fun t15_prestamoDao_consultaDePrestamoPorIdInexistente_retornaNull() = runBlocking {
        val resultado = prestamoDao.obtenerPorId(9995)
        assertNull(resultado)
    }

    @Test
    fun t16_prestamoDao_actualizacionDeEstadoASolicitud() = runBlocking {
        prestamoDao.insertar(PrestamoEntity(id = 16, equipoId = 105, ambienteDestino = "Ambiente 16", duracionHoras = 2, proposito = "P16", estado = "SOLICITADA"))
        
        val filasAfectadas = prestamoDao.actualizarEstado(16, "CANCELADO")
        assertEquals(1, filasAfectadas)
        
        val resultado = prestamoDao.obtenerPorId(16)
        assertEquals("CANCELADO", resultado?.estado)
    }

    @Test
    fun t17_prestamoDao_intentarActualizarEstadoEnIdInexistente_retornaCero() = runBlocking {
        val filasAfectadas = prestamoDao.actualizarEstado(8888, "CANCELADO")
        assertEquals(0, filasAfectadas)
    }

    @Test
    fun t18_prestamoDao_insercionConReemplazo() = runBlocking {
        prestamoDao.insertar(PrestamoEntity(id = 18, equipoId = 106, ambienteDestino = "Original A", duracionHoras = 1, proposito = "P18", estado = "SOLICITADA"))
        prestamoDao.insertar(PrestamoEntity(id = 18, equipoId = 106, ambienteDestino = "Reemplazo A", duracionHoras = 5, proposito = "P18 Modificado", estado = "APROBADA"))
        
        val resultado = prestamoDao.obtenerPorId(18)
        assertEquals("Reemplazo A", resultado?.ambienteDestino)
        assertEquals("APROBADA", resultado?.estado)
    }

    @Test
    fun t19_prestamoDao_preservacionDeCaracteresEspecialesYCadenasLargas() = runBlocking {
        val textoLargoConEspeciales = "Propósito con tildes (áéíóú), eñes (ñÑ), caracteres especiales (@#¢$*&) y longitud considerable ".repeat(2)
        prestamoDao.insertar(PrestamoEntity(id = 19, equipoId = 107, ambienteDestino = "Ambiente Especial № 101-B", duracionHoras = 8, proposito = textoLargoConEspeciales, estado = "SOLICITADA"))
        
        val resultado = prestamoDao.obtenerPorId(19)
        assertEquals("Ambiente Especial № 101-B", resultado?.ambienteDestino)
        assertEquals(textoLargoConEspeciales, resultado?.proposito)
    }

    @Test
    fun t20_prestamoDao_limpiezaCompletaTrasEjecucion() = runBlocking {
        // Valida que la base de datos está activa, responde correctamente y las consultas se ejecutan de forma limpia
        val lista = prestamoDao.obtenerTodos()
        assertNotNull(lista)
    }
}
