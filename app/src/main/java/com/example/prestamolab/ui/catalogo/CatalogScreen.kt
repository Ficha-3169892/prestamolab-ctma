package com.example.prestamolab.ui.catalogo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    equipos: List<Equipo>,
    onEquipoClick: (Int) -> Unit,
    onNavigateToSolicitudes: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Equipos - PréstamoLab") },
                actions = {
                    TextButton(onClick = onNavigateToSolicitudes) {
                        Text("Mis Solicitudes")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(equipos) { equipo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEquipoClick(equipo.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Categoría: ${equipo.categoria}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estado: ${equipo.estado}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (equipo.estado == "DISPONIBLE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}