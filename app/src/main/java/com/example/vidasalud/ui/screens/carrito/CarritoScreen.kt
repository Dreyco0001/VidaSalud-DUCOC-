package com.example.vidasalud.ui.screens.carrito

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.vidasalud.model.Plan
import com.example.vidasalud.viewmodel.GestionPlanesViewModel

@Composable
fun CarritoScreen(
    viewModel: GestionPlanesViewModel,
    nombre: String = "Usuario",
    rol: String = "cliente",
    onVerPerfil: () -> Unit = {},
    onLogout: () -> Unit = {},
    onVerDetallePlan: (Plan) -> Unit = {},
    onModificarPlan: (Plan) -> Unit = {},
    onVolverAlCatalogo: () -> Unit = {}
) {
    val planes by viewModel.planes.collectAsState()

    // Cargar planes solo una vez
    LaunchedEffect(true) { viewModel.obtenerPlanes() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        TextButton(onClick = onVolverAlCatalogo) {
            Text("← Volver al Catálogo", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                Text("Hola, $nombre", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }

            Row {
                IconButton(onClick = onVerPerfil) { Icon(Icons.Default.Person, contentDescription = null) }
                IconButton(onClick = onLogout) { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.Red) }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Planes de Ejercicio Saludables",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(planes, key = { it.id }) { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                        // Imagen opcional
                        plan.imagenUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = plan.nombre,
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        Text(plan.nombre ?: "Sin nombre", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Duración: ${plan.duracion} min")
                        Text("Nivel: ${plan.nivel}")
                        Text("Objetivo: ${plan.objetivo}", color = Color.DarkGray)
                        Spacer(Modifier.height(8.dp))

                        if (rol.lowercase() == "cliente") {
                            Button(
                                onClick = { onVerDetallePlan(plan) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Iniciar Plan")
                            }
                        } else if (rol.lowercase() == "admin") {
                            Button(
                                onClick = { onModificarPlan(plan) },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Modificar Plan")
                            }
                        }
                    }
                }
            }
        }
    }
}
//aaaaa