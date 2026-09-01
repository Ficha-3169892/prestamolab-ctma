package com.example.prestamolab.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun DetalleEquipoScreen(
    equipoId: Int,
    viewModel: PrestamoViewModel,
    onVolver: () -> Unit,
    onSolicitar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val equipo = uiState.equipos.find {
        it.id == equipoId
    }

    if (equipo == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Equipo no encontrado")

            Button(
                onClick = onVolver,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Volver")
            }
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Button(
            onClick = onVolver
        ) {
            Text("Volver")
        }

        Text(
            text = equipo.nombre
        )

        Text(
            text = "Categoría: ${equipo.categoria}"
        )

        Text(
            text = "Estado: ${equipo.estado}"
        )

        if (equipo.estado == EstadoEquipo.DISPONIBLE) {

            Button(
                onClick = onSolicitar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solicitar préstamo")
            }

        } else {

            Text(
                text = "Este equipo no está disponible."
            )
        }
    }
}