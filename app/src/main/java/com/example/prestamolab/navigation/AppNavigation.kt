package com.example.prestamolab.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prestamolab.ui.catalogo.CatalogoScreen
import com.example.prestamolab.ui.equipo.EquipoDetalleScreen
import com.example.prestamolab.ui.misprestamos.MisSolicitudesScreen
import com.example.prestamolab.ui.misprestamos.SolicitudDetalleScreen
import com.example.prestamolab.ui.solicitud.SolicitarScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel

sealed class AppRoute(
    val route: String
) {

    data object Catalogo : AppRoute("catalogo")

    data object EquipoDetalle : AppRoute(
        "equipo_detalle/{equipoId}"
    )

    data object Solicitar : AppRoute(
        "solicitar/{equipoId}"
    )

    data object MisSolicitudes : AppRoute(
        "mis_solicitudes"
    )

    data object SolicitudDetalle : AppRoute(
        "solicitud_detalle/{solicitudId}"
    )
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val viewModel: PrestamoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Catalogo.route
    ) {

        composable(
            route = AppRoute.Catalogo.route
        ) {

            CatalogoScreen(
                viewModel = viewModel,
                onEquipoClick = { equipoId ->

                    navController.navigate(
                        "equipo_detalle/$equipoId"
                    )
                },
                onMisSolicitudesClick = {

                    navController.navigate(
                        AppRoute.MisSolicitudes.route
                    )
                }
            )
        }

        composable(
            route = AppRoute.EquipoDetalle.route,
            arguments = listOf(
                navArgument("equipoId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val equipoId = backStackEntry
                .arguments
                ?.getInt("equipoId") ?: -1

            EquipoDetalleScreen(
                equipoId = equipoId,
                viewModel = viewModel,
                onSolicitarClick = { id ->

                    navController.navigate(
                        "solicitar/$id"
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoute.Solicitar.route,
            arguments = listOf(
                navArgument("equipoId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val equipoId = backStackEntry
                .arguments
                ?.getInt("equipoId") ?: -1

            SolicitarScreen(
                equipoId = equipoId,
                viewModel = viewModel,
                onSolicitudCreada = {

                    navController.navigate(
                        AppRoute.MisSolicitudes.route
                    ) {
                        popUpTo(
                            AppRoute.Catalogo.route
                        )
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoute.MisSolicitudes.route
        ) {

            MisSolicitudesScreen(
                viewModel = viewModel,
                onSolicitudClick = { solicitudId ->

                    navController.navigate(
                        "solicitud_detalle/$solicitudId"
                    )
                },
                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoute.SolicitudDetalle.route,
            arguments = listOf(
                navArgument("solicitudId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val solicitudId = backStackEntry
                .arguments
                ?.getInt("solicitudId") ?: -1

            SolicitudDetalleScreen(
                solicitudId = solicitudId,
                viewModel = viewModel,
                onBackClick = {

                    navController.popBackStack()
                }
            )
        }
    }
}
