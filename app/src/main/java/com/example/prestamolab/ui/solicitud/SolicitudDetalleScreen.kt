package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    solicitud: SolicitudPrestamo?,
    mensajeError: String?,
    onCancelar: (Int) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle de Solicitud") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (solicitud == null) {
                Text("Solicitud no encontrada.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onVolver) { Text("Volver") }
            } else {
                val colorEstado = when (solicitud.estado) {
                    EstadoSolicitud.SOLICITADA -> Color(0xFF2196F3)
                    EstadoSolicitud.APROBADA -> Color(0xFF4CAF50)
                    EstadoSolicitud.CANCELADA -> Color(0xFFF44336)
                    else -> Color.Gray
                }

                Text(text = "Solicitud #${solicitud.id}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Equipo ID: ${solicitud.equipoId}")
                Text(text = "Ambiente Destino: ${solicitud.ambienteDestino}")
                Text(text = "Propósito: ${solicitud.proposito}")
                Text(text = "Duración: ${solicitud.duracionHoras} horas")
                Text(text = "Estado: ${solicitud.estado}", color = colorEstado)
                Spacer(modifier = Modifier.weight(1f))

                if (mensajeError != null) {
                    Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (solicitud.estado == EstadoSolicitud.SOLICITADA) {
                    Button(
                        onClick = { onCancelar(solicitud.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar Solicitud")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver")
                }
            }
        }
    }
}
