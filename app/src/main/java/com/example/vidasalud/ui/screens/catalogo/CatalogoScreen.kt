package com.example.vidasalud.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.repository.PlanEjercicio
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

    // --- 🔥 IMPORTANTE: imports correctos ---
    val productos by viewModel.productos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val carrito by viewModel.carrito.collectAsState()

    // --- 🔥 REPO ESTABLE ---
    val repo = remember { PlanesRepository() }

    // --- 🔥 ESTADOS DE LOS PLANES ---
    var planes by remember { mutableStateOf<List<PlanEjercicio>>(emptyList()) }
    var cargandoPlanes by remember { mutableStateOf(true) }

    // --- 🔥 CARGA REAL DESDE FIREBASE ---
    LaunchedEffect(Unit) {
        cargandoPlanes = true
        planes = repo.obtenerPlanes()
        cargandoPlanes = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // HEADER -------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hola, $nombre 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onVerPerfil,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) { Text("Perfil") }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Cerrar sesión") }

                Spacer(modifier = Modifier.width(8.dp))

                BadgedBox(
                    badge = {
                        if (carrito.isNotEmpty()) {
                            Badge { Text(carrito.sumOf { it.cantidad }.toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onVerCarrito) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = "Carrito")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LISTA DE PLANES -----------------------------------------------
        Text("Planes de Ejercicio", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                                onClick = { /* Acción */ },
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

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓN DE ACCESO A PLANES --------------------------------------
        Button(
            onClick = onVerCarrito,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = "")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Planes de Ejercicio", fontWeight = FontWeight.Bold)
        }
    }
}
