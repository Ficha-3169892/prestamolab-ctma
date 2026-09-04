package com.example.prestamolab.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolab.ui.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudScreen(
    uiState: PrestamoUiState,
    onAmbienteChange: (String) -> Unit,
    onPropositoChange: (String) -> Unit,
    onDuracionChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onAtrasClick: () -> Unit
) {
    val blueAccent = Color(0xFF1E6091)
    val blueBackgroundCard = Color(0xFFE8F1F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onAtrasClick) {
                Text(
                    text = "Atrás",
                    color = blueAccent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Registrar Solicitud",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F2537)
            )
        }

        uiState.equipoSeleccionado?.let { equipo ->
            Card(
                colors = CardDefaults.cardColors(containerColor = blueBackgroundCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Equipo: ${equipo.nombre}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF184E77)
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.ambiente,
            onValueChange = onAmbienteChange,
            label = { Text("Ambiente o Destino", fontFamily = FontFamily.SansSerif) },
            isError = uiState.errorAmbiente != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        if (uiState.errorAmbiente != null) {
            Text(
                text = uiState.errorAmbiente,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.SansSerif
            )
        }

        OutlinedTextField(
            value = uiState.proposito,
            onValueChange = onPropositoChange,
            label = { Text("Propósito (10-180 caracteres)", fontFamily = FontFamily.SansSerif) },
            isError = uiState.errorProposito != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            maxLines = 4,
            shape = RoundedCornerShape(10.dp)
        )
        if (uiState.errorProposito != null) {
            Text(
                text = uiState.errorProposito,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.SansSerif
            )
        }

        OutlinedTextField(
            value = uiState.duracionHoras,
            onValueChange = onDuracionChange,
            label = { Text("Duración estimada (1-8 horas)", fontFamily = FontFamily.SansSerif) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGuardarClick,
            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Solicitar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
        }
    }
}