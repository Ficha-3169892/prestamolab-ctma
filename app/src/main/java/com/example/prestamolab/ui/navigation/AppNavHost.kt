package com.example.prestamolab.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.ui.PrestamoScreen
import com.example.prestamolab.ui.PrestamoViewModel
import com.example.prestamolab.ui.auth.LoginScreen
import com.example.prestamolab.ui.auth.LoginViewModel
import com.example.prestamolab.ui.devolucion.DevolucionRoute
import com.example.prestamolab.ui.devolucion.DevolucionViewModel
import com.example.prestamolab.ui.gestion.GestionScreen
import com.example.prestamolab.ui.sesion.EstadoSesion
import com.example.prestamolab.ui.sesion.SesionViewModel

/** Raíz de la app: login mientras no haya sesión; área autenticada según el rol. */
@Composable
fun AppNavHost() {
    val sesionViewModel: SesionViewModel = viewModel(factory = SesionViewModel.Factory)
    val estado by sesionViewModel.estado.collectAsState()

    when (val sesion = estado) {
        EstadoSesion.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        EstadoSesion.SinSesion -> {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
            val uiState by loginViewModel.uiState.collectAsState()
            LoginScreen(
                uiState = uiState,
                onIdentificadorChange = loginViewModel::onIdentificadorChanged,
                onContrasenaChange = loginViewModel::onContrasenaChanged,
                onIngresarClick = loginViewModel::iniciarSesion
            )
        }

        // La clave descarta la navegación del usuario anterior al cambiar de cuenta
        is EstadoSesion.Autenticado -> key(sesion.usuario.id) {
            AreaAutenticada(usuario = sesion.usuario, onCerrarSesion = sesionViewModel::cerrarSesion)
        }
    }
}

@Composable
private fun AreaAutenticada(usuario: Usuario, onCerrarSesion: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.PRESTAMOS) {
        composable(Rutas.PRESTAMOS) {
            RutaProtegida(Rutas.PRESTAMOS, usuario, onVolver = onCerrarSesion) {
                val viewModel: PrestamoViewModel = viewModel(
                    key = "prestamos-${usuario.id}",
                    factory = PrestamoViewModel.factory(usuario)
                )
                PrestamoScreen(
                    viewModel = viewModel,
                    puedeSolicitar = ControlAcceso.puedeSolicitarPrestamo(usuario.rol),
                    onGestionClick = if (ControlAcceso.puedeAcceder(Rutas.GESTION, usuario.rol)) {
                        { navController.navigate(Rutas.GESTION) }
                    } else null,
                    onRegistrarDevolucion = { id -> navController.navigate(Rutas.devolucion(id)) },
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
        composable(
            route = Rutas.DEVOLUCION,
            arguments = listOf(navArgument(Rutas.ARG_SOLICITUD_ID) { type = NavType.IntType })
        ) { entrada ->
            val solicitudId = entrada.arguments?.getInt(Rutas.ARG_SOLICITUD_ID) ?: -1
            RutaProtegida(Rutas.DEVOLUCION, usuario, onVolver = { navController.popBackStack() }) {
                val viewModel: DevolucionViewModel = viewModel(factory = DevolucionViewModel.factory(solicitudId))
                DevolucionRoute(viewModel = viewModel, onVolver = { navController.popBackStack() })
            }
        }
        composable(Rutas.GESTION) {
            RutaProtegida(Rutas.GESTION, usuario, onVolver = { navController.popBackStack() }) {
                GestionScreen(onVolver = { navController.popBackStack() })
            }
        }
    }
}

/** Bloquea el contenido si el rol no tiene permiso, aunque se llegue a la ruta directamente. */
@Composable
private fun RutaProtegida(
    ruta: String,
    usuario: Usuario,
    onVolver: () -> Unit,
    contenido: @Composable () -> Unit
) {
    if (ControlAcceso.puedeAcceder(ruta, usuario.rol)) {
        contenido()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Acceso denegado", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Tu rol (${usuario.rol.name}) no tiene permiso para esta sección.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onVolver) { Text("Volver") }
        }
    }
}
