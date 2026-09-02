package com.example.prestamolab.ui.catalogo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    equipos: List<Equipo>,
    onEquipoClick: (Int) -> Unit,
    onVerMisSolicitudesClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Equipos") },
                actions = {
                    TextButton(onClick = onVerMisSolicitudesClick) {
                        Text("Mis Solicitudes")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(equipos) { equipo ->
                EquipoItem(equipo = equipo, onClick = { onEquipoClick(equipo.id) })
            }
        }
    }
}

@Composable
fun EquipoItem(equipo: Equipo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "Categoría: ${equipo.categoria}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = when (equipo.estado) {
                    EstadoEquipo.DISPONIBLE -> "[✓] DISPONIBLE"
                    EstadoEquipo.RESERVADO -> "[!] RESERVADO"
                    EstadoEquipo.PRESTADO -> "[X] PRESTADO"
                },
                style = MaterialTheme.typography.labelLarge,
                color = when (equipo.estado) {
                    EstadoEquipo.DISPONIBLE -> MaterialTheme.colorScheme.primary
                    EstadoEquipo.RESERVADO -> MaterialTheme.colorScheme.tertiary
                    EstadoEquipo.PRESTADO -> MaterialTheme.colorScheme.error
                }
            )
        }
    }
}