package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun SolicitarScreen(
    equipoId: Int,
    viewModel: PrestamoViewModel,
    onSolicitudCreada: () -> Unit,
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
                text = "No es posible realizar una solicitud para este equipo.",
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

    if (equipo.estado.toString() != "DISPONIBLE") {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Equipo no disponible",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Este equipo no puede solicitarse porque actualmente está ${equipo.estado}.",
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

    var ambienteDestino by remember {
        mutableStateOf("")
    }

    var proposito by remember {
        mutableStateOf("")
    }

    var duracionHoras by remember {
        mutableStateOf("")
    }

    var errorAmbiente by remember {
        mutableStateOf(false)
    }

    var errorProposito by remember {
        mutableStateOf(false)
    }

    var errorDuracion by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Solicitar préstamo",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Equipo: ${equipo.nombre}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Estado: ${equipo.estado}",
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedTextField(
            value = ambienteDestino,
            onValueChange = {
                ambienteDestino = it
                errorAmbiente = false
            },
            label = {
                Text("Ambiente o destino")
            },
            isError = errorAmbiente,
            supportingText = {
                if (errorAmbiente) {
                    Text("El ambiente o destino es obligatorio")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = proposito,
            onValueChange = {
                proposito = it
                errorProposito = false
            },
            label = {
                Text("Propósito")
            },
            isError = errorProposito,
            supportingText = {
                if (errorProposito) {
                    Text("El propósito debe tener entre 10 y 180 caracteres")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = duracionHoras,
            onValueChange = { nuevoValor ->

                if (nuevoValor.all { it.isDigit() }) {
                    duracionHoras = nuevoValor
                    errorDuracion = false
                }
            },
            label = {
                Text("Duración en horas")
            },
            isError = errorDuracion,
            supportingText = {
                if (errorDuracion) {
                    Text("La duración debe estar entre 1 y 8 horas")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.mensaje != null) {
            Text(
                text = uiState.mensaje!!,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {

                val horas = duracionHoras.toIntOrNull()

                errorAmbiente = ambienteDestino
                    .trim()
                    .isEmpty()

                errorProposito = proposito
                    .trim()
                    .length !in 10..180

                errorDuracion =
                    horas == null || horas !in 1..8

                val formularioValido =
                    !errorAmbiente &&
                            !errorProposito &&
                            !errorDuracion

                if (formularioValido && horas != null) {

                    val creada = viewModel.crearSolicitud(
                        equipoId = equipoId,
                        ambienteDestino = ambienteDestino,
                        proposito = proposito,
                        duracionHoras = horas
                    )

                    if (creada) {
                        onSolicitudCreada()
                    }
                }
            },
            enabled = !uiState.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.guardando) {
                    "Guardando..."
                } else {
                    "Solicitar préstamo"
                }
            )
        }

        Button(
            onClick = onBackClick,
            enabled = !uiState.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}
