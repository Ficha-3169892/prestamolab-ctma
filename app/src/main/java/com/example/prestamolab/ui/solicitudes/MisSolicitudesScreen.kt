package com.example.prestamolab.ui.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoSolicitud
import com.example.prestamolab.model.SolicitudPrestamo

@Composable
fun MisSolicitudesScreen(
    solicitudes: List<SolicitudPrestamo>,
    equipos: List<Equipo>,
    onCancelarClick: (Int) -> Unit,
    onRegistrarDevolucionClick: (Int) -> Unit
) {
    val blueHeader = Color(0xFF1E6091)
    val blueDarkText = Color(0xFF0F2537)
    val blueCardBg = Color(0xFFF0F7FA)

    // CA-HU04-03: canceladas y devueltas no son activas
    val solicitudesVisibles = solicitudes.filter {
        it.estado == EstadoSolicitud.SOLICITADA || it.estado == EstadoSolicitud.PRESTADO
    }

    if (solicitudesVisibles.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tienes solicitudes activas.",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.SansSerif
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Solicitudes (${solicitudesVisibles.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = blueDarkText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(solicitudesVisibles) { solicitud ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = blueCardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Solicitud #${solicitud.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Solicitante: ${solicitud.solicitante}", fontFamily = FontFamily.SansSerif)
                        // El id local no coincide con el de Supabase: se muestra el nombre
                        val nombreEquipo = equipos.find { it.id == solicitud.equipoId }?.nombre
                            ?: "#${solicitud.equipoId}"
                        Text(text = "Equipo: $nombreEquipo", fontFamily = FontFamily.SansSerif)
                        Text(text = "Fecha: ${solicitud.fechaInicio}", fontFamily = FontFamily.SansSerif)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estado: ${solicitud.estado}",
                            color = blueHeader,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        // CA-HU05-01: solo un préstamo PRESTADO se devuelve; solo uno SOLICITADO se cancela
                        if (solicitud.estado == EstadoSolicitud.PRESTADO) {
                            Button(
                                onClick = { onRegistrarDevolucionClick(solicitud.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = blueHeader),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Registrar devolución",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        } else {
                            Button(
                                onClick = { onCancelarClick(solicitud.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC93B2B)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Cancelar Solicitud",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
