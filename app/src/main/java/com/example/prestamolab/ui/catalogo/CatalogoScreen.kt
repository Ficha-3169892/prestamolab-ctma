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
fun CatalogoScreen(
    equipos: List<Equipo>,
    onEquipoClick: (Int) -> Unit,
    onVerSolicitudesClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Catálogo de Equipos") }) },
        bottomBar = {
            Button(
                onClick = onVerSolicitudesClick,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Mis Solicitudes")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(equipos) { equipo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onEquipoClick(equipo.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Categoría: ${equipo.categoria}")
                        Text(text = "Estado: ${equipo.estado}")
                    }
                }
            }
        }
    }
}
