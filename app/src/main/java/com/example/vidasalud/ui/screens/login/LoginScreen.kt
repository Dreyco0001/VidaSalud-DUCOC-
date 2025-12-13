package com.example.vidasalud.ui.screens.login

import androidx.compose.runtime.Composable

import android.widget.Toast //Mensaje emergentes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.* //Organizar los elementos en una vista
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions //Mostrar la entre de datos al usuario
import androidx.compose.material3.* //Elementos para diseñar UI
import androidx.compose.runtime.* // Manejar los estados de la app
import androidx.compose.ui.Alignment // Alinear los elementos
import androidx.compose.ui.Modifier //Modificar el diseño visual de los elemento
import androidx.compose.ui.graphics.Color //Controlar el color de los elementos
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext //obtener el contexto o estado en ejecución del ciclo de vida de la app y poder mostrar mensaje
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType //Controlar el tipo de entrada para el usuario
import androidx.compose.ui.text.input.PasswordVisualTransformation //Ocultar la contraseña al escribirla
import androidx.compose.ui.unit.dp //Controlar el tamaño de los elementos
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.R
import com.example.vidasalud.model.Usuario
import com.example.vidasalud.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit = {},
    onLoginSuccess: (user: Usuario) -> Unit = {}
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val viewModel: LoginViewModel = viewModel()
    val user by viewModel.usuario.collectAsState()
    val carga by viewModel.cargaLogin.collectAsState()
    val errorLogin by viewModel.errorLogin.collectAsState() // 👈 AQUI

    // LOGIN OK
    LaunchedEffect(user) {
        user?.let {
            val mensaje = when (it.rol) {
                "admin" -> "Bienvenido Admin: ${it.nombre}"
                else -> "Bienvenido: ${it.nombre}"
            }
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            onLoginSuccess(it)
            viewModel.clearUser()
        }
    }

    // ❌ LOGIN ERROR
    LaunchedEffect(errorLogin) {
        errorLogin?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.diseno),
            contentDescription = "Fondo de inicio de sesión",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Inciar Sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF4CAF50)
                )

                Spacer(Modifier.height(46.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Usuario", color = Color(0xFFFF5722)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Clave", color = Color(0xFFFF5722)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (correo.isEmpty() || pass.isEmpty()) {
                            Toast.makeText(context, "Completar todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.login(correo, pass)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81154C),
                        contentColor = Color(0xFFC7F9CC)
                    )
                ) {
                    if (carga) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Entrar")
                    }
                }

                Spacer(Modifier.height(24.dp))

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "No tengo una cuenta",
                        color = Color(0xFF2242C9),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
