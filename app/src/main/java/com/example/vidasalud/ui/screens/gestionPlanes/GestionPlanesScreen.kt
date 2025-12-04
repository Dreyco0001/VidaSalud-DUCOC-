package com.example.vidasalud.ui.screens.gestionPlanes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
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

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            error?.let { msg ->
                Text(msg, color = Color.Red)
            }

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

    // DIALOGO CREAR
    if (mostrarDialogoCrear) {
        DialogoPlan(
            titulo = "Crear Plan",
            plan = null,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nombre, duracion, nivel, objetivo, imagenUrl, precio ->
                viewModel.crearPlan(
                    nombre = nombre,
                    duracion = duracion,
                    nivel = nivel,
                    objetivo = objetivo,
                    imagenUrl = imagenUrl,
                    precio = precio
                )
                mostrarDialogoCrear = false
            }
        )
    }

    // DIALOGO EDITAR
    planAEditar?.let { plan ->
        DialogoPlan(
            titulo = "Editar Plan",
            plan = plan,
            onDismiss = { planAEditar = null },
            onGuardar = { nombre, duracion, nivel, objetivo, imagenUrl, precio ->
                viewModel.actualizarPlan(
                    id = plan.id,
                    nombre = nombre,
                    duracion = duracion,
                    nivel = nivel,
                    objetivo = objetivo,
                    imagenUrl = imagenUrl,
                    precio = precio
                )
                planAEditar = null
            }
        )
    }
}

// -------------------------------------------------------------

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

            if (plan.imagenUrl.isNotBlank()) {
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
                Text("Precio: ${formatPrecio(plan.precio)}")
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

// Helper para mostrar precio como $xx.xxx
private fun formatPrecio(precio: Int): String {
    return "$" + "%,d".format(precio).replace(',', '.')
}

// -------------------------------------------------------------

@Composable
fun DialogoPlan(
    titulo: String,
    plan: Plan? = null,
    onDismiss: () -> Unit,
    onGuardar: (String, Int, String, String, String?, Int) -> Unit
) {
    // Work with Strings in the TextFields
    var nombre by remember { mutableStateOf(plan?.nombre ?: "") }
    var duracionStr by remember { mutableStateOf(plan?.duracion?.toString() ?: "") }
    var nivel by remember { mutableStateOf(plan?.nivel ?: "") }
    var objetivo by remember { mutableStateOf(plan?.objetivo ?: "") }
    var imagenUrl by remember { mutableStateOf(plan?.imagenUrl ?: "") }
    var precioStr by remember { mutableStateOf(plan?.precio?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        confirmButton = {
            Button(onClick = {
                // Convertir strings a Int de forma segura
                val duracion = duracionStr.toIntOrNull() ?: 0
                val precio = precioStr.toIntOrNull() ?: 0

                // imagenUrl => null si blank
                val imagenOpt = imagenUrl.ifBlank { null }

                onGuardar(
                    nombre.trim(),
                    duracion,
                    nivel.trim(),
                    objetivo.trim(),
                    imagenOpt,
                    precio
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

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = duracionStr,
                    onValueChange = { duracionStr = it.filter { ch -> ch.isDigit() } }, // solo dígitos
                    label = { Text("Duración (min)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = nivel,
                    onValueChange = { nivel = it },
                    label = { Text("Nivel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = objetivo,
                    onValueChange = { objetivo = it },
                    label = { Text("Objetivo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imagenUrl,
                    onValueChange = { imagenUrl = it },
                    label = { Text("URL de imagen (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precioStr,
                    onValueChange = { precioStr = it.filter { ch -> ch.isDigit() } }, // solo dígitos
                    label = { Text("Precio (sin símbolos)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        }
    )
}
