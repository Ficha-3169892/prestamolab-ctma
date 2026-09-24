package com.example.prestamolab.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** HU-12: registrar un equipo nuevo o editar uno existente. */
@Composable
fun EquipoFormularioScreen(
    formulario: FormularioEquipo,
    esNuevo: Boolean,
    equipoNoEncontrado: Boolean,
    mensajeError: String?,
    onNombreChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onAtrasClick: () -> Unit
) {
    if (equipoNoEncontrado) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Equipo no encontrado", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAtrasClick) { Text("Volver al inventario") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onAtrasClick) { Text("← Inventario", fontFamily = FontFamily.SansSerif) }
        Text(
            text = if (esNuevo) "Nuevo equipo" else "Editar equipo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F2537)
        )
        OutlinedTextField(
            value = formulario.nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre del equipo") },
            isError = formulario.errores.nombre != null,
            supportingText = formulario.errores.nombre?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formulario.categoria,
            onValueChange = onCategoriaChange,
            label = { Text("Categoría") },
            isError = formulario.errores.categoria != null,
            supportingText = formulario.errores.categoria?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (esNuevo) {
            Text("El equipo se registra como DISPONIBLE.", style = MaterialTheme.typography.bodySmall)
        }
        mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = onGuardarClick,
            enabled = !formulario.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (formulario.guardando) "Guardando..." else "Guardar equipo", fontFamily = FontFamily.SansSerif)
        }
    }
}
