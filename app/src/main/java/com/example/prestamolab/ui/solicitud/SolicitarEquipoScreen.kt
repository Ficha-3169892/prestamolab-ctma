package com.example.prestamolab.ui.solicitud

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.example.prestamolab.model.Equipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarEquipoScreen(
    equipo: Equipo?,
    guardando: Boolean,
    mensajeError: String?,
    onGuardar: (String, String, Int) -> Unit,
    onVolver: () -> Unit
) {
    var ambiente by remember { mutableStateOf("") }
    var proposito by remember { mutableStateOf("") }
    var duracionTexto by remember { mutableStateOf("1") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registrar Solicitud") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (equipo == null) {
                Text("Equipo no válido")
                Button(onClick = onVolver) { Text("Volver") }
            } else {
                Text(text = "Solicitando: ${equipo.nombre}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Destino integrado con el GPS como botón al interior (trailing icon)
                CampoDestinoConUbicacion(
                    ambiente = ambiente,
                    onAmbienteChange = { nuevoAmbiente -> ambiente = nuevoAmbiente }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = proposito,
                    onValueChange = { proposito = it },
                    label = { Text("Propósito (10-180 caract.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duracionTexto,
                    onValueChange = { duracionTexto = it },
                    label = { Text("Duración estimada (Horas 1-8)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (mensajeError != null) {
                    Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f, fill = false))

                Button(
                    onClick = {
                        val duracion = duracionTexto.toIntOrNull() ?: 0
                        onGuardar(ambiente, proposito, duracion)
                    },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar Solicitud")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun CampoDestinoConUbicacion(
    ambiente: String,
    onAmbienteChange: (String) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        onAmbienteChange("Lat: " + location.latitude + ", Lon: " + location.longitude)
                    } else {
                        onAmbienteChange("Ubicación no encontrada (Enciende el GPS)")
                    }
                }
            } catch (_: SecurityException) { }
        }
    }

    val obtenerGpsClick: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        onAmbienteChange("Lat: " + location.latitude + ", Lon: " + location.longitude)
                    }
                }
            } catch (_: SecurityException) { }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    OutlinedTextField(
        value = ambiente,
        onValueChange = onAmbienteChange,
        label = { Text("Ambiente o Destino") },
        trailingIcon = {
            TextButton(
                onClick = obtenerGpsClick,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("GPS", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
