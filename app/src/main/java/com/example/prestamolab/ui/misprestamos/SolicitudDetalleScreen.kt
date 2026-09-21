package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.viewmodel.PrestamoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    solicitudId: Int,
    viewModel: PrestamoViewModel,
    onDevolverClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val solicitud = uiState.solicitudes.find { it.id == solicitudId }
    val equipo = uiState.equipos.find { it.id == solicitud?.equipoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Préstamo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (solicitud == null || equipo == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) { Text("Solicitud no encontrada") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Main Info
            Column {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "ID Solicitud: #${solicitud.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Status Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRow(icon = Icons.Default.Info, label = "Estado", value = solicitud.estado.name)
                    DetailRow(icon = Icons.Default.LocationOn, label = "Destino", value = solicitud.ambienteDestino)
                }
            }

            // Additional Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Propósito", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(solicitud.proposito, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // GPS & Photo
            if (solicitud.latitud != null || solicitud.fotoUri != null) {
                Text("Evidencia y Registro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                if (solicitud.latitud != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Registrado en: ${solicitud.latitud}, ${solicitud.longitud}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (solicitud.fotoUri != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Box {
                            AsyncImage(
                                model = solicitud.fotoUri,
                                contentDescription = "Foto de evidencia",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Photo, contentDescription = null, size = 14.dp, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Evidencia adjunta", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Actions
            Spacer(modifier = Modifier.height(16.dp))
            
            if (solicitud.estado == EstadoSolicitud.SOLICITADA) {
                OutlinedButton(
                    onClick = { viewModel.cancelarSolicitud(solicitud.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar Solicitud")
                }
            }

            if (solicitud.estado in listOf(EstadoSolicitud.SOLICITADA, EstadoSolicitud.ENTREGADA, EstadoSolicitud.APROBADA)) {
                Button(
                    onClick = { onDevolverClick(solicitud.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Registrar Devolución")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

// Icon helper since modifier.size doesn't apply to icon internal content properly in some versions
@Composable
private fun Icon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}
