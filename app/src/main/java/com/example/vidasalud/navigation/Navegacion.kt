package com.example.vidasalud.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vidasalud.ui.screens.login.LoginScreen
import com.example.vidasalud.ui.screens.registro.RegistroScreen
import com.example.vidasalud.ui.screens.perfil.PerfilAdminScreen
import com.example.vidasalud.ui.screens.perfil.PerfilClienteScreen

import com.example.vidasalud.ui.screens.carrito.CarritoScreen
import com.example.vidasalud.ui.screens.catalogo.CatalogoScreen
import com.example.vidasalud.ui.screens.pago.PagoConfirmacionScreen
import com.example.vidasalud.viewmodel.CarritoViewModel


@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val carritoViewModel: CarritoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 🔹 Login
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { user ->
                    // Ahora el login siempre manda al catálogo
                    navController.navigate("catalogo/${user.rol}/${user.nombre}") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 🔹 Registro
        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

        // 🔹 Catálogo (Hub principal)
        composable(
            "catalogo/{rol}/{nombre}",
            arguments = listOf(
                navArgument("rol") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString("rol") ?: "cliente"
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"

            CatalogoScreen(
                onVerCarrito = { navController.navigate("carrito/${nombre}") },
                onVerPerfil = {
                    when (rol.lowercase()) {
                        "admin" -> navController.navigate("perfil_admin/$nombre")
                        else -> navController.navigate("perfil_cliente/$nombre")
                    }
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = carritoViewModel
            )
        }

        // 🔹 Perfil Admin
        composable(
            "perfil_admin/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Administrador"
            PerfilAdminScreen(
                nombre = nombre,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 🔹 Perfil Cliente
        composable(
            "perfil_cliente/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"
            PerfilClienteScreen(
                nombre = nombre,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerCarrito = { navController.navigate("carrito/$nombre") },
                viewModel = carritoViewModel
            )
        }

        // 🔹 Carrito
        composable(
            "carrito/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"
            CarritoScreen(
                onVolverAlCatalogo = { navController.popBackStack() },
                onConfirmarPago = { navController.navigate("pago/$nombre") },
                viewModel = carritoViewModel
            )
        }

        // 🔹 Pago
        composable(
            "pago/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"
            PagoConfirmacionScreen(
                nombreUsuario = nombre,
                onVolverAlCatalogo = { navController.navigate("catalogo/cliente/$nombre") }
            )
        }
    }
}
