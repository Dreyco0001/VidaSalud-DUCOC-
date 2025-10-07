package com.example.vidasalud.ui.screens.login

import android.R
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun Login(){
    val context = LocalContext.current
    var user by remember { mutableStateOf( value= "") }
    var pass by remember { mutableStateOf(value = "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
        )

        Spacer(Modifier.height(16.dp)) //Espacio entre elementos

        OutlinedTextField( //input usuario
            value = user,
            onValueChange = {user = it},
            label = {Text("Usuario", color = Color(0xFF7DC9B1))},
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField( //input password
            value = pass,
            onValueChange = {pass = it},
            label = {Text("Contraseña", color = Color(0xFF7DC9B1))},
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), //oculta la contra
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                Toast.makeText(context, "Bienvenido $user", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DC9B1))
        ) {
            Text("Ingresar") // Puedes personalizar esto
        }

    }



}