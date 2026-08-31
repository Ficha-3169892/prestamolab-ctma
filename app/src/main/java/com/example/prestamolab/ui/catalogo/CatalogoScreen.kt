package com.example.prestamolab.ui.catalogo

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
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun CatalogoScreen(
    viewModel: PrestamoViewModel,
    onEquipoClick: (Int) -> Unit,
    onMisSolicitudesClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Catálogo de equipos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta los recursos disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Button(
            onClick = onMisSolicitudesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis solicitudes")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {

            items(
                items = uiState.equipos,
                key = { equipo -> equipo.id }
            ) { equipo ->

                EquipoCard(
                    equipo = equipo,
                    onClick = {
                        onEquipoClick(equipo.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun EquipoCard(
    equipo: Equipo,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = equipo.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Categoría: ${equipo.categoria}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estado: ${equipo.estado}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
