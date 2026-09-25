package com.example.prestamolab.ui.evidencias

import androidx.lifecycle.SavedStateHandle
import com.example.prestamolab.data.evidencias.AlmacenFotos
import com.example.prestamolab.data.evidencias.FotoReservada
import com.example.prestamolab.data.repository.EvidenciaRepository
import com.example.prestamolab.model.EtapaEvidencia
import android.graphics.Bitmap
import com.example.prestamolab.model.EstadoEvidencia
import com.example.prestamolab.model.Evidencia
import com.example.prestamolab.model.Ubicacion
import com.example.prestamolab.testutil.FakeLocationProvider
import com.example.prestamolab.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EvidenciasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Solo el préstamo 2 está PRESTADO, como en la semilla. */
    private class RepositorioFalso : EvidenciaRepository {
        val todas = MutableStateFlow(emptyList<Evidencia>())

        override fun evidencias(solicitudId: Int): Flow<List<Evidencia>> =
            todas.map { lista -> lista.filter { it.solicitudId == solicitudId } }

        override suspend fun registrar(solicitudId: Int, etapa: EtapaEvidencia, uriLocal: String): Result<Evidencia> {
            if (solicitudId != 2) return Result.failure(IllegalStateException("Solo se adjuntan evidencias a un préstamo entregado"))
            val evidencia = Evidencia(todas.value.size + 1, solicitudId, etapa, uriLocal, null, "2026-09-25 10:00")
            todas.update { it + evidencia }
            return Result.success(evidencia)
        }

        override suspend fun agregarUbicacion(evidenciaId: Int, ubicacion: Ubicacion) {
            todas.update { lista ->
                lista.map { if (it.id == evidenciaId) it.copy(latitud = ubicacion.latitud, longitud = ubicacion.longitud) else it }
            }
        }
    }

    private class AlmacenFalso : AlmacenFotos {
        var reservadas = 0
        val descartadas = mutableListOf<String>()

        override fun reservar() = FotoReservada("content://prueba/foto${++reservadas}.jpg", "/files/evidencias/foto$reservadas.jpg")
        override fun descartar(foto: FotoReservada) {
            descartadas += foto.ruta
        }
        override fun leer(uri: String): ByteArray? = null
        override fun miniatura(uri: String): Bitmap? = null
    }

    private val repository = RepositorioFalso()
    private val almacen = AlmacenFalso()
    private val estado = SavedStateHandle()

    private val gps = FakeLocationProvider()
    private val sincronizando = MutableStateFlow(false)

    private fun viewModel(solicitudId: Int = 2, permisoUbicacion: Boolean = false) =
        EvidenciasViewModel(
            repository, almacen, solicitudId, EtapaEvidencia.ENTREGA, estado, gps,
            tienePermisoUbicacion = { permisoUbicacion }, sincronizando = sincronizando
        )

    @Test
    fun TC_HU08_02_AlConfirmarLaFoto_SuUriQuedaAsociadaAlPrestamo() {
        val vm = viewModel()

        val uri = vm.prepararFoto()
        vm.onFotoTomada(true)

        val evidencia = vm.uiState.value.evidencias.single()
        assertEquals(uri, evidencia.uriLocal)
        assertEquals(2, evidencia.solicitudId)
        assertEquals(EtapaEvidencia.ENTREGA, evidencia.etapa)
        assertFalse(vm.uiState.value.esError)
        assertTrue(almacen.descartadas.isEmpty())
    }

    @Test
    fun TC_HU08_04_CancelarLaCamara_NoCreaEvidenciaYBorraElArchivo() {
        val vm = viewModel()

        vm.prepararFoto()
        vm.onFotoTomada(false)

        assertTrue(vm.uiState.value.evidencias.isEmpty())
        assertEquals(listOf("/files/evidencias/foto1.jpg"), almacen.descartadas)
    }

    @Test
    fun TC_HU08_03_PermisoNegado_MuestraUnMensajeExplicativo() {
        val vm = viewModel()

        vm.onPermisoCamaraDenegado()

        assertEquals(EvidenciasViewModel.MENSAJE_SIN_PERMISO, vm.uiState.value.mensaje)
        assertTrue(vm.uiState.value.esError)
        assertEquals(0, almacen.reservadas)
    }

    @Test
    fun LaFotoConfirmadaNoSePierdeSiAndroidCierraLaAppConLaCamaraAbierta() {
        viewModel().prepararFoto()

        // Proceso nuevo: otro ViewModel con el mismo estado guardado recibe el resultado de la cámara
        val recreado = viewModel()
        recreado.onFotoTomada(true)

        assertEquals("content://prueba/foto1.jpg", recreado.uiState.value.evidencias.single().uriLocal)
    }

    @Test
    fun SiElPrestamoNoAdmiteEvidencias_SeBorraLaFotoYSeInforma() {
        val vm = viewModel(solicitudId = 1)

        vm.prepararFoto()
        vm.onFotoTomada(true)

        assertTrue(vm.uiState.value.esError)
        assertEquals("Solo se adjuntan evidencias a un préstamo entregado", vm.uiState.value.mensaje)
        assertEquals(1, almacen.descartadas.size)
    }

    @Test
    fun SinAppDeCamara_SeInformaYNoQuedaArchivo() {
        val vm = viewModel()

        vm.prepararFoto()
        vm.onCamaraNoDisponible()
        vm.onFotoTomada(true)

        assertTrue(vm.uiState.value.evidencias.isEmpty())
        assertEquals(1, almacen.descartadas.size)
        assertTrue(vm.uiState.value.esError)
    }

    @Test
    fun ConPermisoDeUbicacion_LaEvidenciaGuardaLasCoordenadas() {
        val vm = viewModel(permisoUbicacion = true)

        vm.prepararFoto()
        vm.onFotoTomada(true)

        val evidencia = vm.uiState.value.evidencias.single()
        assertEquals(FakeLocationProvider.UBICACION_CTMA.latitud, evidencia.latitud!!, 0.0)
        assertEquals(FakeLocationProvider.UBICACION_CTMA.longitud, evidencia.longitud!!, 0.0)
    }

    @Test
    fun SinPermisoDeUbicacion_LaEvidenciaSeGuardaSinCoordenadasYNoSePideElGps() {
        val vm = viewModel(permisoUbicacion = false)

        vm.prepararFoto()
        vm.onFotoTomada(true)

        assertNull(vm.uiState.value.evidencias.single().latitud)
        assertEquals(0, gps.llamadas)
    }

    @Test
    fun SiElGpsFalla_LaEvidenciaIgualQuedaGuardada() {
        gps.resultado = Result.failure(Exception("GPS apagado"))
        val vm = viewModel(permisoUbicacion = true)

        vm.prepararFoto()
        vm.onFotoTomada(true)

        assertNull(vm.uiState.value.evidencias.single().latitud)
        assertFalse(vm.uiState.value.esError)
    }

    @Test
    fun MientrasSincronizaLaPantallaLoSabe_ParaMostrarSubiendo() {
        val vm = viewModel()
        vm.prepararFoto()
        vm.onFotoTomada(true)
        assertFalse(vm.uiState.value.sincronizando)
        assertEquals(EstadoEvidencia.LOCAL, vm.uiState.value.evidencias.single().estado)

        sincronizando.value = true

        assertTrue(vm.uiState.value.sincronizando)
    }
}
