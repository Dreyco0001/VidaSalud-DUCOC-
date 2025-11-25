package com.example.vidasalud.ui.screens.registroDatos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatosScreen(navController: NavController, userName: String, userRole: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registros") }, // Texto cambiado
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            BarraNavegacionPrincipal(
                navController = navController,
                nombreUsuario = userName,
                rolUsuario = userRole,
                rutaActual = "registroDatos/{nombre}/{rol}"
            )
        }
    ) { paddingValues ->
        // placeholder eliminado
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // contenido real de aqui pa abajo
        }
    }
}
