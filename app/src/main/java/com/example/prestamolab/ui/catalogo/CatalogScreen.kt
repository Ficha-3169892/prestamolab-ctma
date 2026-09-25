package com.example.prestamolab.ui.catalogo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.prestamolab.model.Equipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.FiltroCatalogo

/** CA-HU01-02: un equipo no disponible se distingue del disponible por el color de su estado. */
fun colorEstado(estado: EstadoEquipo): Color = when (estado) {
    EstadoEquipo.DISPONIBLE -> Color(0xFF1B5E20)
    EstadoEquipo.RESERVADO -> Color(0xFFB26A00)
    EstadoEquipo.PRESTADO -> Color(0xFFB3261E)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    equipos: List<Equipo>,
    cargando: Boolean,
    filtro: FiltroCatalogo,
    categorias: List<String>,
    onSoloDisponiblesChange: (Boolean) -> Unit,
    onCategoriaSeleccionada: (String?) -> Unit,
    onQuitarFiltros: () -> Unit,
    onEquipoClick: (Int) -> Unit,
    onNavigateToSolicitudes: (() -> Unit)?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Equipos - PréstamoLab") },
                actions = {
                    if (onNavigateToSolicitudes != null) {
                        TextButton(onClick = onNavigateToSolicitudes) {
                            Text("Mis Solicitudes")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // CA-HU01-03: "Solo disponibles" y una categoría a la vez. Los chips pasan a otra línea en vez de
            // desplazarse: así ninguna categoría queda oculta fuera de la pantalla
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtro.soloDisponibles,
                    onClick = { onSoloDisponiblesChange(!filtro.soloDisponibles) },
                    label = { Text("Solo disponibles") }
                )
                FilterChip(
                    selected = filtro.categoria == null,
                    onClick = { onCategoriaSeleccionada(null) },
                    label = { Text("Todas") }
                )
                categorias.forEach { categoria ->
                    FilterChip(
                        selected = filtro.categoria == categoria,
                        onClick = { onCategoriaSeleccionada(categoria) },
                        label = { Text(categoria) }
                    )
                }
            }

            if (cargando) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Cargando catálogo…", style = MaterialTheme.typography.bodyLarge)
                }
                return@Column
            }

            if (equipos.isEmpty()) {
                // CA-HU01-05: mensaje en lugar de una lista vacía
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No hay equipos para mostrar", style = MaterialTheme.typography.bodyLarge)
                    if (filtro.activo) {
                        TextButton(onClick = onQuitarFiltros) { Text("Quitar filtros") }
                    }
                }
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(equipos, key = { it.id }) { equipo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEquipoClick(equipo.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Categoría: ${equipo.categoria}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estado: ${equipo.estado}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorEstado(equipo.estado),
                                // Quien no distingue colores (o un lector de pantalla) recibe el mismo dato
                                modifier = Modifier.semantics {
                                    contentDescription = if (equipo.estado == EstadoEquipo.DISPONIBLE) {
                                        "Estado: ${equipo.estado}, disponible"
                                    } else {
                                        "Estado: ${equipo.estado}, no disponible"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
