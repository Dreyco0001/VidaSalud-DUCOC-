package com.example.vidasalud.ui.screens.social

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.model.Comentario
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel = viewModel()
) {
    val comentarios by feedViewModel.comentarios.collectAsState()
    val mensaje by feedViewModel.mensaje.collectAsState()
    var isChatExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isChatExpanded = !isChatExpanded },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "Abrir chat")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Aquí puedes poner el contenido principal del feed si lo hubiera.
            // Por ahora, solo tenemos el chat.

            // Ventana de chat desplegable
            AnimatedVisibility(
                visible = isChatExpanded,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                ChatWindow(
                    comentarios = comentarios,
                    mensaje = mensaje,
                    onMensajeChanged = { feedViewModel.onMensajeChanged(it) },
                    onEnviarMensaje = { feedViewModel.enviarMensaje() }
                )
            }
        }
    }
}

@Composable
fun ChatWindow(
    comentarios: List<Comentario>,
    mensaje: String,
    onMensajeChanged: (String) -> Unit,
    onEnviarMensaje: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(comentarios.size) {
        if (comentarios.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(comentarios.size - 1)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .heightIn(max = 400.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                items(comentarios) { comentario ->
                    ChatItem(comentario)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ChatInput(
                mensaje = mensaje,
                onMensajeChanged = onMensajeChanged,
                onEnviarMensaje = onEnviarMensaje
            )
        }
    }
}


@Composable
fun ChatItem(comentario: Comentario) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isMyMessage = comentario.userId == currentUserId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMyMessage) 16.dp else 0.dp,
                bottomEnd = if (isMyMessage) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = comentario.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comentario.mensaje,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    mensaje: String,
    onMensajeChanged: (String) -> Unit,
    onEnviarMensaje: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = mensaje,
            onValueChange = onMensajeChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un mensaje...") },
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onEnviarMensaje,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Enviar",
                tint = Color.White
            )
        }
    }
}
