package com.example.prestamolab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolab.ui.catalogo.CatalogScreen
import com.example.prestamolab.ui.solicitud.SolicitudScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamoScreen(
    viewModel: PrestamoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Paleta de azul
    val blueHeader = Color(0xFF1E6091)
    val blueContainer = Color(0xFFD9EDF8)
    val blueDarkText = Color(0xFF0F2537)
    val blueCardBg = Color(0xFFF0F7FA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PréstamoLab CTMA",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = blueHeader,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE8F1F5)) {
                NavigationBarItem(
                    selected = uiState.seccionActual == SeccionApp.CATALOGO || uiState.seccionActual == SeccionApp.DETALLE_EQUIPO,
                    onClick = { viewModel.navegarA(SeccionApp.CATALOGO) },
                    label = { Text("Catálogo", fontFamily = FontFamily.SansSerif) },
                    icon = { Text("📦") }
                )
                NavigationBarItem(
                    selected = uiState.seccionActual == SeccionApp.MIS_SOLICITUDES,
                    onClick = { viewModel.navegarA(SeccionApp.MIS_SOLICITUDES) },
                    label = { Text("Mis Solicitudes", fontFamily = FontFamily.SansSerif) },
                    icon = { Text("📋") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.seccionActual) {
                SeccionApp.CATALOGO -> CatalogScreen(
                    equipos = uiState.equipos,
                    onEquipoClick = { equipoId ->
                        val equipoEncontrado = uiState.equipos.find { it.id == equipoId }
                        equipoEncontrado?.let { equipo ->
                            viewModel.seleccionarEquipoParaDetalle(equipo)
                        }
                    },
                    onNavigateToSolicitudes = {
                        viewModel.navegarA(SeccionApp.MIS_SOLICITUDES)
                    }
                )

                SeccionApp.DETALLE_EQUIPO -> {
                    val equipo = uiState.equipoSeleccionado
                    if (equipo != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { viewModel.navegarA(SeccionApp.CATALOGO) }) {
                                    Text(
                                        text = "Atrás",
                                        color = blueHeader,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Detalle del Equipo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = blueDarkText
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = blueCardBg)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = equipo.nombre,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = blueDarkText
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "ID: ${equipo.id}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Categoría: ${equipo.categoria}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val esDisponible = equipo.estado == "DISPONIBLE"
                                    Surface(
                                        color = if (esDisponible) blueContainer else Color(0xFFFFDAD6),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Estado: ${equipo.estado}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = if (esDisponible) Color(0xFF184E77) else Color(0xFF410E0B)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = { viewModel.irAFormulario() },
                                enabled = equipo.estado == "DISPONIBLE",
                                colors = ButtonDefaults.buttonColors(containerColor = blueHeader),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text(
                                    text = "Solicitar Préstamo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                SeccionApp.FORMULARIO -> SolicitudScreen(
                    uiState = uiState,
                    onAmbienteChange = { viewModel.onAmbienteChanged(it) },
                    onPropositoChange = { viewModel.onPropositoChanged(it) },
                    onDuracionChange = { viewModel.onDuracionChanged(it) },
                    onGuardarClick = { viewModel.guardarSolicitud() },
                    onAtrasClick = { viewModel.navegarA(SeccionApp.DETALLE_EQUIPO) }
                )

                SeccionApp.MIS_SOLICITUDES -> {
                    val solicitudesVisibles = uiState.solicitudes.filter { it.estado != "CANCELADA" }

                    if (solicitudesVisibles.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tienes solicitudes activas.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Mis Solicitudes (${solicitudesVisibles.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = blueDarkText,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(solicitudesVisibles) { solicitud ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = blueCardBg)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Solicitud #${solicitud.id}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Solicitante: ${solicitud.solicitante}", fontFamily = FontFamily.SansSerif)
                                            Text(text = "Equipo ID: ${solicitud.equipoId}", fontFamily = FontFamily.SansSerif)
                                            Text(text = "Fecha: ${solicitud.fechaInicio}", fontFamily = FontFamily.SansSerif)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Estado: ${solicitud.estado}",
                                                color = blueHeader,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = { viewModel.cancelarSolicitud(solicitud.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC93B2B)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Cancelar Solicitud",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.SansSerif
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}