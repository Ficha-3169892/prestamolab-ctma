package com.example.prestamolab.ui.actividades

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
import com.example.prestamolab.model.Actividad

/** HU-11: el instructor mantiene las actividades; el estudiante las consulta (CA-HU11-05). */
@Composable
fun ActividadesScreen(
    actividades: List<Actividad>,
    puedeGestionar: Boolean,
    mensajeError: String?,
    onNuevaClick: () -> Unit,
    onEditarClick: (Int) -> Unit,
    onEliminarClick: (Int) -> Unit
) {
    var porEliminar by remember { mutableStateOf<Actividad?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Actividades formativas (${actividades.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F2537),
                modifier = Modifier.weight(1f)
            )
            if (puedeGestionar) {
                Button(onClick = onNuevaClick) { Text("Nueva", fontFamily = FontFamily.SansSerif) }
            }
        }
        mensajeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (actividades.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay actividades programadas.", fontFamily = FontFamily.SansSerif)
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(actividades, key = { it.id }) { actividad ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            actividad.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text("Fecha: ${actividad.fecha}", fontFamily = FontFamily.SansSerif)
                        Text("Ambiente: ${actividad.ambiente}", fontFamily = FontFamily.SansSerif)
                        if (actividad.descripcion.isNotBlank()) {
                            Text(actividad.descripcion, style = MaterialTheme.typography.bodySmall)
                        }
                        if (puedeGestionar) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onEditarClick(actividad.id) },
                                    modifier = Modifier.testTag("editar-actividad-${actividad.id}")
                                ) { Text("Editar", fontFamily = FontFamily.SansSerif) }
                                OutlinedButton(
                                    onClick = { porEliminar = actividad },
                                    modifier = Modifier.testTag("eliminar-actividad-${actividad.id}")
                                ) { Text("Eliminar", fontFamily = FontFamily.SansSerif) }
                            }
                        }
                    }
                }
            }
        }
    }

    // CA-HU11-04: si se cancela la confirmación, la actividad se conserva
    porEliminar?.let { actividad ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar ${actividad.titulo}?", fontFamily = FontFamily.SansSerif) },
            text = { Text("La actividad se quitará para todos los usuarios.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarClick(actividad.id)
                    porEliminar = null
                }) { Text("Confirmar eliminación") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}
