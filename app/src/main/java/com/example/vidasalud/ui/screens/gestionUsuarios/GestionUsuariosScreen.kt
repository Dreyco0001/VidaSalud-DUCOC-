package com.example.vidasalud.ui.screens.gestionUsuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.model.Usuario
import com.example.vidasalud.viewmodel.GestionUsuariosViewModel

@Composable
fun GestionUsuariosScreen(
    onVolver: () -> Unit,
    vm: GestionUsuariosViewModel = viewModel()
) {
    val usuarios by vm.usuarios.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("cliente") } // valor inicial por defecto
    var mostrarAdmins by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onVolver) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FORMULARIO CREAR USUARIO
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Clave") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Rol:", style = MaterialTheme.typography.bodyMedium)
        Row {
            Button(
                onClick = { rol = "admin" },
                colors = if (rol == "admin") ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                else ButtonDefaults.buttonColors()
            ) {
                Text("Admin")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { rol = "cliente" },
                colors = if (rol == "cliente") ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                else ButtonDefaults.buttonColors()
            ) {
                Text("Cliente")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (correo.isNotBlank() && clave.length >= 6 && nombre.isNotBlank() && rol.isNotBlank()) {
                    vm.crearUsuario(
                        correo = correo,
                        clave = clave,
                        nombre = nombre,
                        rol = rol
                    )
                    correo = ""; clave = ""; nombre = ""; rol = "cliente"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear usuario")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        val admins = usuarios.filter { it.rol == "admin" }
        val noAdmins = usuarios.filter { it.rol != "admin" }

        if (admins.isNotEmpty()) {
            OutlinedButton(
                onClick = { mostrarAdmins = !mostrarAdmins },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(if (mostrarAdmins) "Ocultar Administradores" else "Mostrar Administradores")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(noAdmins) { usuario ->
                UsuarioItem(
                    usuario = usuario,
                    onEliminar = { vm.eliminarUsuario(usuario.uid) },
                    onActualizar = { nombreNuevo, rolNuevo, correoNuevo, claveNueva ->
                        vm.actualizarUsuario(
                            uid = usuario.uid,
                            nombre = nombreNuevo,
                            rol = rolNuevo,
                            correo = correoNuevo,
                            clave = claveNueva,
                            usuariosActuales = usuarios
                        )
                    }
                )
            }

            if (mostrarAdmins) {
                items(admins) { usuario ->
                    UsuarioItem(
                        usuario = usuario,
                        onEliminar = { vm.eliminarUsuario(usuario.uid) },
                        onActualizar = { nombreNuevo, rolNuevo, correoNuevo, claveNueva ->
                            vm.actualizarUsuario(
                                uid = usuario.uid,
                                nombre = nombreNuevo,
                                rol = rolNuevo,
                                correo = correoNuevo,
                                clave = claveNueva,
                                usuariosActuales = usuarios
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UsuarioItem(
    usuario: Usuario,
    onEliminar: () -> Unit,
    onActualizar: (String, String, String, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Correo: ${usuario.correo}")
            Text("Nombre: ${usuario.nombre}")
            Text("Rol: ${usuario.rol}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { showEditDialog = true }) {
                    Text("Editar")
                }

                OutlinedButton(onClick = onEliminar) {
                    Text("Eliminar")
                }
            }
        }
    }

    if (showEditDialog) {
        EditUsuarioDialog(
            usuario = usuario,
            onDismiss = { showEditDialog = false },
            onSave = { nombre, rol, correo, clave ->
                onActualizar(nombre, rol, correo, clave)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditUsuarioDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var rol by remember { mutableStateOf(usuario.rol) }
    var correo by remember { mutableStateOf(usuario.correo) }
    var clave by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Rol:", style = MaterialTheme.typography.bodyMedium)
                Row {
                    Button(
                        onClick = { rol = "admin" },
                        colors = if (rol == "admin") ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                        else ButtonDefaults.buttonColors()
                    ) {
                        Text("Admin")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { rol = "cliente" },
                        colors = if (rol == "cliente") ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                        else ButtonDefaults.buttonColors()
                    ) {
                        Text("Cliente")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it },
                    label = { Text("Nueva Contraseña (min 6)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (correo.isNotBlank() && (clave.isBlank() || clave.length >= 6)) {
                    onSave(nombre, rol, correo, clave)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
