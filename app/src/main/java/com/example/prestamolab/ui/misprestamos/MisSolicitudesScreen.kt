package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.SolicitudPrestamo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    solicitudes: List<SolicitudPrestamo>,
    onSolicitudClick: (Int) -> Unit,
    onVolverClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes") },
                navigationIcon = {
                    TextButton(onClick = onVolverClick) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (solicitudes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes solicitudes registradas.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(solicitudes) { solicitud ->
                    SolicitudItem(solicitud = solicitud, onClick = { onSolicitudClick(solicitud.id) })
                }
            }
        }
    }
}

@Composable
fun SolicitudItem(solicitud: SolicitudPrestamo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Solicitud #${solicitud.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Ambiente: ${solicitud.ambienteDestino}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${solicitud.estado}", style = MaterialTheme.typography.labelLarge)
        }
    }
}