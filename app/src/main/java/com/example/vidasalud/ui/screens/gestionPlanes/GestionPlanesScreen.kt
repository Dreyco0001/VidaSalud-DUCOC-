package com.example.vidasalud.ui.screens.gestionPlanes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vidasalud.model.Plan
import com.example.vidasalud.viewmodel.GestionPlanesViewModel

@Composable
fun GestionPlanesScreen(
    viewModel: GestionPlanesViewModel = viewModel(),
    onVolver: () -> Unit
) {
    val planes by viewModel.planes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var planAEditar by remember { mutableStateOf<Plan?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoCrear = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar plan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column {
                    Text(
                        "Gestión de Planes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${planes.size} plan(es) cargado(s)", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

            error?.let { Text(it, color = Color.Red) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(planes) { plan ->
                    PlanItem(
                        plan = plan,
                        onEditar = { planAEditar = plan },
                        onEliminar = { viewModel.eliminarPlan(plan.id) }
                    )
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoPlan(
            titulo = "Crear Plan",
            plan = null,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nombre, duracion, nivel, objetivo, imagenUrl ->
                viewModel.crearPlan(
                    nombre,
                    duracion,
                    nivel,
                    objetivo,
                    imagenUrl // <-- URL directa, sin bytes
                )
                mostrarDialogoCrear = false
            }
        )
    }

    planAEditar?.let { plan ->
        DialogoPlan(
            titulo = "Editar Plan",
            plan = plan,
            onDismiss = { planAEditar = null },
            onGuardar = { nombre, duracion, nivel, objetivo, imagenUrl ->
                viewModel.actualizarPlan(
                    plan.id,
                    nombre,
                    duracion,
                    nivel,
                    objetivo,
                    imagenUrl // <-- URL directa
                )
                planAEditar = null
            }
        )
    }
}

@Composable
fun PlanItem(
    plan: Plan,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF2F7))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (plan.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = plan.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(plan.nombre, fontWeight = FontWeight.Bold)
                Text("Duración: ${plan.duracion} min")
                Text("Nivel: ${plan.nivel}")
                Text("Objetivo: ${plan.objetivo}")
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}

@Composable
fun DialogoPlan(
    titulo: String,
    plan: Plan? = null,
    onDismiss: () -> Unit,
    onGuardar: (String, Int, String, String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(plan?.nombre ?: "") }
    var duracion by remember { mutableStateOf(plan?.duracion?.toString() ?: "") }
    var nivel by remember { mutableStateOf(plan?.nivel ?: "") }
    var objetivo by remember { mutableStateOf(plan?.objetivo ?: "") }
    var imagenUrl by remember { mutableStateOf(plan?.imagenUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        confirmButton = {
            Button(onClick = {
                onGuardar(
                    nombre,
                    duracion.toIntOrNull() ?: 0,
                    nivel,
                    objetivo,
                    imagenUrl.ifBlank { null } // <-- URL o null
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = duracion, onValueChange = { duracion = it }, label = { Text("Duración (min)") })
                OutlinedTextField(value = nivel, onValueChange = { nivel = it }, label = { Text("Nivel") })
                OutlinedTextField(value = objetivo, onValueChange = { objetivo = it }, label = { Text("Objetivo") })
                OutlinedTextField(value = imagenUrl, onValueChange = { imagenUrl = it }, label = { Text("URL de imagen (opcional)") })
            }
        }
    )
}
