package com.example.prestamolab.ui.actividades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** HU-11: crear o editar una actividad formativa. */
@Composable
fun ActividadFormularioScreen(
    formulario: FormularioActividad,
    esNueva: Boolean,
    actividadNoEncontrada: Boolean,
    mensajeError: String?,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onAmbienteChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onAtrasClick: () -> Unit
) {
    if (actividadNoEncontrada) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Actividad no encontrada", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAtrasClick) { Text("Volver a actividades") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onAtrasClick) { Text("← Actividades", fontFamily = FontFamily.SansSerif) }
        Text(
            text = if (esNueva) "Nueva actividad" else "Editar actividad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F2537)
        )
        Campo("Título", formulario.titulo, formulario.errores.titulo, onTituloChange)
        Campo("Descripción (opcional)", formulario.descripcion, formulario.errores.descripcion, onDescripcionChange, unaLinea = false)
        Campo("Ambiente", formulario.ambiente, formulario.errores.ambiente, onAmbienteChange)
        Campo(
            "Fecha y hora (AAAA-MM-DD HH:MM)", formulario.fecha, formulario.errores.fecha, onFechaChange,
            ayuda = "Ejemplo: 2026-10-15 08:00"
        )
        mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = onGuardarClick,
            enabled = !formulario.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (formulario.guardando) "Guardando..." else "Guardar actividad", fontFamily = FontFamily.SansSerif)
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    error: String?,
    onChange: (String) -> Unit,
    unaLinea: Boolean = true,
    ayuda: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = (error ?: ayuda)?.let { { Text(it) } },
        singleLine = unaLinea,
        modifier = Modifier.fillMaxWidth()
    )
}
