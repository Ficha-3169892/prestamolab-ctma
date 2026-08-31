package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun MisSolicitudesScreen(
    viewModel: PrestamoViewModel,
    onSolicitudClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Mis solicitudes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta el estado de tus préstamos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(
                top = 4.dp,
                bottom = 16.dp
            )
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        if (uiState.solicitudes.isEmpty()) {

            Text(
                text = "No tienes solicitudes registradas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {

                items(
                    items = uiState.solicitudes,
                    key = { solicitud -> solicitud.id }
                ) { solicitud ->

                    val equipo = uiState.equipos.find {
                        it.id == solicitud.equipoId
                    }

                    Card(
                        onClick = {
                            onSolicitudClick(solicitud.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            Text(
                                text = "Solicitud #${solicitud.id}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Equipo: ${
                                    equipo?.nombre ?: "Equipo no encontrado"
                                }",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Destino: ${solicitud.ambienteDestino}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Duración: ${solicitud.duracionHoras} hora(s)",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Estado: ${solicitud.estado}",
                                style = MaterialTheme.typography.titleSmall,
                                color = when (solicitud.estado.toString()) {
                                    "SOLICITADA" ->
                                        MaterialTheme.colorScheme.primary

                                    "CANCELADA" ->
                                        MaterialTheme.colorScheme.error

                                    else ->
                                        MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
