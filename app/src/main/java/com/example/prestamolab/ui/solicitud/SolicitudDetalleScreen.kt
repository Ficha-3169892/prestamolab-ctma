package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    solicitud: SolicitudPrestamo?,
    mensajeError: String?,
    onCancelar: (Int) -> Unit,
    onAdjuntarEvidencia: ((Int) -> Unit)? = null,
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

                if (!solicitud.evidenciaUrl.isNull_or_empty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Evidencia Fotográfica:",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AsyncImage(
                        model = solicitud.evidenciaUrl,
                        contentDescription = "Evidencia Fotográfica",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (mensajeError != null) {
                    Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (onAdjuntarEvidencia != null) {
                    Button(
                        onClick = { onAdjuntarEvidencia(solicitud.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adjuntar Evidencia Fotográfica")
                    }
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

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
