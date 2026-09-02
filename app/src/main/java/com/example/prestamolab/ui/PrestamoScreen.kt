package com.example.prestamolab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamoScreen(viewModel: PrestamoViewModel = viewModel()) {
    // Ordenamos para que los DISPONIBLES queden arriba y los demás al final
    val equipos = viewModel.equipos.sortedBy { if (it.estado == "DISPONIBLE") 0 else 1 }

    // Filtramos para no mostrar las solicitudes canceladas en el listado activo
    val solicitudesActivas = viewModel.solicitudes.filter { it.estado != "CANCELADA" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Préstamo Lab - Gestión de Equipos") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Equipos Disponibles", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(equipos) { equipo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = equipo.nombre, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Estado: ${equipo.estado}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.solicitarPrestamo(
                                        equipoId = equipo.id,
                                        solicitante = "Andrés Vargas",
                                        fechaInicio = "2026-09-02",
                                        fechaFin = "2026-09-06"
                                    )
                                },
                                enabled = equipo.estado == "DISPONIBLE"
                            ) {
                                Text("Solicitar Préstamo")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Mis Solicitudes Activas (${solicitudesActivas.size})", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(solicitudesActivas) { solicitud ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Solicitante: ${solicitud.solicitante}")
                            Text(text = "Fechas: ${solicitud.fechaInicio} al ${solicitud.fechaFin}")
                            Text(text = "Estado: ${solicitud.estado}")
                            if (solicitud.estado == "PENDIENTE") {
                                TextButton(onClick = { viewModel.cancelarSolicitud(solicitud.id) }) {
                                    Text("Cancelar Solicitud", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}