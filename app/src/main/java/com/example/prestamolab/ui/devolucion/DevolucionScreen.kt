package com.example.prestamolab.ui.devolucion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.prestamolab.model.CondicionEquipo
import java.util.Locale

private val PERMISOS_UBICACION = arrayOf(
    // Android 12+ exige pedir FINE junto con COARSE; el usuario puede conceder solo la aproximada
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun tienePermisoUbicacion(context: Context) = PERMISOS_UBICACION.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

/** Conecta la pantalla con el ViewModel y gestiona el permiso de ubicación en tiempo de ejecución. */
@Composable
fun DevolucionRoute(viewModel: DevolucionViewModel, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // El permiso se pide solo cuando el usuario toca el botón (mínimo privilegio)
    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) viewModel.capturarUbicacion() else viewModel.onPermisoUbicacionDenegado()
    }

    LaunchedEffect(uiState.completada) {
        if (uiState.completada) onVolver()
    }

    DevolucionScreen(
        uiState = uiState,
        onCondicionSeleccionada = viewModel::onCondicionSeleccionada,
        onObservacionChange = viewModel::onObservacionChanged,
        onCapturarUbicacionClick = {
            if (tienePermisoUbicacion(context)) viewModel.capturarUbicacion()
            else solicitarPermiso.launch(PERMISOS_UBICACION)
        },
        onConfirmarClick = viewModel::registrarDevolucion,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevolucionScreen(
    uiState: DevolucionUiState,
    onCondicionSeleccionada: (CondicionEquipo) -> Unit,
    onObservacionChange: (String) -> Unit,
    onCapturarUbicacionClick: () -> Unit,
    onConfirmarClick: () -> Unit,
    onVolver: () -> Unit
) {
    val blueAccent = Color(0xFF1E6091)
    val blueCardBg = Color(0xFFF0F7FA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Sin Scaffold: la app es de borde a borde y la barra de navegación tapaba "Confirmar devolución"
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver) {
                Text("Atrás", color = blueAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontSize = 16.sp)
            }
            Text(
                text = "Registrar Devolución",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F2537)
            )
        }

        val solicitud = uiState.solicitud
        when {
            uiState.cargando -> CircularProgressIndicator()
            solicitud == null -> Text("Préstamo no encontrado", style = MaterialTheme.typography.bodyLarge)
            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = blueCardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Préstamo #${solicitud.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Equipo: ${uiState.nombreEquipo}", fontFamily = FontFamily.SansSerif)
                        Text("Estado: ${solicitud.estado}", color = blueAccent, fontWeight = FontWeight.Bold)
                    }
                }

                if (!uiState.puedeDevolverse) {
                    Text(
                        text = "Solo se pueden devolver préstamos en estado PRESTADO.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Estado del equipo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CondicionEquipo.entries.forEach { condicion ->
                            FilterChip(
                                selected = uiState.condicion == condicion,
                                onClick = { onCondicionSeleccionada(condicion) },
                                label = { Text(condicion.etiqueta) }
                            )
                        }
                    }
                    uiState.errorCondicion?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = uiState.observacion,
                        onValueChange = onObservacionChange,
                        label = { Text("Observaciones", fontFamily = FontFamily.SansSerif) },
                        isError = uiState.errorObservacion != null,
                        supportingText = uiState.errorObservacion?.let { { Text(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )

                    SeccionUbicacion(uiState, onCapturarUbicacionClick)

                    uiState.mensajeError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    Button(
                        onClick = onConfirmarClick,
                        enabled = !uiState.guardando,
                        colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (uiState.guardando) "Procesando..." else "Confirmar devolución",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionUbicacion(uiState: DevolucionUiState, onCapturarUbicacionClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F1F5))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ubicación de la devolución (GPS)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = onCapturarUbicacionClick,
                enabled = !uiState.capturandoUbicacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.capturandoUbicacion) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Obteniendo ubicación...")
                } else {
                    Text(if (uiState.ubicacion == null) "Capturar ubicación actual" else "Volver a capturar ubicación")
                }
            }
            uiState.mensajeUbicacion?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = if (uiState.ubicacion != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            uiState.ubicacion?.let { ubicacion ->
                Text(
                    text = String.format(Locale.US, "Lat: %.6f, Lng: %.6f", ubicacion.latitud, ubicacion.longitud),
                    fontFamily = FontFamily.Monospace
                )
                ubicacion.precisionMetros?.let {
                    Text(String.format(Locale.US, "Precisión: ±%.0f m", it), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
