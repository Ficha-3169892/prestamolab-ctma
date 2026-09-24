package com.example.prestamolab.ui.inventario

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
import com.example.prestamolab.model.EstadoEquipo

/** HU-12: lista del inventario con las acciones del instructor. */
@Composable
fun InventarioScreen(
    equipos: List<Equipo>,
    mensajeError: String?,
    onNuevoClick: () -> Unit,
    onEditarClick: (Int) -> Unit,
    onEliminarClick: (Int) -> Unit,
    onAtrasClick: () -> Unit
) {
    var porEliminar by remember { mutableStateOf<Equipo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onAtrasClick) { Text("← Gestión", fontFamily = FontFamily.SansSerif) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventario (${equipos.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F2537),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onNuevoClick) { Text("Nuevo equipo", fontFamily = FontFamily.SansSerif) }
        }
        mensajeError?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(equipos, key = { it.id }) { equipo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            equipo.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text("Categoría: ${equipo.categoria}", fontFamily = FontFamily.SansSerif)
                        Text(
                            "Estado: ${equipo.estado}",
                            fontFamily = FontFamily.SansSerif,
                            color = if (equipo.estado == EstadoEquipo.DISPONIBLE) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onEditarClick(equipo.id) },
                                modifier = Modifier.testTag("editar-${equipo.id}")
                            ) { Text("Editar", fontFamily = FontFamily.SansSerif) }
                            OutlinedButton(
                                onClick = { porEliminar = equipo },
                                modifier = Modifier.testTag("eliminar-${equipo.id}")
                            ) { Text("Eliminar", fontFamily = FontFamily.SansSerif) }
                        }
                    }
                }
            }
        }
    }

    // CA-HU12-04: la regla se valida en el repositorio; el diálogo evita eliminar por accidente
    porEliminar?.let { equipo ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar ${equipo.nombre}?", fontFamily = FontFamily.SansSerif) },
            text = { Text("Solo se pueden eliminar equipos sin préstamos registrados.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarClick(equipo.id)
                    porEliminar = null
                }) { Text("Confirmar eliminación") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}
