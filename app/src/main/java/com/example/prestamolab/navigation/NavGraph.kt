package com.example.prestamolab.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prestamolab.ui.admin.AdminDashboardScreen
import com.example.prestamolab.ui.admin.FormularioEquipoScreen
import com.example.prestamolab.ui.auth.LoginScreen
import com.example.prestamolab.ui.catalogo.CatalogoScreen
import com.example.prestamolab.ui.equipo.EquipoDetalleScreen
import com.example.prestamolab.ui.evidencia.CapturaEvidenciaScreen
import com.example.prestamolab.ui.misprestamos.MisSolicitudesScreen
import com.example.prestamolab.ui.solicitud.SolicitarEquipoScreen
import com.example.prestamolab.ui.solicitud.SolicitudDetalleScreen
import com.example.prestamolab.viewmodel.AuthViewModel
import com.example.prestamolab.viewmodel.PrestamoViewModel

sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object Catalogo : Pantalla("catalogo")
    object EquipoDetalle : Pantalla("equipo_detalle/{equipoId}") {
        fun crearRuta(equipoId: Int) = "equipo_detalle/$equipoId"
    }
    object Solicitar : Pantalla("solicitar/{equipoId}") {
        fun crearRuta(equipoId: Int) = "solicitar/$equipoId"
    }
    object MisSolicitudes : Pantalla("mis_solicitudes")
    object SolicitudDetalle : Pantalla("solicitud_detalle/{solicitudId}") {
        fun crearRuta(solicitudId: Int) = "solicitud_detalle/$solicitudId"
    }
    object AdminDashboard : Pantalla("admin_dashboard")
    object FormularioEquipo : Pantalla("formulario_equipo?equipoId={equipoId}") {
        fun crearRuta(equipoId: Int? = null) = if (equipoId != null) "formulario_equipo?equipoId=$equipoId" else "formulario_equipo"
    }
    object CapturaEvidencia : Pantalla("captura_evidencia/{solicitudId}") {
        fun crearRuta(solicitudId: Int) = "captura_evidencia/$solicitudId"
    }
}

