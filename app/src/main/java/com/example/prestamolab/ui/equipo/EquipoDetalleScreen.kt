package com.example.prestamolab.ui.equipo

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
fun EquipoDetalleScreen(
    equipoId: Int,
    viewModel: PrestamoViewModel,
    onSolicitarClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    val equipo = uiState.equipos.find {
        it.id == equipoId
    }

    if (equipo == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Equipo no encontrado",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "El equipo solicitado no existe o ya no está disponible.",
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = equipo.nombre,
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
                    text = "Categoría",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = equipo.categoria.toString(),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = equipo.estado.toString(),
                    color = when (equipo.estado.toString()) {
                        "DISPONIBLE" ->
                            MaterialTheme.colorScheme.primary

                        "RESERVADO" ->
                            MaterialTheme.colorScheme.error

                        else ->
                            MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        if (equipo.estado.toString() == "DISPONIBLE") {

            Button(
                onClick = {
                    onSolicitarClick(equipo.id)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solicitar préstamo")
            }
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
