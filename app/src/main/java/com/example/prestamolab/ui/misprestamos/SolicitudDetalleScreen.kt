package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    solicitud: SolicitudPrestamo?,
    onCancelarClick: (Int) -> Unit,
    onVolverClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Solicitud") },
                navigationIcon = {
                    TextButton(onClick = onVolverClick) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (solicitud == null) {
                Column {
                    Text("Solicitud no encontrada o ID inexistente.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onVolverClick) {
                        Text("Volver")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Solicitud #${solicitud.id}", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "ID del Equipo: ${solicitud.equipoId}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Ambiente/Destino: ${solicitud.ambienteDestino}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Propósito: ${solicitud.proposito}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Duración: ${solicitud.duracionHoras} horas", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Estado: ${solicitud.estado}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (solicitud.estado) {
                            EstadoSolicitud.SOLICITADA -> MaterialTheme.colorScheme.primary
                            EstadoSolicitud.CANCELADA -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (solicitud.estado == EstadoSolicitud.SOLICITADA) {
                        Button(
                            onClick = { onCancelarClick(solicitud.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar Solicitud")
                        }
                    }
                }
            }
        }
    }
}