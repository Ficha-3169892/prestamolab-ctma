package com.example.prestamolab.navigation

import android.R.attr.type
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
import com.example.prestamolab.ui.solicitud.SolicitarEquipoScreen
import com.example.prestamolab.ui.solicitud.SolicitudDetalleScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel

sealed class Pantalla(val ruta: String) {
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
}

@Composable
fun AppNavigation(viewModel: PrestamoViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Pantalla.Catalogo.ruta) {

        composable(Pantalla.Catalogo.ruta) {
            CatalogoScreen(
                equipos = uiState.equipos,
                onEquipoClick = { id -> navController.navigate(Pantalla.EquipoDetalle.crearRuta(id)) },
                onVerSolicitudesClick = { navController.navigate(Pantalla.MisSolicitudes.ruta) }
            )
        }

        composable(
            route = Pantalla.EquipoDetalle.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: -1
            viewModel.seleccionarEquipo(equipoId)

            EquipoDetalleScreen(
                equipo = uiState.equipoSeleccionado,
                onSolicitarClick = { id -> navController.navigate(Pantalla.Solicitar.crearRuta(id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Pantalla.Solicitar.ruta,
            arguments = listOf(navArgument("equipoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getInt("equipoId") ?: -1
            viewModel.seleccionarEquipo(equipoId)

            SolicitarEquipoScreen(
                equipo = uiState.equipoSeleccionado,
                guardando = uiState.guardando,
                mensajeError = uiState.mensajeError,
                onGuardar = { ambiente, proposito, duracion ->
                    viewModel.guardarSolicitud(equipoId, ambiente, proposito, duracion)
                    if (uiState.mensajeError == null) {
                        navController.navigate(Pantalla.MisSolicitudes.ruta) {
                            popUpTo(Pantalla.Catalogo.ruta)
                        }
                    }
                },
                onVolver = {
                    viewModel.limpiarMensaje()
                    navController.popBackStack()
                }
            )
        }

        composable(Pantalla.MisSolicitudes.ruta) {
            MisSolicitudesScreen(
                solicitudes = uiState.solicitudes,
                onSolicitudClick = { id -> navController.navigate(Pantalla.SolicitudDetalle.crearRuta(id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Pantalla.SolicitudDetalle.ruta,
            arguments = listOf(navArgument("solicitudId") { type = NavType.IntType })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getInt("solicitudId") ?: -1
            viewModel.seleccionarSolicitud(solicitudId)

            SolicitudDetalleScreen(
                solicitud = uiState.solicitudSeleccionada,
                mensajeError = uiState.mensajeError,
                onCancelar = { id -> viewModel.cancelarSolicitud(id) },
                onVolver = {
                    viewModel.limpiarMensaje()
                    navController.popBackStack()
                }
            )
        }
    }
}
