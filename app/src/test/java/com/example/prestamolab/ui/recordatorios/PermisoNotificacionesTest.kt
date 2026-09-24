package com.example.prestamolab.ui.recordatorios

import org.junit.Assert.*
import org.junit.Test

class PermisoNotificacionesTest {

    private val android13 = 33

    @Test
    fun TC_HU09_04_EnAndroid13SinPermiso_SePideAlTenerUnPrestamoEntregado() {
        assertTrue(debePedirPermisoNotificaciones(android13, concedido = false, hayPrestamoEntregado = true, yaSePidio = false))
    }

    @Test
    fun NoSePideAlAbrirLaAppSinPrestamosEntregados() {
        assertFalse(debePedirPermisoNotificaciones(android13, concedido = false, hayPrestamoEntregado = false, yaSePidio = false))
    }

    @Test
    fun NoSeInsisteSiYaSePidioOSiYaEstaConcedido() {
        assertFalse(debePedirPermisoNotificaciones(android13, concedido = false, hayPrestamoEntregado = true, yaSePidio = true))
        assertFalse(debePedirPermisoNotificaciones(android13, concedido = true, hayPrestamoEntregado = true, yaSePidio = false))
    }

    @Test
    fun AntesDeAndroid13NoHacePermiso() {
        assertFalse(debePedirPermisoNotificaciones(32, concedido = false, hayPrestamoEntregado = true, yaSePidio = false))
    }
}
