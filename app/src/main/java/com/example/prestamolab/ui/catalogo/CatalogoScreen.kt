package com.example.prestamolab.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onEquipoClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "PréstamoLab CTMA",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Catálogo de equipos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            )
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(uiState.equipos) { equipo ->

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
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Categoría: ${equipo.categoria}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Estado: ${equipo.estado}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Ver detalle",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}