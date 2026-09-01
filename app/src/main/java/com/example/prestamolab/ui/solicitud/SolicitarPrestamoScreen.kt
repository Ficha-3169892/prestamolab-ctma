package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun SolicitarPrestamoScreen(
    equipoId: Int,
    viewModel: PrestamoViewModel,
    onVolver: () -> Unit,
    onSolicitudCreada: () -> Unit
) {
    var ambienteDestino by remember {
        mutableStateOf("")
    }

    var proposito by remember {
        mutableStateOf("")
    }

    var duracionTexto by remember {
        mutableStateOf("")
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Crear préstamo"
        )

        OutlinedTextField(
            value = ambienteDestino,
            onValueChange = {
                ambienteDestino = it
            },
            label = {
                Text("Ambiente de destino")
            },
            placeholder = {
                Text("Ej: Ambiente 302")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = proposito,
            onValueChange = {
                if (it.length <= 180) {
                    proposito = it
                }
            },
            label = {
                Text("Propósito")
            },
            placeholder = {
                Text("¿Para qué necesitas el equipo?")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Text(
            text = "${proposito.length}/180 caracteres"
        )

        OutlinedTextField(
            value = duracionTexto,
            onValueChange = {
                duracionTexto = it.filter { caracter ->
                    caracter.isDigit()
                }
            },
            label = {
                Text("Duración en horas")
            },
            placeholder = {
                Text("Entre 1 y 8 horas")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {

                val duracionHoras =
                    duracionTexto.toIntOrNull() ?: 0

                val solicitudCreada =
                    viewModel.crearSolicitud(
                        equipoId = equipoId,
                        ambienteDestino = ambienteDestino,
                        proposito = proposito,
                        duracionHoras = duracionHoras
                    )

                if (solicitudCreada) {
                    onSolicitudCreada()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear préstamo")
        }

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        uiState.mensaje?.let { mensaje ->

            Text(
                text = mensaje
            )
        }
    }
}