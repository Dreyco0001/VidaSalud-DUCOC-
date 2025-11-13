package com.example.vidasalud.ui.screens.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PlanEjercicio(
    val id: Int,
    val nombre: String,
    val duracion: String,
    val nivel: String,
    val objetivo: String,
    val color: Color
)

@Composable
fun CarritoScreen(
    nombre: String = "Usuario",
    rol: String = "cliente", // 🔹 Nuevo parámetro para controlar el rol
    onVerPerfil: () -> Unit = {},
    onLogout: () -> Unit = {},
    onVerDetallePlan: (PlanEjercicio) -> Unit = {},
    onModificarPlan: (PlanEjercicio) -> Unit = {}, // 🔹 Nuevo callback para admins
    onVolverAlCatalogo: () -> Unit = {}
) {
    val planes = remember {
        listOf(
            PlanEjercicio(1, "Cardio Vital", "30 min", "Intermedio", "Mejorar resistencia", Color(0xFFFF9800)),
            PlanEjercicio(2, "Fuerza Total", "45 min", "Avanzado", "Aumentar masa muscular", Color(0xFF4CAF50)),
            PlanEjercicio(3, "Yoga Zen", "25 min", "Básico", "Reducir estrés y mejorar flexibilidad", Color(0xFF9C27B0)),
            PlanEjercicio(4, "Rutina Express", "15 min", "Básico", "Activar cuerpo rápidamente", Color(0xFF03A9F4))
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 🔹 BOTÓN VOLVER
        TextButton(onClick = onVolverAlCatalogo) {
            Text("← Volver al Catálogo", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = "Salud", tint = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hola, $nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVerPerfil) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = "Cerrar Sesión", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Título principal
        Text(
            "Planes de Ejercicio Saludables",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Lista de planes
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(planes, key = { it.id }) { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = plan.color.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(plan.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Duración: ${plan.duracion}")
                        Text("Nivel: ${plan.nivel}")
                        Text("Objetivo: ${plan.objetivo}", color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (rol.lowercase() == "cliente") {
                            Button(
                                onClick = { onVerDetallePlan(plan) },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = plan.color)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Iniciar Plan")
                            }
                        } else if (rol.lowercase() == "admin" || rol.lowercase() == "administrador") {
                            Button(
                                onClick = { onModificarPlan(plan) },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modificar")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modificar Plan")
                            }
                        }
                    }
                }
            }
        }
    }
}
