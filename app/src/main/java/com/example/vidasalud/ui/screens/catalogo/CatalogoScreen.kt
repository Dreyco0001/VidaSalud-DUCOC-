package com.example.vidasalud.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vidasalud.model.Plan
import com.example.vidasalud.repository.PlanesRepository
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal

@Composable
fun CatalogoScreen(
    navController: NavController,
    nombre: String = "Cliente",
    rol: String = "cliente",
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
                onClick = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
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
        BarraNavegacionPrincipal(
            navController = navController,
            nombreUsuario = nombre,
            rolUsuario = rol,
            rutaActual = "registroDatos/{nombre}/{rol}"
        )
    }
}
