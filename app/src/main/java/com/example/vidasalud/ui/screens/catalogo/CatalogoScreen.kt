package com.example.vidasalud.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.vidasalud.model.NotificacionPlan
import com.example.vidasalud.model.Plan
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal
import com.example.vidasalud.viewmodel.GestionPlanesViewModel
import com.example.vidasalud.viewmodel.NotificacionesViewModel

@Composable
fun CatalogoScreen(
    navController: NavController,
    nombre: String,
    rol: String,
    planesViewModel: GestionPlanesViewModel = viewModel(),
    notificacionesViewModel: NotificacionesViewModel = viewModel()
) {
    val planes by planesViewModel.planes.collectAsState()
    val cargando by planesViewModel.isLoading.collectAsState()

    val notificaciones by notificacionesViewModel.notificaciones.collectAsState()
    val error by notificacionesViewModel.error.collectAsState()

    var planSeleccionado by remember { mutableStateOf<Plan?>(null) }
    var notificacionEliminar by remember { mutableStateOf<NotificacionPlan?>(null) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mostrarNotificaciones by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        notificacionesViewModel.cargarNotificaciones(rol)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        /* ================= TOP BAR ================= */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("VidaSalud 💙", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            IconButton(onClick = { mostrarNotificaciones = !mostrarNotificaciones }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
            }
        }

        /* ================= NOTIFICACIONES ================= */
        if (mostrarNotificaciones) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6FA))
            ) {
                Column(Modifier.padding(12.dp)) {

                    if (rol == "admin") {
                        Text(
                            "🛡️ Vista Admin – todas las notificaciones",
                            fontSize = 13.sp,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    Text("Planes Tomados", fontWeight = FontWeight.Bold)

                    if (notificaciones.isEmpty()) {
                        Text("No hay planes registrados", color = Color.Gray)
                    } else {
                        notificaciones.forEach { noti ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        noti.nombrePlan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Plan por 1 día · $${noti.precio}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )

                                    if (rol == "admin") {
                                        Text(
                                            "Usuario: ${noti.nombreUsuario}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                TextButton(onClick = {
                                    notificacionEliminar = noti
                                }) {
                                    Text("Eliminar", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        /* ================= PLANES ================= */
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(planes) { plan ->
                    PlanCard(plan = plan) {
                        planSeleccionado = plan
                        mostrarConfirmacion = true
                    }
                }
            }
        }

        BarraNavegacionPrincipal(
            navController = navController,
            nombreUsuario = nombre,
            rolUsuario = rol,
            rutaActual = "catalogo/{nombre}/{rol}"
        )
    }

    /* ================= CONFIRMAR PLAN ================= */
    planSeleccionado?.let { plan ->
        if (mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacion = false },
                title = { Text("Confirmar Plan") },
                text = {
                    Column {
                        Text(plan.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("📅 Plan por 1 día", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text(plan.objetivo)
                        Spacer(Modifier.height(6.dp))
                        Text("🍽 ${plan.comidaRecomendada}")
                        Text("Precio: $${plan.precio}")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        mostrarConfirmacion = false
                        notificacionesViewModel.tomarPlan(
                            plan = plan,
                            rolUsuario = rol
                        ) {
                            mensajeResultado = "✅ Plan tomado con éxito"
                            notificacionesViewModel.cargarNotificaciones(rol)
                        }
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacion = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    /* ================= CONFIRMAR ELIMINAR ================= */
    notificacionEliminar?.let { noti ->
        AlertDialog(
            onDismissRequest = { notificacionEliminar = null },
            title = { Text("Eliminar notificación") },
            text = { Text("¿Deseas eliminar este plan tomado?") },
            confirmButton = {
                Button(onClick = {
                    notificacionesViewModel.eliminarNotificacion(
                        id = noti.id,
                        rol = rol,
                        onOk = {
                            mensajeResultado = "🗑️ Notificación eliminada"
                        }
                    )
                    notificacionEliminar = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { notificacionEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    /* ================= RESULTADOS ================= */
    mensajeResultado?.let {
        AlertDialog(
            onDismissRequest = { mensajeResultado = null },
            confirmButton = {
                TextButton(onClick = { mensajeResultado = null }) {
                    Text("OK")
                }
            },
            title = { Text("Estado") },
            text = { Text(it) }
        )
    }

    /* ================= ERRORES ================= */
    error?.let {
        AlertDialog(
            onDismissRequest = { notificacionesViewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { notificacionesViewModel.clearError() }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text(it) }
        )
    }
}

/* ================= CARD PLAN ================= */
@Composable
fun PlanCard(
    plan: Plan,
    onReservar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = plan.imagenUrl,
                contentDescription = plan.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {
                Text(plan.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("📅 Plan por 1 día", color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(plan.objetivo)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$${plan.precio}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(onClick = onReservar) {
                        Text("Tomar Plan")
                    }
                }
            }
        }
    }
}
