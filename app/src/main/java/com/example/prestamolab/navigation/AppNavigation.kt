package com.example.prestamolab.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prestamolab.ui.catalogo.CatalogoScreen
import com.example.prestamolab.ui.equipo.EquipoDetalleScreen
import com.example.prestamolab.ui.misprestamos.MisSolicitudesScreen
import com.example.prestamolab.ui.misprestamos.SolicitudDetalleScreen
import com.example.prestamolab.ui.solicitud.SolicitudFormScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun AppNavigation(viewModel: PrestamoViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "catalogo"
    ) {
        composable("catalogo") {
            CatalogoScreen(
                equipos = uiState.equipos,
                onEquipoClick = { equipoId ->
                    navController.navigate("equipo_detalle/$equipoId")
                },
                onVerMisSolicitudesClick = {
                    navController.navigate("mis_solicitudes")
                }
            )
        }

        composable(
            route = "equipo_detalle/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: -1
            val equipo = uiState.equipos.find { it.id == equipoId }

            EquipoDetalleScreen(
                equipo = equipo,
                onSolicitarClick = { id ->
                    navController.navigate("solicitud_form/$id")
                },
                onVolverClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "solicitud_form/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: -1

            SolicitudFormScreen(
                equipoId = equipoId,
                guardando = uiState.guardando,
                mensajeError = uiState.mensaje,
                onGuardarClick = { id, ambiente, proposito, duracion ->
                    viewModel.limpiarMensaje()
                    viewModel.registrarSolicitud(id, ambiente, proposito, duracion) {
                        navController.navigate("mis_solicitudes") {
                            popUpTo("catalogo")
                        }
                    }
                },
                onVolverClick = {
                    viewModel.limpiarMensaje()
                    navController.popBackStack()
                }
            )
        }

        composable("mis_solicitudes") {
            MisSolicitudesScreen(
                solicitudes = uiState.solicitudes,
                onSolicitudClick = { solicitudId ->
                    navController.navigate("solicitud_detalle/$solicitudId")
                },
                onVolverClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "solicitud_detalle/{solicitudId}",
            arguments = listOf(navArgument("solicitudId") { type = NavType.IntType })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getInt("solicitudId") ?: -1
            val solicitud = uiState.solicitudes.find { it.id == solicitudId }

            SolicitudDetalleScreen(
                solicitud = solicitud,
                onCancelarClick = { id ->
                    viewModel.cancelarSolicitud(id)
                },
                onVolverClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}