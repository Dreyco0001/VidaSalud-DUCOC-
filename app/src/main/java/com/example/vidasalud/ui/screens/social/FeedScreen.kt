package com.example.vidasalud.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel = viewModel(),
    currentUserName: String = "Invitado",
    currentIsAdmin: Boolean = false,
    onBack: () -> Unit = {}
) {
    val comentarios by feedViewModel.comentarios.observeAsState(emptyList())
    val error by feedViewModel.error.observeAsState()

    var mensaje by remember { mutableStateOf("") }
    var comentarioEditar by remember { mutableStateOf<Comentario?>(null) }
    var textoEditado by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                reverseLayout = true
            ) {
                items(comentarios.sortedByDescending { it.timestamp }) { comentario ->

                    val canEdit = currentIsAdmin ||
                            (comentario.userId == currentUserId &&
                                    puedeEditarLocal(comentario, currentUserId, currentIsAdmin))

                    val canDelete = currentIsAdmin || comentario.userId == currentUserId

                    ChatItem(
                        comentario = comentario,
                        canEdit = canEdit,
                        canDelete = canDelete,
                        currentUserId = currentUserId,
                        onDelete = {
                            feedViewModel.eliminarComentario(comentario.id, currentIsAdmin)
                        },
                        onEdit = {
                            comentarioEditar = comentario
                            textoEditado = comentario.mensaje
                        },
                        onLike = { feedViewModel.toggleLike(comentario.id) },
                        viewModel = feedViewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it.take(250) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje…") },
                    shape = CircleShape
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (mensaje.isNotBlank()) {
                            feedViewModel.enviarMensaje(
                                texto = mensaje.trim(),
                                userName = currentUserName
                            )
                            mensaje = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    if (comentarioEditar != null) {
        AlertDialog(
            onDismissRequest = { comentarioEditar = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        comentarioEditar?.let {
                            feedViewModel.editarComentario(
                                it.id,
                                textoEditado.trim(),
                                currentIsAdmin
                            )
                        }
                        comentarioEditar = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { comentarioEditar = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Editar comentario") },
            text = {
                OutlinedTextField(
                    value = textoEditado,
                    onValueChange = { textoEditado = it.take(250) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}


@Composable
fun ChatItem(
    comentario: Comentario,
    canEdit: Boolean,
    canDelete: Boolean,
    currentUserId: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onLike: () -> Unit,
    viewModel: FeedViewModel
) {
    val fecha = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        .format(Date(comentario.timestamp))

    var likeCount by remember { mutableStateOf(0) }
    var listener by remember { mutableStateOf<ListenerRegistration?>(null) }

    DisposableEffect(comentario.id) {

        listener = viewModel.repoPublic.escucharLikes(comentario.id) { likes ->
            likeCount = likes.size
        }

        onDispose {
            listener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(comentario.userName, fontSize = 14.sp, modifier = Modifier.weight(1f))

            if (canEdit)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }

            if (canDelete)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(comentario.mensaje)
                Text(fecha, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLike) {
                Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.Red)
            }
            Text("$likeCount likes", fontSize = 12.sp)
        }
    }
}


fun puedeEditarLocal(c: Comentario, userId: String, isAdmin: Boolean): Boolean {
    if (isAdmin) return true
    if (c.userId != userId) return false
    val tiempo = System.currentTimeMillis() - c.timestamp
    return tiempo <= (10 * 60 * 1000L)
}
