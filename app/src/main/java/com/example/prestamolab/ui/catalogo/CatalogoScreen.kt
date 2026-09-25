package com.example.prestamolab.ui.catalogo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    equipos: List < Equipo >,
    mensajeExito: String?,
    mensajeError: String?,
    onEquipoClick: (Int) -> Unit,
    onVerSolicitudesClick: () -> Unit,
    onSincronizarClick: () -> Unit,
    onLimpiarMensaje: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Equipos") },
                actions = {
                    TextButton(onClick = onLogoutClick) {
                        Text("Salir", color = Color.Red)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = onVerSolicitudesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Text("Mis Solicitudes")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSincronizarClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sincronizar Catálogo con Nube", color = MaterialTheme.colorScheme.primary)
                }

                if (mensajeExito != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensajeExito, color = Color(0xFF4CAF50), fontSize = 14.sp)
                }
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensajeError, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            items(equipos) { equipo ->
                val colorEstado = when (equipo.estado) {
                    EstadoEquipo.DISPONIBLE -> Color(0xFF4CAF50)
                    EstadoEquipo.RESERVADO -> Color(0xFFFF9800)
                    EstadoEquipo.PRESTADO -> Color(0xFFF44336)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onEquipoClick(equipo.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Categoría: ${equipo.categoria}")
                        Text(text = "Estado: ${equipo.estado}", color = colorEstado)
                    }
                }
            }
        }
    }
}
