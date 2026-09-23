package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prestamolab.model.SolicitudPrestamo
import com.example.prestamolab.viewmodel.ListadoUiState
import com.example.prestamolab.viewmodel.PrestamoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    viewModel: PrestamoViewModel,
    onSolicitudClick: (Int) -> Unit,
    onVolverClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes") },
                navigationIcon = {
                    TextButton(onClick = onVolverClick) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de búsqueda en tiempo real
            OutlinedTextField(
                value = uiState.busquedaQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                label = { Text("Buscar solicitudes...") },
                placeholder = { Text("Buscar por ambiente o estado") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Evaluación del estado del listado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState.listadoState) {
                    is ListadoUiState.Cargando -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando solicitudes...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is ListadoUiState.Vacio -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "No hay solicitudes registradas o encontradas",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = onVolverClick) {
                                Text("Volver al Catálogo")
                            }
                        }
                    }
                    is ListadoUiState.Contenido -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.lista, key = { it.id }) { solicitud ->
                                SolicitudItem(
                                    solicitud = solicitud,
                                    onClick = { onSolicitudClick(solicitud.id) }
                                )
                            }
                        }
                    }
                    is ListadoUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = state.mensaje,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(onClick = { viewModel.reintentarCarga() }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudItem(solicitud: SolicitudPrestamo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Solicitud #${solicitud.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Ambiente: ${solicitud.ambienteDestino}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${solicitud.estado}", style = MaterialTheme.typography.labelLarge)
        }
    }
}
