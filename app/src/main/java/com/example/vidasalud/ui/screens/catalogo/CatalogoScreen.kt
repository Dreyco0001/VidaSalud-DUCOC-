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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vidasalud.model.Plan
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal
import com.example.vidasalud.viewmodel.GestionPlanesViewModel

@Composable
fun CatalogoScreen(
    navController: NavController,
    nombre: String = "Cliente",
    rol: String = "cliente",
    viewModel: GestionPlanesViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }

    val planes by viewModel.planes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // ------------------------------
        // TOP BAR
        // ------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("VidaSalud 💙", fontSize = 24.sp, fontWeight = FontWeight.Bold)
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

        // ------------------------------
        // LOADING
        // ------------------------------
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            // ------------------------------
            // LISTA DE PLANES
            // ------------------------------
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Planes de Ejercicio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(planes) { plan ->
                    PlanCard(plan = plan, onReserveClick = {
                        selectedPlan = it
                        showDialog = true
                    })
                }
            }
        }

        // ------------------------------
        // NAV BAR
        // ------------------------------
        BarraNavegacionPrincipal(
            navController = navController,
            nombreUsuario = nombre,
            rolUsuario = rol,
            rutaActual = "catalogo/{nombre}/{rol}"
        )
    }

    if (showDialog && selectedPlan != null) {
        ReservationDialog(
            activityName = selectedPlan!!.nombre,
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                // Lógica de reserva real la agregas luego
            }
        )
    }
}

@Composable
fun PlanCard(plan: Plan, onReserveClick: (Plan) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {

            // Imagen desde URL usando Coil
            AsyncImage(
                model = plan.imagenUrl,
                contentDescription = "Imagen de ${plan.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {

                Text(plan.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Duración: ${plan.duracion} minutos", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Nivel: ${plan.nivel}", fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))
                Text(plan.objetivo, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Precio + Botón reservar
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "$${plan.precio}",
                        modifier = Modifier.align(Alignment.CenterStart),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { onReserveClick(plan) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Reservar")
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationDialog(activityName: String, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var quantity by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservar: $activityName") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Cuántas clases deseas reservar?")
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Reducir cantidad")
                    }
                    Text(quantity.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (quantity < 5) quantity++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity) }) {
                Text("Confirmar reserva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
