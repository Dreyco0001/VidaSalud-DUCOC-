package com.example.vidasalud.navigation

import android.net.Uri
import androidx.compose.material3.Text
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
                },
                onGestionAdmin = {
                    navController.navigate("gestion_planes")
                }
            )
        }

        // GESTION DE PLANES (CRUD)
        composable("gestion_planes") {
            // Aquí va la pantalla para administrar los planes
            Text("Pantalla de Gestión de Planes")
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
                    val nombreEncoded = Uri.encode(nombre)
                    val rolEncoded = Uri.encode(rol)
                    navController.navigate("carrito/$nombreEncoded/$rolEncoded")
                }

            )
        }

        // CARRITO
        composable(
            route = "carrito/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType; defaultValue = "Usuario" },
                navArgument("rol") { type = NavType.StringType; defaultValue = "cliente" }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
            val rol = backStackEntry.arguments?.getString("rol") ?: "cliente"

            CarritoScreen(
                nombre = nombre,
                onVerPerfil = { navController.navigate("perfil/$nombre/$rol") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVerDetallePlan = { plan -> println("Iniciando plan: ${plan.nombre}") },
                onVolverAlCatalogo = { navController.popBackStack() }
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
