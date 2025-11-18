package com.example.vidasalud.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.model.Plan
import com.example.vidasalud.repository.PlanesRepository
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

    val repo = remember { PlanesRepository() }

    // 🔥 LISTA REAL DE PLANES DESDE FIREBASE
    var planes by remember { mutableStateOf<List<Plan>>(emptyList()) }
    var cargandoPlanes by remember { mutableStateOf(true) }

    // 🔥 ESCUCHA EN TIEMPO REAL
    LaunchedEffect(Unit) {
        repo.escucharPlanes { lista ->
            planes = lista
            cargandoPlanes = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // ================ HEADER =====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "VidaSalud 💙",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cerrar sesión", color = Color.White)
            }
        }

        // ================ CONTENIDO =====================
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {

            Text(
                "Planes de Ejercicio",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (cargandoPlanes) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(planes) { plan ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF03A9F4).copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Text(plan.nombre, fontWeight = FontWeight.Bold)
                                Text("Duración: ${plan.duracion} min")
                                Text("Nivel: ${plan.nivel}")
                                Text("Objetivo: ${plan.objetivo}")

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { /* Ver Plan */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF03A9F4)
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Ver Plan")
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================ FOOTER =====================
        NavigationBar(
            containerColor = Color(0xFF03A9F4).copy(alpha = 0.1f)
        ) {

            NavigationBarItem(
                selected = false,
                onClick = onVerPerfil,
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                label = { Text("Perfil") }
            )

            NavigationBarItem(
                selected = false,
                onClick = onVerCarrito,
                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Planes") },
                label = { Text("Planes") }
            )

            NavigationBarItem(
                selected = false,
                onClick = { /* animación visual */ },
                icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calorías") },
                label = { Text("Calorías") }
            )
        }
    }
}
