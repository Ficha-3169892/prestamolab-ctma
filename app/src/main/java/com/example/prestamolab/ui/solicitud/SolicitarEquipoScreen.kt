package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarEquipoScreen(
    equipo: Equipo?,
    guardando: Boolean,
    mensajeError: String?,
    onGuardar: (String, String, Int) -> Unit,
    onVolver: () -> Unit
) {
    var ambiente by remember { mutableStateOf("") }
    var proposito by remember { mutableStateOf("") }
    var duracionTexto by remember { mutableStateOf("1") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registrar Solicitud") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (equipo == null) {
                Text("Equipo no válido")
                Button(onClick = onVolver) { Text("Volver") }
            } else {
                Text(text = "Solicitando: ${equipo.nombre}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ambiente,
                    onValueChange = { ambiente = it },
                    label = { Text("Ambiente o Destino") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = proposito,
                    onValueChange = { proposito = it },
                    label = { Text("Propósito (10-180 caract.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duracionTexto,
                    onValueChange = { duracionTexto = it },
                    label = { Text("Duración estimada (Horas 1-8)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (mensajeError != null) {
                    Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        val duracion = duracionTexto.toIntOrNull() ?: 0
                        onGuardar(ambiente, proposito, duracion)
                    },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar Solicitud")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            }
        }
    }
}
