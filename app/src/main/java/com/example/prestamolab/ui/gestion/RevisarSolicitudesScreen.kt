package com.example.prestamolab.ui.gestion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.RevisionSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

/** HU-14: el instructor aprueba o rechaza (con motivo) las solicitudes SOLICITADAS. */
@Composable
fun RevisarSolicitudesScreen(
    solicitudes: List<SolicitudPrestamo>,
    equipos: List<Equipo>,
    mensajeError: String?,
    onAprobarClick: (Int) -> Unit,
    onRechazarClick: (id: Int, motivo: String) -> Unit,
    onAtrasClick: () -> Unit
) {
    val pendientes = solicitudes.filter { it.estado == EstadoSolicitud.SOLICITADA }
    var porRechazar by remember { mutableStateOf<SolicitudPrestamo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onAtrasClick) { Text("← Gestión", fontFamily = FontFamily.SansSerif) }
        Text(
            text = "Revisar solicitudes (${pendientes.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F2537),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        mensajeError?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (pendientes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay solicitudes pendientes.", fontFamily = FontFamily.SansSerif)
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pendientes, key = { it.id }) { solicitud ->
                TarjetaSolicitud(
                    solicitud = solicitud,
                    // El id local no coincide con el de Supabase: se muestra el nombre
                    nombreEquipo = equipos.find { it.id == solicitud.equipoId }?.nombre ?: "#${solicitud.equipoId}",
                    onAprobarClick = { onAprobarClick(solicitud.id) },
                    onRechazarClick = { porRechazar = solicitud }
                )
            }
        }
    }

    porRechazar?.let { solicitud ->
        DialogoRechazo(
            solicitudId = solicitud.id,
            onConfirmar = { motivo ->
                onRechazarClick(solicitud.id, motivo)
                porRechazar = null
            },
            onDescartar = { porRechazar = null }
        )
    }
}

@Composable
private fun TarjetaSolicitud(
    solicitud: SolicitudPrestamo,
    nombreEquipo: String,
    onAprobarClick: () -> Unit,
    onRechazarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Solicitud #${solicitud.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(Modifier.height(4.dp))
            // CA-HU14-01: estudiante, equipo, ambiente, propósito y duración
            Text("Estudiante: ${solicitud.solicitante}", fontFamily = FontFamily.SansSerif)
            Text("Equipo: $nombreEquipo", fontFamily = FontFamily.SansSerif)
            Text("Ambiente: ${solicitud.ambiente}", fontFamily = FontFamily.SansSerif)
            Text("Propósito: ${solicitud.proposito}", fontFamily = FontFamily.SansSerif)
            Text("Duración: ${solicitud.duracionHoras} h", fontFamily = FontFamily.SansSerif)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAprobarClick, modifier = Modifier.testTag("aprobar-${solicitud.id}")) { Text("Aprobar", fontFamily = FontFamily.SansSerif) }
                OutlinedButton(onClick = onRechazarClick) { Text("Rechazar", fontFamily = FontFamily.SansSerif) }
            }
        }
    }
}

@Composable
private fun DialogoRechazo(solicitudId: Int, onConfirmar: (String) -> Unit, onDescartar: () -> Unit) {
    var motivo by remember { mutableStateOf("") }
    val demasiadoLargo = motivo.trim().length > RevisionSolicitud.MOTIVO_MAXIMO

    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text("Rechazar solicitud #$solicitudId", fontFamily = FontFamily.SansSerif) },
        text = {
            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("Motivo del rechazo") },
                isError = demasiadoLargo,
                supportingText = {
                    Text(if (demasiadoLargo) "Máximo ${RevisionSolicitud.MOTIVO_MAXIMO} caracteres" else "Obligatorio")
                }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(motivo) }, enabled = motivo.isNotBlank() && !demasiadoLargo) {
                Text("Confirmar rechazo")
            }
        },
        dismissButton = { TextButton(onClick = onDescartar) { Text("Cancelar") } }
    )
}
