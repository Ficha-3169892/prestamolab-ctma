package com.example.prestamolab

import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.repository.PrestamoRepository
import com.example.prestamolab.viewmodel.PrestamoViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakePrestamoRepository : PrestamoRepository {
    var equipos = mutableListOf<Equipo>()
    var solicitudes = mutableListOf<SolicitudPrestamo>()

    override fun obtenerEquipos(): List<Equipo> = equipos.toList()
    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }
    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes.toList()
    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        val equipo = obtenerEquipo(solicitud.equipoId) ?: return Result.failure(Exception("El equipo no existe"))
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(Exception("El equipo no está disponible"))
        }
        if (solicitudes.any { it.equipoId == solicitud.equipoId && it.estado == EstadoSolicitud.SOLICITADA }) {
            return Result.failure(Exception("Ya existe una solicitud para este equipo"))
        }
        solicitudes.add(solicitud)
        val index = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (index != -1) {
            equipos[index] = equipo.copy(estado = EstadoEquipo.RESERVADO)
        }
        return Result.success(Unit)
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val solicitud = obtenerSolicitud(id) ?: return Result.failure(Exception("La solicitud no existe"))
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(Exception("Solo se pueden cancelar solicitudes SOLICITADA"))
        }
        val idxS = solicitudes.indexOfFirst { it.id == id }
        if (idxS != -1) {
            solicitudes[idxS] = solicitud.copy(estado = EstadoSolicitud.CANCELADA)
        }
        val equipo = obtenerEquipo(solicitud.equipoId)
        if (equipo != null) {
            val idxE = equipos.indexOfFirst { it.id == solicitud.equipoId }
            if (idxE != -1) {
                equipos[idxE] = equipo.copy(estado = EstadoEquipo.DISPONIBLE)
            }
        }
        return Result.success(Unit)
    }
}

class PrestamoViewModelTest {

    private lateinit var repository: FakePrestamoRepository
    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setUp() {
        repository = FakePrestamoRepository()
        repository.equipos = mutableListOf(
            Equipo(1, "Kit de electrónica", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
            Equipo(2, "Multímetro digital", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
            Equipo(3, "Cámara digital", CategoriaEquipo.OTRO, EstadoEquipo.RESERVADO),
            Equipo(4, "Taladro percutor", CategoriaEquipo.HERRAMIENTA, EstadoEquipo.PRESTADO)
        )
        viewModel = PrestamoViewModel(repository)
    }

    @Test
    fun tu01_cargarDatos_inicializaEquiposYSolicitudesCorrectamente() {
        val state = viewModel.uiState.value
        assertEquals(4, state.equipos.size)
        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun tu02_obtenerEquipo_conIdExistente_retornaEquipoValido() {
        val equipo = repository.obtenerEquipo(1)
        assertNotNull(equipo)
        assertEquals("Kit de electrónica", equipo?.nombre)
    }

    @Test
    fun tu03_obtenerEquipo_conIdInexistente_retornaNull() {
        val equipo = repository.obtenerEquipo(99)
        assertNull(equipo)
    }

    @Test
    fun tu04_crearSolicitud_conDatosValidos_retornaTrueYActualizaEstado() {
        val exito = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = "Práctica de soldadura de circuitos de control",
            duracionHoras = 4
        )
        assertTrue(exito)
        assertEquals("Solicitud creada correctamente", viewModel.uiState.value.mensaje)
        assertEquals(1, viewModel.uiState.value.solicitudes.size)
    }

    @Test
    fun tu05_crearSolicitud_conAmbienteVacio_retornaFalseYMuestraMensaje() {
        val exito = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "",
            proposito = "Práctica de soldadura de circuitos de control",
            duracionHoras = 4
        )
        assertFalse(exito)
        assertEquals("Debes ingresar el ambiente de destino", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu06_crearSolicitud_conPropositoCorto_retornaFalseYMuestraMensaje() {
        val exito = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = "Corto",
            duracionHoras = 4
        )
        assertFalse(exito)
        assertEquals("El propósito debe tener entre 10 y 180 caracteres", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu07_crearSolicitud_conPropositoExcesivo_retornaFalseYMuestraMensaje() {
        val propositoLargo = "A".repeat(181)
        val exito = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = propositoLargo,
            duracionHoras = 4
        )
        assertFalse(exito)
        assertEquals("El propósito debe tener entre 10 y 180 caracteres", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu08_crearSolicitud_conDuracionMenorAlLimite_retornaFalseYMuestraMensaje() {
        val exito = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = "Práctica de soldadura de circuitos de control",
            duracionHoras = 0
        )
        assertFalse(exito)
        assertEquals("La duración debe estar entre 1 y 8 horas", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu09_crearSolicitud_conDuracionMayorAlLimite_retornaFalseYMuestraMensaje() {
        val exito = viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = "Práctica de soldadura de circuitos de control",
            duracionHoras = 9
        )
        assertFalse(exito)
        assertEquals("La duración debe estar entre 1 y 8 horas", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu10_crearSolicitud_sobreEquipoReservado_retornaFalseYFalla() {
        val exito = viewModel.crearSolicitud(
            equipoId = 3,
            ambienteDestino = "Ambiente 101",
            proposito = "Grabación de evidencia fotográfica del laboratorio",
            duracionHoras = 2
        )
        assertFalse(exito)
        assertEquals("El equipo no está disponible", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu11_crearSolicitud_sobreEquipoPrestado_retornaFalseYFalla() {
        val exito = viewModel.crearSolicitud(
            equipoId = 4,
            ambienteDestino = "Ambiente 105",
            proposito = "Prácticas de perforación en muros",
            duracionHoras = 3
        )
        assertFalse(exito)
        assertEquals("El equipo no está disponible", viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu12_crearSolicitud_exito_cambiaEstadoEquipoAReservado() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = "Práctica de soldadura de circuitos de control",
            duracionHoras = 4
        )
        val equipo = viewModel.uiState.value.equipos.find { it.id == 1 }
        assertEquals(EstadoEquipo.RESERVADO, equipo?.estado)
    }

    @Test
    fun tu13_limpiarMensaje_poneMensajeEnNull() {
        viewModel.crearSolicitud(1, "", "Propósito válido", 4)
        assertNotNull(viewModel.uiState.value.mensaje)
        viewModel.limpiarMensaje()
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun tu14_cancelarSolicitud_conIdValido_cambiaEstadoACanceladaYLiberaEquipo() {
        viewModel.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Ambiente 302",
            proposito = "Práctica de soldadura de circuitos de control",
            duracionHoras = 4
        )
        val idSolicitud = viewModel.uiState.value.solicitudes.first().id
        viewModel.cancelarSolicitud(idSolicitud)

        assertEquals("Solicitud cancelada correctamente", viewModel.uiState.value.mensaje)
        assertEquals(EstadoSolicitud.CANCELADA, viewModel.uiState.value.solicitudes.first().estado)
        assertEquals(EstadoEquipo.DISPONIBLE, viewModel.uiState.value.equipos.find { it.id == 1 }?.estado)
    }

    @Test
    fun tu15_cancelarSolicitud_Inexistente_muestraMensajeError() {
        viewModel.cancelarSolicitud(999)
        assertEquals("La solicitud no existe", viewModel.uiState.value.mensaje)
    }
}
