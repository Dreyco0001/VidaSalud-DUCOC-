package com.example.vidasalud.ui.screens.gestionUsuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var rol by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // 🔙 BOTÓN VOLVER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedButton(onClick = onVolver) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FORM CREAR USUARIO
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

        OutlinedTextField(
            value = rol,
            onValueChange = { rol = it },
            label = { Text("Rol") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (correo.isNotBlank() && clave.isNotBlank() && nombre.isNotBlank() && rol.isNotBlank()) {
                    vm.crearUsuario(
                        correo = correo,
                        clave = clave,
                        nombre = nombre,
                        rol = rol
                    )
                    correo = ""
                    clave = ""
                    nombre = ""
                    rol = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear usuario")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LOADING
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // ERROR
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // LISTA DE USUARIOS
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(usuarios) { usuario ->
                UsuarioItem(
                    usuario = usuario,
                    onEliminar = { vm.eliminarUsuario(usuario.correo) },
                    onActualizar = {
                        vm.actualizarUsuario(
                            correo = usuario.correo,
                            nombre = usuario.nombre,
                            rol = usuario.rol
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun UsuarioItem(
    usuario: Usuario,
    onEliminar: () -> Unit,
    onActualizar: () -> Unit
) {
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
                OutlinedButton(onClick = onActualizar) {
                    Text("Actualizar")
                }
                OutlinedButton(onClick = onEliminar) {
                    Text("Eliminar")
                }
            }
        }
    }
}
