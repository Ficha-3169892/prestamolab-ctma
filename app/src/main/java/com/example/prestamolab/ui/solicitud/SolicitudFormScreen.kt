package com.example.prestamolab.ui.solicitud

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prestamolab.viewmodel.OperacionUiState
import com.example.prestamolab.viewmodel.PrestamoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    equipoId: Int,
    viewModel: PrestamoViewModel,
    onVolverClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var ambiente by remember { mutableStateOf("") }
    var proposito by remember { mutableStateOf("") }
    var duracionTexto by remember { mutableStateOf("1") }

    val operacionState = uiState.operacionState
    val guardando = operacionState is OperacionUiState.EnCurso

    // Escuchar operacionState para reaccionar a Exitosa
    LaunchedEffect(operacionState) {
        if (operacionState is OperacionUiState.Exitosa) {
            Toast.makeText(context, operacionState.mensaje, Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(operacionState.mensaje)
            viewModel.limpiarOperacionState()
            onVolverClick()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registrar Solicitud") },
                navigationIcon = {
                    TextButton(
                        onClick = onVolverClick,
                        enabled = !guardando
                    ) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ambiente,
                onValueChange = { ambiente = it },
                label = { Text("Ambiente o Destino") },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = proposito,
                onValueChange = { proposito = it },
                label = { Text("Propósito (10-180 caracteres)") },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = duracionTexto,
                onValueChange = { duracionTexto = it },
                label = { Text("Duración estimada (1-8 horas)") },
                enabled = !guardando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Evaluación de error cuando operacionState es Fallida
            if (operacionState is OperacionUiState.Fallida) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = operacionState.mensaje,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (uiState.mensaje != null && operacionState !is OperacionUiState.Exitosa) {
                Text(
                    text = uiState.mensaje!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val duracion = duracionTexto.toIntOrNull() ?: 0
                    viewModel.registrarSolicitud(equipoId, ambiente, proposito, duracion)
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Text("Guardando...")
                    }
                } else {
                    Text("Guardar Solicitud")
                }
            }
        }
    }
}
