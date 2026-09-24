package com.example.prestamolab.ui.gestion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Menú de gestión del instructor. Cada sección se implementa en el sprint indicado. */
@Composable
fun GestionScreen(onVolver: () -> Unit) {
    val blueAccent = Color(0xFF1E6091)
    val secciones = listOf(
        "Inventario de equipos" to "HU-12 · Sprint 6",
        "Actividades formativas" to "HU-11 · Sprint 6",
        "Revisar solicitudes" to "HU-14 · Sprint 7"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver) {
                Text("Atrás", color = blueAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontSize = 16.sp)
            }
            Text(
                text = "Gestión (Instructor)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F2537)
            )
        }
        secciones.forEach { (titulo, pendiente) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FA))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    Text("Próximamente: $pendiente", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.SansSerif)
                }
            }
        }
    }
}
