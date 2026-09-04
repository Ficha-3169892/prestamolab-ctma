package com.example.prestamolab.ui.equipo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoDetailScreen(
    equipo: Equipo?,
    onBackClick: () -> Unit,
    onSolicitarClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Equipo") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (equipo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Equipo no encontrado", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = equipo.nombre, style = MaterialTheme.typography.headlineMedium)
                Text(text = "Categoría: ${equipo.categoria}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Estado: ${equipo.estado}", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onSolicitarClick(equipo.id) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = equipo.estado == "DISPONIBLE"
                ) {
                    Text("Solicitar Préstamo")
                }
            }
        }
    }
}