@Composable
fun AppNavigation(
    viewModel: PrestamoViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    val startRoute = when {
        authUiState.userEmail.isNull_or_empty() && authUiState.userId.isNull_or_empty() -> Pantalla.Login.ruta
        authUiState.userRole == "admin" -> Pantalla.AdminDashboard.ruta
        else -> Pantalla.Catalogo.ruta
    }

    NavHost(
        navController = navController,
        startDestination = startRoute,
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
        popExitTransition = { fadeOut(animationSpec = tween(150)) }
    ) {

        composable(Pantalla.Login.ruta) {
            LoginScreen(
                uiState = authUiState,
                onLoginClick = { email, pass ->
                    authViewModel.login(email, pass) { role ->
                        viewModel.sincronizarEquipos() // Sincronización automática al iniciar sesión
                        val destino = if (role == "admin") Pantalla.AdminDashboard.ruta else Pantalla.Catalogo.ruta
                        navController.navigate(destino) {
                            popUpTo(Pantalla.Login.ruta) { inclusive = true }
                        }
                    }
                },
                onRegisterClick = { email, pass ->
                    authViewModel.register(email, pass) {
                        authViewModel.login(email, pass) { role ->
                            viewModel.sincronizarEquipos() // Sincronización automática tras registrarse e iniciar sesión
                            val destino = if (role == "admin") Pantalla.AdminDashboard.ruta else Pantalla.Catalogo.ruta
                            navController.navigate(destino) {
                                popUpTo(Pantalla.Login.ruta) { inclusive = true }
                            }
                        }
                    }
                },
                onLimpiarError = { authViewModel.limpiarMensaje() }
            )
        }

        composable(Pantalla.Catalogo.ruta) {
            CatalogoScreen(
                equipos = uiState.equipos,
                mensajeExito = uiState.mensajeExito,
                mensajeError = uiState.mensajeError,
                onEquipoClick = { id -> navController.navigate(Pantalla.EquipoDetalle.crearRuta(id)) },
                onVerSolicitudesClick = { navController.navigate(Pantalla.MisSolicitudes.ruta) },
                onSincronizarClick = { viewModel.sincronizarEquipos() },
                onLimpiarMensaje = { viewModel.limpiarMensaje() },
                onLogoutClick = {
                    authViewModel.logout {
                        navController.navigate(Pantalla.Login.ruta) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Pantalla.EquipoDetalle.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: -1
            val equipo = uiState.equipos.find { it.id == equipoId }

            EquipoDetalleScreen(
                equipo = equipo,
                onSolicitarClick = { id -> navController.navigate(Pantalla.Solicitar.crearRuta(id)) },
                onVolver = {
                    if (navController.previousBackStackEntry != null) navController.popBackStack()
                }
            )
        }

        composable(
            route = Pantalla.Solicitar.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: -1
            val equipo = uiState.equipos.find { it.id == equipoId }

            SolicitarEquipoScreen(
                equipo = equipo,
                guardando = uiState.guardando,
                mensajeError = uiState.mensajeError,
                onGuardar = { ambiente, proposito, duracion ->
                    viewModel.guardarSolicitud(equipoId, ambiente, proposito, duracion) {
                        navController.navigate(Pantalla.MisSolicitudes.ruta) {
                            popUpTo(Pantalla.Catalogo.ruta)
                        }
                    }
                },
                onVolver = {
                    viewModel.limpiarMensaje()
                    if (navController.previousBackStackEntry != null) navController.popBackStack()
                }
            )
        }

        composable(Pantalla.MisSolicitudes.ruta) {
            MisSolicitudesScreen(
                solicitudes = uiState.solicitudes,
                onSolicitudClick = { id -> navController.navigate(Pantalla.SolicitudDetalle.crearRuta(id)) },
                onVolver = {
                    if (navController.previousBackStackEntry != null) navController.popBackStack()
                }
            )
        }

        composable(
            route = Pantalla.SolicitudDetalle.ruta,
            arguments = listOf(navArgument("solicitudId") { type = NavType.IntType })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getInt("solicitudId") ?: -1
            val solicitud = uiState.solicitudes.find { it.id == solicitudId }

            SolicitudDetalleScreen(
                solicitud = solicitud,
                mensajeError = uiState.mensajeError,
                onCancelar = { id -> viewModel.cancelarSolicitud(id) },
                onAdjuntarEvidencia = { id -> navController.navigate(Pantalla.CapturaEvidencia.crearRuta(id)) },
                onVolver = {
                    viewModel.limpiarMensaje()
                    if (navController.previousBackStackEntry != null) navController.popBackStack()
                }
            )
        }

        composable(Pantalla.AdminDashboard.ruta) {
            AdminDashboardScreen(
                equipos = uiState.equipos,
                onAgregarEquipoClick = {
                    navController.navigate(Pantalla.FormularioEquipo.crearRuta())
                },
                onEditarEquipoClick = { id ->
                    navController.navigate(Pantalla.FormularioEquipo.crearRuta(id))
                },
                onEliminarEquipoClick = { id ->
                    viewModel.eliminarEquipo(id, authUiState.userRole ?: "usuario") {}
                },
                onSincronizarClick = { viewModel.sincronizarEquipos() },
                onLogoutClick = {
                    authViewModel.logout {
                        navController.navigate(Pantalla.Login.ruta) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Pantalla.FormularioEquipo.ruta,
            arguments = listOf(navArgument("equipoId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getString("equipoId")
            val equipoId = solicitudId?.toIntOrNull()
            val equipoExistente = if (equipoId != null) uiState.equipos.find { it.id == equipoId } else null

            FormularioEquipoScreen(
                equipoExistente = equipoExistente,
                mensajeError = uiState.mensajeError,
                onGuardar = { nombre, cat, est ->
                    val rol = authUiState.userRole ?: "usuario"
                    if (equipoExistente == null) {
                        viewModel.agregarEquipo(nombre, cat, est, rol) {
                            navController.popBackStack()
                        }
                    } else {
                        viewModel.editarEquipo(equipoExistente.id, nombre, cat, est, rol) {
                            navController.popBackStack()
                        }
                    }
                },
                onVolver = {
                    viewModel.limpiarMensaje()
                    if (navController.previousBackStackEntry != null) navController.popBackStack()
                }
            )
        }

        composable(
            route = Pantalla.CapturaEvidencia.ruta,
            arguments = listOf(navArgument("solicitudId") { type = NavType.IntType })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getInt("solicitudId") ?: -1

            CapturaEvidenciaScreen(
                solicitudId = solicitudId,
                guardando = uiState.guardando,
                mensajeError = uiState.mensajeError,
                onEvidenciaCapturada = { bytes, ext ->
                    viewModel.adjuntarEvidencia(solicitudId, bytes, ext) {
                        navController.popBackStack()
                    }
                },
                onVolver = {
                    viewModel.limpiarMensaje()
                    if (navController.previousBackStackEntry != null) navController.popBackStack()
                }
            )
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
