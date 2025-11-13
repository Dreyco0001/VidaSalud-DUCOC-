package com.example.vidasalud.ui.screens.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.viewmodel.CarritoViewModel

@Composable
fun CatalogoScreen(
    viewModel: CarritoViewModel = viewModel(),
    nombre: String = "Cliente",
    rol: String = "cliente",
    onVerPerfil: () -> Unit = {},
    onLogout: () -> Unit = {},
    onVerCarrito: () -> Unit = {}
) {
    val productos by viewModel.productos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val carrito by viewModel.carrito.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔹 HEADER principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hola, $nombre 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onVerPerfil,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) { Text("Perfil", fontSize = 14.sp) }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Cerrar sesión", fontSize = 14.sp) }

                Spacer(modifier = Modifier.width(8.dp))

                BadgedBox(
                    badge = {
                        if (carrito.isNotEmpty()) {
                            Badge { Text(carrito.sumOf { it.cantidad }.toString()) }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onVerCarrito,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = "Planes de ejercicio")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
// 🔹 Plan de ejercicio de prueba
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF03A9F4).copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Plan de prueba: Cardio Express", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Duración: 20 min")
                Text("Nivel: Básico")
                Text("Objetivo: Activar cuerpo rápidamente", color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                if (rol.lowercase() == "cliente") {
                    Button(
                        onClick = { /* acción de iniciar plan */ },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
                    ) {
                        Text("Iniciar Plan")
                    }
                } else if (rol.lowercase() == "admin" || rol.lowercase() == "administrador") {
                    Button(
                        onClick = { /* acción de modificar plan */ },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                    ) {
                        Text("Modificar Plan")
                    }
                }
            }
        }


        // 🔹 Botón para ver planes de ejercicio
        Button(
            onClick = onVerCarrito,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = "Planes de ejercicio",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Planes de Ejercicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Lista de productos (placeholder para futuros datos)
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos) { producto ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$${producto.precio}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Stock: ${producto.stock}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = { viewModel.agregarAlCarrito(producto) }) {
                                Text("Agregar al carrito")
                            }
                        }
                    }
                }
            }
        }
    }
}
