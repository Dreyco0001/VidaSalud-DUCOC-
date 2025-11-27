package com.example.vidasalud.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.viewmodel.FeedViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * FeedScreen corregida:
 * - Ya no depende de propiedades inexistentes en el ViewModel.
 * - Recibe (o usa defaults) currentUserId, currentUserName, currentIsAdmin.
 * - Calcula localmente si un comentario es editable (10 min para cliente).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel = viewModel(),
    currentUserId: String = "",                // pasa aquí el uid real desde tu login
    currentUserName: String = "Invitado",      // pasa el nombre de usuario real
    currentIsAdmin: Boolean = false            // true si es admin
) {

    // Observers
    val comentarios by feedViewModel.comentarios.observeAsState(emptyList())
    val error by feedViewModel.error.observeAsState()

    // Estados UI
    var mensaje by remember { mutableStateOf("") }
    var comentarioEditar by remember { mutableStateOf<Comentario?>(null) }
    var textoEditado by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error en snackbar
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

            // LISTA DE COMENTARIOS
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                reverseLayout = true
            ) {
                items(comentarios.sortedByDescending { it.timestamp }) { comentario ->

                    val canDelete = (currentIsAdmin || comentario.userId == currentUserId)
                    val canEdit = puedeEditarLocal(comentario, currentUserId, currentIsAdmin)

                    ChatItem(
                        comentario = comentario,
                        canDelete = canDelete,
                        canEdit = canEdit,
                        onDelete = {
                            feedViewModel.eliminarComentario(
                                comentarioId = comentario.id,
                                userId = currentUserId,
                                isAdmin = currentIsAdmin
                            )
                        },
                        onEdit = {
                            comentarioEditar = comentario
                            textoEditado = comentario.mensaje
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // INPUT PARA ESCRIBIR
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
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Enviar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    // DIALOGO EDITAR
    if (comentarioEditar != null) {
        AlertDialog(
            onDismissRequest = { comentarioEditar = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        comentarioEditar?.let {
                            feedViewModel.editarComentario(
                                comentarioId = it.id,
                                nuevoTexto = textoEditado.trim(),
                                userId = currentUserId,
                                isAdmin = currentIsAdmin
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

/** Calcula localmente si el comentario puede editarse:
 * - admin: siempre
 * - cliente: solo si es creador y han pasado <= 10 minutos
 */
private fun puedeEditarLocal(c: Comentario, currentUserId: String, isAdmin: Boolean): Boolean {
    if (isAdmin) return true
    if (c.userId != currentUserId) return false
    val tiempoPasado = System.currentTimeMillis() - c.timestamp
    val limite = 10 * 60 * 1000L
    return tiempoPasado <= limite
}

@Composable
fun ChatItem(
    comentario: Comentario,
    canDelete: Boolean,
    canEdit: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val fecha = try {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            .format(Date(comentario.timestamp))
    } catch (e: Exception) {
        ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = comentario.userName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            if (canEdit) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red
                    )
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
                Text(
                    text = comentario.mensaje,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    fecha,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
