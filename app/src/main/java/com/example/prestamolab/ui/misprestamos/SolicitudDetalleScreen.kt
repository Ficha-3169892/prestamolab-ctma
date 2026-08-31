package com.example.prestamolab.ui.misprestamos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun SolicitudDetalleScreen(
    solicitudId: Int,
    viewModel: PrestamoViewModel,
    onBackClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    val solicitud = uiState.solicitudes.find {
        it.id == solicitudId
    }

    if (solicitud == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Solicitud no encontrada",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "La solicitud indicada no existe.",
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }

        return
    }

    val equipo = uiState.equipos.find {
        it.id == solicitud.equipoId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Detalle de solicitud",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Solicitud #${solicitud.id}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Equipo: ${
                        equipo?.nombre ?: "Equipo no encontrado"
                    }",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Ambiente o destino: ${solicitud.ambienteDestino}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Propósito: ${solicitud.proposito}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Duración: ${solicitud.duracionHoras} horas",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Estado: ${solicitud.estado}",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (solicitud.estado.toString()) {
                        "CANCELADA" ->
                            MaterialTheme.colorScheme.error

                        "SOLICITADA" ->
                            MaterialTheme.colorScheme.primary

                        else ->
                            MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        if (solicitud.estado.toString() == "SOLICITADA") {

            Button(
                onClick = {
                    viewModel.cancelarSolicitud(solicitud.id)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar solicitud")
            }
        }

        if (uiState.mensaje != null) {

            Text(
                text = uiState.mensaje!!,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
