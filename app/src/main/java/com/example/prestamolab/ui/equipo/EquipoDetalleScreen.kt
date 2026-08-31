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
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle de Equipo") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (equipo == null) {
                Text("Equipo no encontrado o ID inexistente.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onVolver) { Text("Volver") }
            } else {
                Text(text = equipo.nombre, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Categoría: ${equipo.categoria}")
                Text(text = "Estado actual: ${equipo.estado}")
                Spacer(modifier = Modifier.height(24.dp))

                if (equipo.estado == EstadoEquipo.DISPONIBLE) {
                    Button(
                        onClick = { onSolicitarClick(equipo.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Solicitar Préstamo")
                    }
                } else {
                    Text(
                        text = "Este equipo no se encuentra disponible actualmente.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al Catálogo")
                }
            }
        }
    }
}
