package com.example.prestamolab.data.evidencias

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.local.DatosSemilla
import com.example.prestamolab.data.local.PrestamoLabDatabase
import com.example.prestamolab.data.local.entity.EstadoSincronizacion
import com.example.prestamolab.data.repository.RoomEvidenciaRepository
import com.example.prestamolab.model.EtapaEvidencia
import com.example.prestamolab.model.Ubicacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** CA-HU08-02: la foto se guarda mediante FileProvider y su URI queda asociada al préstamo en Room. */
@RunWith(AndroidJUnit4::class)
class EvidenciasDatosTest {

    private val contexto = ApplicationProvider.getApplicationContext<Context>()
    private val almacen = FileProviderAlmacenFotos(contexto)
    private lateinit var db: PrestamoLabDatabase
    private lateinit var repository: RoomEvidenciaRepository
    private var cambiosLocales = 0
    private val reservadas = mutableListOf<FotoReservada>()

    @Before
    fun setup() {
        db = PrestamoLabDatabase.construir(contexto, enMemoria = true)
        DatosSemilla.reiniciar(db)
        repository = RoomEvidenciaRepository(db, alCambiarLocalmente = { cambiosLocales++ })
    }

    @After
    fun cerrar() {
        reservadas.forEach(almacen::descartar)
        db.close()
    }

    private fun reservar() = almacen.reservar().also { reservadas += it }

    @Test
    fun ReservarDaUnaUriDelFileProviderEnLaCarpetaPrivada() {
        val foto = reservar()

        assertTrue(foto.uri.startsWith("content://${contexto.packageName}.fileprovider/evidencias/"))
        assertEquals(File(contexto.filesDir, "evidencias").absolutePath, File(foto.ruta).parent)
    }

    @Test
    fun LoQueEscribeLaCamaraSeLeePorLaUri_YDescartarLoBorra() {
        val foto = reservar()
        // Lo que haría la app de cámara: escribir la imagen en la URI recibida
        contexto.contentResolver.openOutputStream(android.net.Uri.parse(foto.uri))!!.use { it.write(byteArrayOf(1, 2, 3)) }

        assertArrayEquals(byteArrayOf(1, 2, 3), almacen.leer(foto.uri))

        almacen.descartar(foto)
        assertFalse(File(foto.ruta).exists())
        assertNull(almacen.leer(foto.uri))
    }

    @Test
    fun TC_HU08_02_LaUriQuedaAsociadaAlPrestamoEnRoom() = runTest {
        val foto = reservar()

        val evidencia = repository.registrar(2, EtapaEvidencia.ENTREGA, foto.uri).getOrThrow()

        val guardada = repository.evidencias(2).first().single()
        assertEquals(evidencia.id, guardada.id)
        assertEquals(foto.uri, guardada.uriLocal)
        assertNull(guardada.urlRemota)
        val entidad = db.evidenceDao().obtener(evidencia.id)!!
        assertEquals(2, entidad.loanId)
        assertEquals(EstadoSincronizacion.PENDIENTE, entidad.syncStatus)
        assertEquals(1, cambiosLocales)
    }

    @Test
    fun UnPrestamoNoEntregado_NoAdmiteEvidencias() = runTest {
        // Solicitud #1: SOLICITADA
        assertTrue(repository.registrar(1, EtapaEvidencia.ENTREGA, "content://x/1.jpg").isFailure)
        assertTrue(repository.evidencias(1).first().isEmpty())
        assertEquals(0, cambiosLocales)
    }

    @Test
    fun LaUbicacionSeAgregaALaEvidenciaYQuedaPendienteDeEnviar() = runTest {
        val evidencia = repository.registrar(2, EtapaEvidencia.DEVOLUCION, "content://x/1.jpg").getOrThrow()
        db.evidenceDao().marcarEnviada(evidencia.id, null, null, EstadoSincronizacion.SINCRONIZADO)

        repository.agregarUbicacion(evidencia.id, Ubicacion(6.2518, -75.5636, 12f))

        val guardada = repository.evidencias(2).first().single()
        assertEquals(6.2518, guardada.latitud!!, 0.0)
        assertEquals(-75.5636, guardada.longitud!!, 0.0)
        assertEquals(EstadoSincronizacion.PENDIENTE, db.evidenceDao().obtener(evidencia.id)!!.syncStatus)
    }
}
