package com.example.vidasalud.ui.screens.compartido

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class ItemNavegacion(
    val etiqueta: String,
    val icono: ImageVector,
    val plantillaRuta: String
)

@Composable
fun BarraNavegacionPrincipal(
    navController: NavController,
    nombreUsuario: String,
    rolUsuario: String,
    rutaActual: String
) {
    val items = listOf(
        ItemNavegacion("Inicio", Icons.Filled.Home, "home/{nombre}/{rol}"),
        ItemNavegacion("Registrar", Icons.Filled.AddCircleOutline, "registroDatos/{nombre}/{rol}"),
        ItemNavegacion("Comunidad", Icons.Filled.Group, "feed/{nombre}/{rol}"),
        ItemNavegacion("Perfil", Icons.Filled.Person, "perfil/{nombre}/{rol}")
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val rutaReal = item.plantillaRuta
                    .replace("{nombre}", nombreUsuario)
                    .replace("{rol}", rolUsuario)
                
                val estaSeleccionado = item.plantillaRuta == rutaActual

                VistaItemNavegacion(item = item, estaSeleccionado = estaSeleccionado) {
                    if (item.plantillaRuta != rutaActual) {
                        navController.navigate(rutaReal) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VistaItemNavegacion(item: ItemNavegacion, estaSeleccionado: Boolean, onClick: () -> Unit) {
    val colorIcono = if (estaSeleccionado) Color.White else Color.DarkGray.copy(alpha = 0.6f)
    val colorTexto = if (estaSeleccionado) MaterialTheme.colorScheme.primary else Color.Gray
    val fondo = if (estaSeleccionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(fondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icono,
                contentDescription = item.etiqueta,
                tint = colorIcono,
                modifier = Modifier.size(24.dp)
            )
        }
        // espacio entre icono y texto
        Spacer(modifier = Modifier.height(2.dp)) 
        Text(
            text = item.etiqueta,
            color = colorTexto,
            fontSize = 12.sp,
            fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal
        )
    }
}
