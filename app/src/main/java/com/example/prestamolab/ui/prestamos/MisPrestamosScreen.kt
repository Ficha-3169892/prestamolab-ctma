package com.example.prestamolab.ui.prestamos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.viewmodel.PrestamoViewModel

@Composable
fun MisPrestamosScreen(
    viewModel: PrestamoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val solicitudes = uiState.solicitudes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Mis préstamos",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (solicitudes.isEmpty()) {

            Text(
                text = "No tienes préstamos registrados."
            )

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(solicitudes) { solicitud ->

                    val equipo = uiState.equipos.find {
                        it.id == solicitud.equipoId
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = equipo?.nombre
                                    ?: "Equipo no encontrado"
                            )

                            Text(
                                text = "Ambiente: ${solicitud.ambienteDestino}"
                            )

                            Text(
                                text = "Propósito: ${solicitud.proposito}",
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = "Duración: ${solicitud.duracionHoras} horas",
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = "Estado: ${solicitud.estado}",
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            if (solicitud.estado == EstadoSolicitud.SOLICITADA) {

                                Button(
                                    onClick = {
                                        viewModel.cancelarSolicitud(
                                            solicitud.id
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Text("Cancelar solicitud")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}