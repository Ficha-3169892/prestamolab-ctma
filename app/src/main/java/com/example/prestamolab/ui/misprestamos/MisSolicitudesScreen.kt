package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.viewmodel.PrestamoViewModel
import com.example.prestamolab.ui.theme.EmeraldSuccess
import com.example.prestamolab.ui.theme.AmberWarning
import com.example.prestamolab.ui.theme.RoseError
import com.example.prestamolab.ui.theme.IndigoPrimary
import com.example.prestamolab.ui.theme.TealSecondary
import com.example.prestamolab.ui.theme.RoseError
import com.example.prestamolab.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    viewModel: PrestamoViewModel,
    onSolicitudClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Préstamos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            if (uiState.solicitudes.isEmpty()) {
                EmptySolicitudes()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(
                        items = uiState.solicitudes,
                        key = { solicitud -> solicitud.id }
                    ) { solicitud ->
                        val equipo = uiState.equipos.find { it.id == solicitud.equipoId }
                        SolicitudCard(
                            solicitud = solicitud,
                            equipoNombre = equipo?.nombre ?: "Desconocido",
                            onClick = { onSolicitudClick(solicitud.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySolicitudes() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.History, 
                contentDescription = null, 
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No tienes solicitudes", 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SolicitudCard(
    solicitud: SolicitudPrestamo,
    equipoNombre: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipoNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Solicitud #${solicitud.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusPill(estado = solicitud.estado)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${solicitud.duracionHoras}h",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = solicitud.ambienteDestino,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StatusPill(estado: EstadoSolicitud) {
    val color = when (estado) {
        EstadoSolicitud.SOLICITADA -> IndigoPrimary
        EstadoSolicitud.APROBADA -> EmeraldSuccess
        EstadoSolicitud.ENTREGADA -> TealSecondary
        EstadoSolicitud.DEVUELTA -> Color.Gray
        EstadoSolicitud.CANCELADA, EstadoSolicitud.RECHAZADA -> RoseError
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = CircleShape,
        modifier = Modifier.clip(CircleShape)
    ) {
        Text(
            text = estado.name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
