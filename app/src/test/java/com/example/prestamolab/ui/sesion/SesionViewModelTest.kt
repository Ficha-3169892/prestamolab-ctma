package com.example.prestamolab.ui.sesion

import com.example.prestamolab.data.auth.DemoAuthRepository
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.FakeSessionStore
import com.example.prestamolab.testutil.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SesionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val usuario = Usuario("u-1", "Aprendiz", "a@ctma.edu.co", Rol.ESTUDIANTE)

    @Test
    fun `Sin sesion guardada el estado es SinSesion`() {
        val vm = SesionViewModel(DemoAuthRepository(FakeSessionStore()))

        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }

    @Test
    fun `Con sesion guardada el estado es Autenticado con el usuario`() {
        val vm = SesionViewModel(DemoAuthRepository(FakeSessionStore(Sesion(usuario, "t"))))

        assertEquals(EstadoSesion.Autenticado(usuario), vm.estado.value)
    }

    @Test
    fun `TC-HU10-05 - Cerrar sesion borra token y rol y vuelve a SinSesion`() {
        val store = FakeSessionStore(Sesion(usuario, "t"))
        val vm = SesionViewModel(DemoAuthRepository(store))

        vm.cerrarSesion()

        assertNull(store.sesion.value)
        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }
}
