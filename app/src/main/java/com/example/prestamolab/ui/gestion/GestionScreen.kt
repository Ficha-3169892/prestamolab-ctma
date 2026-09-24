package com.example.prestamolab.ui.gestion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class SeccionGestion(val titulo: String, val detalle: String, val onClick: (() -> Unit)? = null)

/** Menú de gestión del instructor. Las secciones sin acción se implementan en el sprint indicado. */
@Composable
fun GestionScreen(onRevisarSolicitudesClick: () -> Unit, onInventarioClick: () -> Unit) {
    val secciones = listOf(
        SeccionGestion("Revisar solicitudes", "Aprobar o rechazar préstamos pendientes", onRevisarSolicitudesClick),
        SeccionGestion("Inventario de equipos", "Registrar, editar y eliminar equipos", onInventarioClick),
        SeccionGestion("Actividades formativas", "Próximamente: HU-11 · Sprint 6")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "Salir" y el catálogo están en las barras comunes del área autenticada
        Text(
            text = "Gestión (Instructor)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F2537)
        )
        secciones.forEach { seccion ->
            Card(
                onClick = { seccion.onClick?.invoke() },
                enabled = seccion.onClick != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FA))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(seccion.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                    Text(seccion.detalle, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.SansSerif)
                }
            }
        }
    }
}
