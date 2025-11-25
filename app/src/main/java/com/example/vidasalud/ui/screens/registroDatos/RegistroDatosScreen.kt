package com.example.vidasalud.ui.screens.registroDatos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatosScreen(navController: NavController, userName: String, userRole: String) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraNavegacionPrincipal(
                navController = navController,
                nombreUsuario = userName,
                rolUsuario = userRole,
                rutaActual = "registroDatos/{nombre}/{rol}"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrar",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // contenido real de aqui pa abajo
        }
    }
}
