package com.example.prestamolab.ui.navigation

import com.example.prestamolab.model.Rol
import org.junit.Assert.*
import org.junit.Test

class ControlAccesoTest {

    @Test
    fun `TC-HU10-06 - El estudiante no puede acceder a la ruta de gestion`() {
        assertFalse(ControlAcceso.puedeAcceder(Rutas.GESTION, Rol.ESTUDIANTE))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.CATALOGO, Rol.ESTUDIANTE))
        assertEquals(
            listOf(Rutas.CATALOGO, Rutas.MIS_SOLICITUDES, Rutas.ACTIVIDADES),
            ControlAcceso.destinosPrincipales(Rol.ESTUDIANTE)
        )
    }

    @Test
    fun `TC-HU10-07 - El instructor accede a gestion y al catalogo`() {
        assertTrue(ControlAcceso.puedeAcceder(Rutas.GESTION, Rol.INSTRUCTOR))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.CATALOGO, Rol.INSTRUCTOR))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.DETALLE_EQUIPO, Rol.INSTRUCTOR))
        assertEquals(listOf(Rutas.CATALOGO, Rutas.ACTIVIDADES, Rutas.GESTION), ControlAcceso.destinosPrincipales(Rol.INSTRUCTOR))
    }

    @Test
    fun `TC-HU14-01 - Solo el instructor revisa solicitudes`() {
        assertTrue(ControlAcceso.puedeAcceder(Rutas.REVISAR_SOLICITUDES, Rol.INSTRUCTOR))
        assertFalse(ControlAcceso.puedeAcceder(Rutas.REVISAR_SOLICITUDES, Rol.ESTUDIANTE))
    }

    @Test
    fun `TC-HU12-05 - Solo el instructor entra al inventario`() {
        listOf(Rutas.INVENTARIO, Rutas.NUEVO_EQUIPO, Rutas.EDITAR_EQUIPO).forEach {
            assertTrue(it, ControlAcceso.puedeAcceder(it, Rol.INSTRUCTOR))
            assertFalse(it, ControlAcceso.puedeAcceder(it, Rol.ESTUDIANTE))
        }
        assertEquals("gestion/inventario/7/editar", Rutas.editarEquipo(7))
    }

    @Test
    fun `TC-HU11-05 - Ambos roles consultan actividades y solo el instructor las edita`() {
        assertTrue(ControlAcceso.puedeAcceder(Rutas.ACTIVIDADES, Rol.ESTUDIANTE))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.ACTIVIDADES, Rol.INSTRUCTOR))
        listOf(Rutas.NUEVA_ACTIVIDAD, Rutas.EDITAR_ACTIVIDAD).forEach {
            assertTrue(it, ControlAcceso.puedeAcceder(it, Rol.INSTRUCTOR))
            assertFalse(it, ControlAcceso.puedeAcceder(it, Rol.ESTUDIANTE))
        }
        assertEquals("actividades/4/editar", Rutas.editarActividad(4))
    }

    @Test
    fun `HU-08 - Solo el estudiante adjunta evidencias`() {
        assertTrue(ControlAcceso.puedeAcceder(Rutas.EVIDENCIAS, Rol.ESTUDIANTE))
        assertFalse(ControlAcceso.puedeAcceder(Rutas.EVIDENCIAS, Rol.INSTRUCTOR))
        assertEquals("prestamo/2/evidencias/DEVOLUCION", Rutas.evidencias(2, com.example.prestamolab.model.EtapaEvidencia.DEVOLUCION))
    }

    @Test
    fun `Cada rol inicia en su pantalla principal`() {
        assertEquals(Rutas.GESTION, ControlAcceso.rutaInicio(Rol.INSTRUCTOR))
        assertEquals(Rutas.CATALOGO, ControlAcceso.rutaInicio(Rol.ESTUDIANTE))
    }

    @Test
    fun `El instructor no entra por ruta directa a solicitar, a Mis Solicitudes ni a devolver`() {
        listOf(Rutas.SOLICITUD, Rutas.MIS_SOLICITUDES, Rutas.DEVOLUCION).forEach {
            assertFalse(it, ControlAcceso.puedeAcceder(it, Rol.INSTRUCTOR))
            assertTrue(it, ControlAcceso.puedeAcceder(it, Rol.ESTUDIANTE))
        }
    }

    @Test
    fun `Las rutas con argumentos coinciden con sus patrones`() {
        assertEquals("equipo/7", Rutas.detalleEquipo(7))
        assertEquals("equipo/7/solicitud", Rutas.solicitud(7))
        assertEquals("devolucion/2", Rutas.devolucion(2))
        assertEquals("equipo/{equipoId}", Rutas.DETALLE_EQUIPO)
    }

    @Test
    fun `Una ruta no registrada se niega a todos los roles`() {
        Rol.entries.forEach { assertFalse(ControlAcceso.puedeAcceder("ruta-desconocida", it)) }
    }

    @Test
    fun `Solo el estudiante puede solicitar prestamos`() {
        assertTrue(ControlAcceso.puedeSolicitarPrestamo(Rol.ESTUDIANTE))
        assertFalse(ControlAcceso.puedeSolicitarPrestamo(Rol.INSTRUCTOR))
    }
}
