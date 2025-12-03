package com.example.vidasalud.ui.screens.catalogo

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// 1. Data class para representar cada actividad (con precio)
data class Actividad(
    val nombre: String,
    val duracion: String,
    val descripcion: String,
    @DrawableRes val imagen: Int,
    val precio: String
)

// 2. Lista de todas las actividades (con precios actualizados)
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

        // ================ CONTENIDO DESLIZABLE =====================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre tarjetas
        ) {

            item { // Título de la sección
                Text("Planes de Ejercicio", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Itera sobre la lista de actividades para crear cada tarjeta
            items(listaDeActividades) { actividad ->
                ActivityCard(actividad = actividad)
            }
        }

        // ================ FOOTER =====================
        BarraNavegacionPrincipal(
            navController = navController,
            nombreUsuario = nombre,
            rolUsuario = rol,
            rutaActual = "catalogo/{nombre}/{rol}"
        )
    }
}

// 4. Función reutilizable para dibujar cada tarjeta de actividad (con precio)
@Composable
fun ActivityCard(actividad: Actividad) {
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
                        onClick = { /* Lógica para reservar */ },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Reservar")
                    }
                }
            }
        }
    }
}