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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel = viewModel(),
    currentUserId: String = "",
    currentUserName: String = "Invitado",
    currentIsAdmin: Boolean = false
) {
    val comentarios by feedViewModel.comentarios.observeAsState(emptyList())
    val error by feedViewModel.error.observeAsState()

    var mensaje by remember { mutableStateOf("") }
    var comentarioEditar by remember { mutableStateOf<Comentario?>(null) }
    var textoEditado by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Comunidad") }) },
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

                    val canEdit = puedeEditarLocal(comentario, currentUserId, currentIsAdmin)
                    val canDelete = (currentIsAdmin || comentario.userId == currentUserId)

                    ChatItem(
                        comentario = comentario,
                        canEdit = canEdit,
                        canDelete = canDelete,
                        currentUserId = currentUserId,
                        onDelete = {
                            feedViewModel.eliminarComentario(
                                comentario.id,
                                currentUserId,
                                currentIsAdmin
                            )
                        },
                        onEdit = {
                            comentarioEditar = comentario
                            textoEditado = comentario.mensaje
                        },
                        onLike = {
                            feedViewModel.toggleLike(comentario.id, currentUserId)
                        },
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
                            feedViewModel.enviarMensaje(
                                texto = mensaje.trim(),
                                userId = currentUserId,
                                userName = currentUserName
                            )
                            mensaje = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Send, "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
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
                                currentUserId,
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


/* ----------------------------------------------------------
   CHAT ITEM (composable limpio y funcional)
----------------------------------------------------------- */

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

    // REAL-TIME: cada cambio en Firestore dispara update automático
    LaunchedEffect(comentario.id) {
        likeCount = viewModel.getLikes(comentario.id)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                comentario.userName,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                }
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
                Text(
                    fecha,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                onLike()
            }) {
                Icon(
                    Icons.Default.Favorite,
                    "Like",
                    tint = Color.Red
                )
            }

            Text("$likeCount likes", fontSize = 12.sp)
        }
    }
}


/* ----------------------------------------------------------
   FUNCIÓN NORMAL – NO ES COMPOSABLE
----------------------------------------------------------- */
fun puedeEditarLocal(c: Comentario, userId: String, isAdmin: Boolean): Boolean {
    if (isAdmin) return true
    if (c.userId != userId) return false
    val tiempo = System.currentTimeMillis() - c.timestamp
    return tiempo <= (10 * 60 * 1000L)
}
