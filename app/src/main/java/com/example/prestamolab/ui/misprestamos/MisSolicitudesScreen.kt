package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.EstadoSolicitud
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (solicitudes.isEmpty()) {
                    Text("No hay solicitudes registradas.", modifier = Modifier.padding(top = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(solicitudes) { solicitud ->
                            val colorEstado = when (solicitud.estado) {
                                EstadoSolicitud.SOLICITADA -> Color(0xFF2196F3)
                                EstadoSolicitud.APROBADA -> Color(0xFF4CAF50)
                                EstadoSolicitud.CANCELADA -> Color(0xFFF44336)
                                else -> Color.Gray
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSolicitudClick(solicitud.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Solicitud #${solicitud.id}", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Destino: ${solicitud.ambienteDestino}")
                                    Text(text = "Estado: ${solicitud.estado}", color = colorEstado)
                                }
                            }
                        }
                    }
                }
            }
            Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Volver al Catálogo")
            }
        }
    }
}
