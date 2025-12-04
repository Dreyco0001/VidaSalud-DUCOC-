package com.example.vidasalud.ui.screens.catalogo

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vidasalud.R
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal

data class Actividad(
    val nombre: String,
    val duracion: String,
    val descripcion: String,
    @DrawableRes val imagen: Int,
    val precio: String
)

val listaDeActividades = listOf(
    Actividad(
        nombre = "Yoga",
        duracion = "20 minutos",
        descripcion = "El yoga es una práctica que conecta el cuerpo, la respiración y la mente. Utiliza posturas físicas, ejercicios de respiración y meditación para mejorar la salud general.",
        imagen = R.drawable.yoga,
        precio = "$14.990"
    ),
    Actividad(
        nombre = "Aeróbicos",
        duracion = "30 minutos",
        descripcion = "Los ejercicios aeróbicos mejoran la resistencia cardiovascular y ayudan a quemar calorías de forma efectiva, fortaleciendo el corazón y los pulmones.",
        imagen = R.drawable.aeribicos,
        precio = "$14.990"
    ),
    Actividad(
        nombre = "Fuerza",
        duracion = "45 minutos",
        descripcion = "El entrenamiento de fuerza se centra en desarrollar la masa muscular y la resistencia, utilizando pesas o el propio peso corporal para tonificar el cuerpo.",
        imagen = R.drawable.fuerza,
        precio = "$14.990"
    ),
    Actividad(
        nombre = "Natación",
        duracion = "40 minutos",
        descripcion = "La natación es un ejercicio de bajo impacto que trabaja todos los grupos musculares, ideal para mejorar la postura y la capacidad pulmonar.",
        imagen = R.drawable.natacion,
        precio = "$19.990"
    ),
    Actividad(
        nombre = "Zumba",
        duracion = "50 minutos",
        descripcion = "Zumba es una disciplina de baile divertida y energética que combina ritmos latinos con ejercicios aeróbicos para una sesión de cardio completa.",
        imagen = R.drawable.zumba,
        precio = "$14.990"
    ),
    Actividad(
        nombre = "Pilates",
        duracion = "35 minutos",
        descripcion = "Pilates se enfoca en fortalecer el núcleo del cuerpo, mejorar la flexibilidad y la conciencia corporal a través de movimientos controlados y precisos.",
        imagen = R.drawable.pilates,
        precio = "$19.990"
    ),
)

@Composable
fun CatalogoScreen(
    navController: NavController,
    nombre: String = "Cliente",
    rol: String = "cliente",
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<Actividad?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item { 
                Text("Planes de Ejercicio", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(listaDeActividades) { actividad ->
                ActivityCard(actividad = actividad, onReserveClick = {
                    selectedActivity = it
                    showDialog = true
                })
            }
        }

        BarraNavegacionPrincipal(
            navController = navController,
            nombreUsuario = nombre,
            rolUsuario = rol,
            rutaActual = "catalogo/{nombre}/{rol}"
        )
    }

    if (showDialog && selectedActivity != null) {
        ReservationDialog(
            activityName = selectedActivity!!.nombre,
            onDismiss = { showDialog = false },
            onConfirm = { quantity ->
                // Lógica de confirmación
                showDialog = false
            }
        )
    }
}

@Composable
fun ActivityCard(actividad: Actividad, onReserveClick: (Actividad) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = actividad.imagen),
                contentDescription = "Imagen de ${actividad.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = actividad.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Duración: ${actividad.duracion}", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = actividad.descripcion, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = actividad.precio,
                        modifier = Modifier.align(Alignment.CenterStart),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { onReserveClick(actividad) },
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
    var quantity by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Reservar: $activityName")
        },
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
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Reducir cantidad")
                    }

                    Text(text = quantity.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    IconButton(
                        onClick = { if (quantity < 5) quantity++ },
                        enabled = quantity < 5
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity) },
            ) {
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
