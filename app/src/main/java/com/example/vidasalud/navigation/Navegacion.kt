package com.example.vidasalud.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vidasalud.ui.screens.carrito.CarritoScreen
import com.example.vidasalud.ui.screens.catalogo.CatalogoScreen
import com.example.vidasalud.ui.screens.login.LoginScreen
import com.example.vidasalud.ui.screens.pago.PagoConfirmacionScreen
import com.example.vidasalud.ui.screens.perfil.PerfilScreen
import com.example.vidasalud.ui.screens.registro.RegistroScreen
import com.example.vidasalud.viewmodel.CarritoViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val carritoViewModel: CarritoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register")
                },
                onLoginSuccess = { user ->
                    val nombreEncoded = Uri.encode(user.nombre)
                    val rolEncoded = Uri.encode(user.rol)

                    // Ambos roles van al mismo PerfilScreen
                    navController.navigate("perfil/$nombreEncoded/$rolEncoded") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // REGISTRO
        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

        // PERFIL UNIFICADO
        composable(
            route = "perfil/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
            val rol = backStackEntry.arguments?.getString("rol") ?: "cliente"

            PerfilScreen(
                nombre = nombre,
                rol = rol,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerCatalogo = {
                    // Navega al catálogo pasando nombre y rol
                    val nombreEncoded = Uri.encode(nombre)
                    val rolEncoded = Uri.encode(rol)
                    navController.navigate("catalogo/$nombreEncoded/$rolEncoded")
                }
            )
        }

        // CATALOGO
        composable(
            route = "catalogo/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"
            val rol = backStackEntry.arguments?.getString("rol") ?: "cliente"

            CatalogoScreen(
                nombre = nombre,
                rol = rol,
                onVerPerfil = {
                    navController.navigate("perfil/$nombre/$rol")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerCarrito = {
                    navController.navigate("carrito")
                }
            )
        }

        // CARRITO
        composable("carrito") {
            CarritoScreen(
                viewModel = carritoViewModel,
                onVolverAlCatalogo = { navController.popBackStack() },
                onConfirmarPago = {
                    navController.navigate("pago") {
                        popUpTo("catalogo") { inclusive = false }
                    }
                }
            )
        }

        // PAGO
        composable("pago") {
            PagoConfirmacionScreen(
                nombreUsuario = "Cliente",
                onVolverAlCatalogo = {
                    navController.navigate("catalogo/Cliente/cliente") {
                        popUpTo("login") { inclusive = false }
                    }
                }
            )
        }
    }
}
