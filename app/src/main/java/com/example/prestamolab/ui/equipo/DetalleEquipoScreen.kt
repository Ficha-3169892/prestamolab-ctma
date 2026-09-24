package com.example.prestamolab.ui.equipo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo

/** Lo que la ruta `equipo/{id}` sabe del equipo pedido. */
sealed interface EstadoDetalleEquipo {
    data object Cargando : EstadoDetalleEquipo
    data object NoEncontrado : EstadoDetalleEquipo
    data class Encontrado(val equipo: Equipo) : EstadoDetalleEquipo
}

@Composable
fun DetalleEquipoScreen(
    estado: EstadoDetalleEquipo,
    puedeSolicitar: Boolean,
    onAtrasClick: () -> Unit,
    onSolicitarClick: () -> Unit
) {
    val blueHeader = Color(0xFF1E6091)
    val blueContainer = Color(0xFFD9EDF8)
    val blueDarkText = Color(0xFF0F2537)
    val blueCardBg = Color(0xFFF0F7FA)

    when (estado) {
        EstadoDetalleEquipo.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        EstadoDetalleEquipo.NoEncontrado -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Equipo no encontrado", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAtrasClick) {
                    Text("Volver al Catálogo")
                }
            }
        }

        is EstadoDetalleEquipo.Encontrado -> {
            val equipo = estado.equipo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onAtrasClick) {
                        Text(
                            text = "Atrás",
                            color = blueHeader,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Detalle del Equipo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = blueDarkText
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = blueCardBg)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = equipo.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = blueDarkText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ID: ${equipo.id}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Categoría: ${equipo.categoria}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val esDisponible = equipo.estado == EstadoEquipo.DISPONIBLE
                        Surface(
                            color = if (esDisponible) blueContainer else Color(0xFFFFDAD6),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Estado: ${equipo.estado}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = if (esDisponible) Color(0xFF184E77) else Color(0xFF410E0B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (puedeSolicitar) Button(
                    onClick = onSolicitarClick,
                    enabled = equipo.estado == EstadoEquipo.DISPONIBLE,
                    colors = ButtonDefaults.buttonColors(containerColor = blueHeader),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Solicitar Préstamo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White
                    )
                }
            }
        }
    }
}
