package com.example.vidasalud.ui.screens.pago

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun PagoConfirmacionScreen(
    nombreUsuario: String = "Cliente", // Recibir el nombre del usuario
    onVolverAlPerfil: () -> Unit = {} // Cambiar nombre del parámetro
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "¡Pago Confirmado!",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Tu pedido ha sido procesado exitosamente",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Recibirás una confirmación por correo electrónico",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVolverAlPerfil,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text("Volver al Perfil de $nombreUsuario")
        }
    }
}