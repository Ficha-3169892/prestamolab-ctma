package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.prestamolab.viewmodel.PrestamoEvent
import com.example.prestamolab.viewmodel.PrestamoViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarScreen(
    equipoId: Int,
    viewModel: PrestamoViewModel,
    onSolicitudCreada: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val equipo = uiState.equipos.find { it.id == equipoId }

    var ambienteDestino by remember { mutableStateOf("") }
    var proposito by remember { mutableStateOf("") }
    var duracionHoras by remember { mutableStateOf("") }
    
    var errorAmbiente by remember { mutableStateOf(false) }
    var errorProposito by remember { mutableStateOf(false) }
    var errorDuracion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is PrestamoEvent.SolicitudCreada) {
                onSolicitudCreada()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (equipo == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) { Text("Equipo no disponible") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                Text(
                    text = "Equipo Seleccionado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Form Fields
            OutlinedTextField(
                value = ambienteDestino,
                onValueChange = { ambienteDestino = it; errorAmbiente = false },
                label = { Text("Ambiente o Destino") },
                placeholder = { Text("Ej. Laboratorio A-102") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorAmbiente,
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = proposito,
                onValueChange = { proposito = it; errorProposito = false },
                label = { Text("Propósito") },
                placeholder = { Text("Explique brevemente el uso...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                isError = errorProposito,
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = duracionHoras,
                onValueChange = { if (it.all { char -> char.isDigit() }) duracionHoras = it; errorDuracion = false },
                label = { Text("Duración (Horas)") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorDuracion,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )

            if (uiState.mensaje != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.mensaje!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val horas = duracionHoras.toIntOrNull()
                    errorAmbiente = ambienteDestino.trim().isEmpty()
                    errorProposito = proposito.trim().length !in 10..180
                    errorDuracion = horas == null || horas !in 1..8

                    if (!errorAmbiente && !errorProposito && !errorDuracion && horas != null) {
                        viewModel.crearSolicitud(equipoId, ambienteDestino, proposito, horas)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !uiState.guardando
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Confirmar Solicitud", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
