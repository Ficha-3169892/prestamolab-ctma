package com.example.prestamolab.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prestamolab.ui.catalogo.CatalogoScreen
import com.example.prestamolab.ui.detalle.DetalleEquipoScreen
import com.example.prestamolab.ui.prestamos.MisPrestamosScreen
import com.example.prestamolab.ui.solicitud.SolicitarPrestamoScreen
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun AppNavigation(
    viewModel: PrestamoViewModel = viewModel()
) {
    val navController = rememberNavController()

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 45.dp,
                    bottom = 16.dp
                )
        ) {

            Button(
                onClick = {
                    navController.navigate("catalogo") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Catálogo")
            }

            Button(
                onClick = {
                    navController.navigate("mis_prestamos") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Mis préstamos")
            }
        }

        NavHost(
            navController = navController,
            startDestination = "catalogo"
        ) {

            // CATÁLOGO
            composable("catalogo") {
                CatalogoScreen(
                    viewModel = viewModel,
                    onEquipoClick = { equipoId ->
                        navController.navigate("detalle/$equipoId")
                    }
                )
            }

            // DETALLE DEL EQUIPO
            composable("detalle/{equipoId}") { backStackEntry ->

                val equipoId = backStackEntry
                    .arguments
                    ?.getString("equipoId")
                    ?.toIntOrNull()

                if (equipoId != null) {
                    DetalleEquipoScreen(
                        equipoId = equipoId,
                        viewModel = viewModel,
                        onVolver = {
                            navController.popBackStack()
                        },
                        onSolicitar = {
                            navController.navigate("solicitar/$equipoId")
                        }
                    )
                }
            }

            // CREAR SOLICITUD / PRÉSTAMO
            composable("solicitar/{equipoId}") { backStackEntry ->

                val equipoId = backStackEntry
                    .arguments
                    ?.getString("equipoId")
                    ?.toIntOrNull()

                if (equipoId != null) {
                    SolicitarPrestamoScreen(
                        equipoId = equipoId,
                        viewModel = viewModel,
                        onVolver = {
                            navController.popBackStack()
                        },
                        onSolicitudCreada = {
                            navController.navigate("mis_prestamos") {
                                popUpTo("catalogo")
                            }
                        }
                    )
                }
            }

            // MIS PRÉSTAMOS
            composable("mis_prestamos") {
                MisPrestamosScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}