package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.SolicitudPrestamo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    solicitudes: List<SolicitudPrestamo>,
    onSolicitudClick: (Int) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Solicitudes") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (solicitudes.isEmpty()) {
                Text("No hay solicitudes registradas.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(solicitudes) { solicitud ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSolicitudClick(solicitud.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Solicitud #${solicitud.id}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Destino: ${solicitud.ambienteDestino}")
                                Text(text = "Estado: ${solicitud.estado}")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al Catálogo")
            }
        }
    }
}
