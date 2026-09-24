package com.example.prestamolab.ui.auth

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onIdentificadorChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onIngresarClick: () -> Unit
) {
    val blueAccent = Color(0xFF1E6091)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PréstamoLab CTMA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F2537)
        )
        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.SansSerif
        )

        OutlinedTextField(
            value = uiState.identificador,
            onValueChange = onIdentificadorChange,
            label = { Text("Correo o documento", fontFamily = FontFamily.SansSerif) },
            isError = uiState.errorIdentificador != null,
            supportingText = uiState.errorIdentificador?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = uiState.contrasena,
            onValueChange = onContrasenaChange,
            label = { Text("Contraseña", fontFamily = FontFamily.SansSerif) },
            isError = uiState.errorContrasena != null,
            supportingText = uiState.errorContrasena?.let { { Text(it) } },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        if (uiState.mensajeError != null) {
            Text(
                text = uiState.mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif
            )
        }

        Button(
            onClick = onIngresarClick,
            enabled = !uiState.cargando,
            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (uiState.cargando) "Ingresando..." else "Ingresar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
        }
    }
}
