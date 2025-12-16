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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column {
                    Text(
                        "Gestión de Planes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${planes.size} plan(es) cargado(s)", color = Color.Gray)

                    // 🔔 AVISO ÚNICO AGREGADO (NO SE MODIFICA NADA MÁS)
                    Text(
                        text = "ℹ️ Todos los planes tienen una duración de 1 día.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) CircularProgressIndicator()
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

    // CREAR
    if (mostrarDialogoCrear) {
        DialogoPlan(
            titulo = "Crear Plan",
            plan = null,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nombre, duracion, nivel, objetivo, imagenUrl, precio, comida, imagenComida ->
                viewModel.crearPlan(
                    nombre, duracion, nivel, objetivo,
                    imagenUrl, precio, comida, imagenComida
                )
                mostrarDialogoCrear = false
            }
        )
    }

    // EDITAR
    planAEditar?.let { plan ->
        DialogoPlan(
            titulo = "Editar Plan",
            plan = plan,
            onDismiss = { planAEditar = null },
            onGuardar = { nombre, duracion, nivel, objetivo, imagenUrl, precio, comida, imagenComida ->
                viewModel.actualizarPlan(
                    id = plan.id,
                    nombre = nombre,
                    duracion = duracion,
                    nivel = nivel,
                    objetivo = objetivo,
                    imagenUrl = imagenUrl,
                    precio = precio,
                    comidaRecomendada = comida,
                    imagenComidaUrl = imagenComida
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
        Column(Modifier.padding(12.dp)) {

            if (plan.imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = plan.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(plan.nombre, fontWeight = FontWeight.Bold)
            Text("Duración: ${plan.duracion} min")
            Text("Nivel: ${plan.nivel}")
            Text("Objetivo: ${plan.objetivo}")
            Text("Precio: ${formatPrecio(plan.precio)}")

            if (plan.comidaRecomendada.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("🍽 Recomendación:", fontWeight = FontWeight.SemiBold)
                Text(plan.comidaRecomendada)
            }

            if (plan.imagenComidaUrl.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                AsyncImage(
                    model = plan.imagenComidaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }
        }
    }
}

// -------------------------------------------------------------

@Composable
fun DialogoPlan(
    titulo: String,
    plan: Plan?,
    onDismiss: () -> Unit,
    onGuardar: (String, Int, String, String, String?, Int, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(plan?.nombre ?: "") }
    var duracionStr by remember { mutableStateOf(plan?.duracion?.toString() ?: "") }
    var nivel by remember { mutableStateOf(plan?.nivel ?: "") }
    var objetivo by remember { mutableStateOf(plan?.objetivo ?: "") }
    var imagenUrl by remember { mutableStateOf(plan?.imagenUrl ?: "") }
    var precioStr by remember { mutableStateOf(plan?.precio?.toString() ?: "") }
    var comida by remember { mutableStateOf(plan?.comidaRecomendada ?: "") }
    var imagenComida by remember { mutableStateOf(plan?.imagenComidaUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        confirmButton = {
            Button(onClick = {
                onGuardar(
                    nombre,
                    duracionStr.toIntOrNull() ?: 0,
                    nivel,
                    objetivo,
                    imagenUrl.ifBlank { null },
                    precioStr.toIntOrNull() ?: 0,
                    comida,
                    imagenComida
                )
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(
                    duracionStr,
                    { duracionStr = it.filter(Char::isDigit) },
                    label = { Text("Duración (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(nivel, { nivel = it }, label = { Text("Nivel") }, singleLine = true)
                OutlinedTextField(objetivo, { objetivo = it }, label = { Text("Objetivo") }, singleLine = true)
                OutlinedTextField(imagenUrl, { imagenUrl = it }, label = { Text("Imagen del plan") }, singleLine = true)
                OutlinedTextField(
                    precioStr,
                    { precioStr = it.filter(Char::isDigit) },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    comida,
                    { comida = it },
                    label = { Text("Descripción comida recomendada") },
                    singleLine = true
                )
                OutlinedTextField(
                    imagenComida,
                    { imagenComida = it },
                    label = { Text("Imagen comida (URL)") },
                    singleLine = true
                )
            }
        }
    )
}

// -------------------------------------------------------------

private fun formatPrecio(precio: Int): String =
    "$" + "%,d".format(precio).replace(',', '.')
