package com.example.prestamolab.ui.evidencias

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.prestamolab.PrestamoLabApp
import com.example.prestamolab.data.evidencias.FileProviderAlmacenFotos
import com.example.prestamolab.model.EtapaEvidencia
import com.example.prestamolab.model.Evidencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Conecta la pantalla con la cámara: el permiso se pide solo al tocar "Adjuntar evidencia" (CA-HU08-01). */
@Composable
fun EvidenciasRoute(viewModel: EvidenciasViewModel, solicitudId: Int, etapa: EtapaEvidencia, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), viewModel::onFotoTomada)
    val abrirCamara = {
        try {
            camara.launch(Uri.parse(viewModel.prepararFoto()))
        } catch (e: ActivityNotFoundException) {
            viewModel.onCamaraNoDisponible()
        }
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else viewModel.onPermisoCamaraDenegado()
    }

    EvidenciasScreen(
        solicitudId = solicitudId,
        etapa = etapa,
        uiState = uiState,
        onAdjuntarClick = {
            val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            if (concedido) abrirCamara() else permiso.launch(Manifest.permission.CAMERA)
        },
        onVolver = onVolver
    )
}

@Composable
fun EvidenciasScreen(
    solicitudId: Int,
    etapa: EtapaEvidencia,
    uiState: EvidenciasUiState,
    onAdjuntarClick: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onVolver) { Text("← Atrás", fontFamily = FontFamily.SansSerif) }
        Text(
            "Evidencias del préstamo #$solicitudId",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F2537)
        )
        Text("Fotos del estado del equipo ${etapa.etiqueta}.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onAdjuntarClick, modifier = Modifier.fillMaxWidth()) {
            Text("Adjuntar evidencia", fontFamily = FontFamily.SansSerif)
        }
        uiState.mensaje?.let {
            Text(
                it,
                color = if (uiState.esError) MaterialTheme.colorScheme.error else Color(0xFF1B5E20),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (uiState.evidencias.isEmpty()) {
            Text("Aún no hay evidencias en este préstamo.", fontFamily = FontFamily.SansSerif)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.evidencias, key = { it.id }) { FilaEvidencia(it) }
            }
        }
    }
}

@Composable
private fun FilaEvidencia(evidencia: Evidencia) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FA))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Miniatura(evidencia.uriLocal)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (evidencia.etapa == EtapaEvidencia.ENTREGA) "Entrega" else "Devolución",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(evidencia.fecha, style = MaterialTheme.typography.bodySmall)
                Text(
                    if (evidencia.urlRemota != null) "Subida a Supabase" else "Pendiente de subir",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun Miniatura(uri: String) {
    val contexto = LocalContext.current
    val almacen = (contexto.applicationContext as PrestamoLabApp).container.almacenFotos
    val imagen by produceState<Bitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) { (almacen as? FileProviderAlmacenFotos)?.miniatura(uri) }
    }
    val modificador = Modifier
        .size(64.dp)
        .clip(RoundedCornerShape(8.dp))
    imagen?.let {
        Image(it.asImageBitmap(), contentDescription = "Foto de evidencia", contentScale = ContentScale.Crop, modifier = modificador)
    } ?: Box(modificador.background(Color(0xFFD5E3EA)))
}
