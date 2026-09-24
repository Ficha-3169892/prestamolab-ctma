package com.example.prestamolab.ui.navigation

import com.example.prestamolab.model.Rol
import org.junit.Assert.*
import org.junit.Test

class ControlAccesoTest {

    @Test
    fun `TC-HU10-06 - El estudiante no puede acceder a la ruta de gestion`() {
        assertFalse(ControlAcceso.puedeAcceder(Rutas.GESTION, Rol.ESTUDIANTE))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.PRESTAMOS, Rol.ESTUDIANTE))
    }

    @Test
    fun `TC-HU10-07 - El instructor accede a gestion y al catalogo`() {
        assertTrue(ControlAcceso.puedeAcceder(Rutas.GESTION, Rol.INSTRUCTOR))
        assertTrue(ControlAcceso.puedeAcceder(Rutas.PRESTAMOS, Rol.INSTRUCTOR))
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
