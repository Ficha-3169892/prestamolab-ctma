package com.example.prestamolab.ui.sesion

import com.example.prestamolab.data.auth.UsuariosAuthRepository
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.FakeSessionStore
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import com.example.prestamolab.testutil.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SesionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val usuario = Usuario("u-1", "Aprendiz", "a@ctma.edu.co", Rol.ESTUDIANTE)

    private fun authCon(store: FakeSessionStore) = UsuariosAuthRepository(FakeUsuariosDataSource(), store)

    @Test
    fun `Sin sesion guardada el estado es SinSesion`() {
        val vm = SesionViewModel(authCon(FakeSessionStore()))

        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }

    @Test
    fun `Con sesion guardada el estado es Autenticado con el usuario`() {
        val vm = SesionViewModel(authCon(FakeSessionStore(Sesion(usuario))))

        assertEquals(EstadoSesion.Autenticado(usuario), vm.estado.value)
    }

    @Test
    fun `TC-HU10-05 - Cerrar sesion borra el usuario y el rol y vuelve a SinSesion`() {
        val store = FakeSessionStore(Sesion(usuario))
        val vm = SesionViewModel(authCon(store))

        vm.cerrarSesion()

        assertNull(store.sesion.value)
        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }
}
