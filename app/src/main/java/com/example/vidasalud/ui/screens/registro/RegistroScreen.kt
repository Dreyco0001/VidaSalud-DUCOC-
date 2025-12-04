package com.example.vidasalud.ui.screens.registro

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.vidasalud.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(onBack: () -> Unit = {}, onRegisterSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val repositorio = AuthRepository()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Crear una Cuenta",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (correo.isEmpty() || pass.isEmpty() || nombre.isEmpty()) {
                    Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // Lógica de registro movida aquí
                scope.launch {
                    val user = repositorio.register(correo, pass, nombre)
                    if (user != null) {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    } else {
                        Toast.makeText(context, "Error en el registro", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Registrarse")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Ya tengo una cuenta")
        }
    }
}