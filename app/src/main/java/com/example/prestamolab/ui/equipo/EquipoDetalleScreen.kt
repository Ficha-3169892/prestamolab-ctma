package com.example.prestamolab.ui.equipo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoDetalleScreen(
    equipo: Equipo?,
    onSolicitarClick: (Int) -> Unit,
    onVolverClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Equipo") },
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
            if (equipo == null) {
                Column {
                    Text("Equipo no encontrado o ID inexistente.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onVolverClick) {
                        Text("Volver al Catálogo")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = equipo.nombre, style = MaterialTheme.typography.headlineMedium)
                    Text(text = "ID: ${equipo.id}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Categoría: ${equipo.categoria}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Estado: ${equipo.estado}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (equipo.estado) {
                            EstadoEquipo.DISPONIBLE -> MaterialTheme.colorScheme.primary
                            EstadoEquipo.RESERVADO -> MaterialTheme.colorScheme.tertiary
                            EstadoEquipo.PRESTADO -> MaterialTheme.colorScheme.error
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (equipo.estado == EstadoEquipo.DISPONIBLE) {
                        Button(
                            onClick = { onSolicitarClick(equipo.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Solicitar Préstamo")
                        }
                    } else {
                        Text(
                            text = "Este equipo no está disponible actualmente para solicitudes.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}