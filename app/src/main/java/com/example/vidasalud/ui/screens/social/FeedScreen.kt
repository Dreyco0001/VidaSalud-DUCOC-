package com.example.vidasalud.ui.screens.social

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedScreen(feedViewModel: FeedViewModel = viewModel()) {
    var chatVisible by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { chatVisible = !chatVisible }) {
                Icon(Icons.Filled.Chat, contentDescription = "Abrir chat")
            }
        }
    ) { paddingValues ->
        // Contenido principal de la pantalla de Comunidad
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Comunidad",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Anuncios
            Anuncio(
                titulo = "¡Nuevo Desafío Fitness!",
                contenido = "Únete a nuestro desafío de 30 días y transforma tu cuerpo y mente. ¡Inscripciones abiertas!"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Anuncio(
                titulo = "Clase de Yoga Gratuita",
                contenido = "Este sábado a las 10 AM, clase de yoga al aire libre en el parque central. ¡No te la pierdas!"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Anuncio(
                titulo = "Tips de Nutrición",
                contenido = "Descubre 5 superalimentos que no pueden faltar en tu dieta para potenciar tu energía."
            )
        }

        // Chat animado que aparece desde abajo
        AnimatedVisibility(
            visible = chatVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Fondo oscuro semitransparente
                    .imePadding() // Ajusta la vista cuando el teclado aparece
            ) {
                ChatWindow(
                    feedViewModel = feedViewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f) // Ocupa el 70% de la altura
                )
            }
        }
    }
}


@Composable
fun Anuncio(titulo: String, contenido: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = contenido,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun ChatWindow(feedViewModel: FeedViewModel, modifier: Modifier = Modifier) {
    val comentarios by feedViewModel.comentarios.observeAsState(emptyList())
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Lista de mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            reverseLayout = true
        ) {
            items(comentarios.sortedByDescending { it.timestamp }) { comentario ->
                ChatItem(comentario = comentario)
            }
        }

        // Campo de texto para enviar mensaje
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = mensaje,
                onValueChange = { mensaje = it },
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (mensaje.isNotBlank()) {
                        feedViewModel.enviarMensaje(mensaje)
                        mensaje = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Enviar",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


@Composable
fun ChatItem(comentario: Comentario) {
    val formattedDate = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        .format(Date(comentario.timestamp ?: System.currentTimeMillis()))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp) // Padding reducido
        ) {
            Column {
                Text(
                    text = comentario.userName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = comentario.mensaje,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
