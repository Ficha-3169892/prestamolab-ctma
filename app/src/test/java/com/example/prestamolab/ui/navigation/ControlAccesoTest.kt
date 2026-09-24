package com.example.prestamolab.ui.navigation

import com.example.prestamolab.model.Rol
import org.junit.Assert.*
import org.junit.Test

class ControlAccesoTest {

    @Test
    fun `TC-HU10-06 - El estudiante no puede acceder a la ruta de gestion`() {
        assertFalse(ControlAcceso.puedeAcceder(Rutas.GESTION, Rol.ESTUDIANTE))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.CATALOGO, Rol.ESTUDIANTE))
        assertEquals(listOf(Rutas.CATALOGO, Rutas.MIS_SOLICITUDES), ControlAcceso.destinosPrincipales(Rol.ESTUDIANTE))
    }

    @Test
    fun `TC-HU10-07 - El instructor accede a gestion y al catalogo`() {
        assertTrue(ControlAcceso.puedeAcceder(Rutas.GESTION, Rol.INSTRUCTOR))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.CATALOGO, Rol.INSTRUCTOR))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.DETALLE_EQUIPO, Rol.INSTRUCTOR))
        assertEquals(listOf(Rutas.CATALOGO, Rutas.GESTION), ControlAcceso.destinosPrincipales(Rol.INSTRUCTOR))
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
