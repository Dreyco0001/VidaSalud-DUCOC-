package com.example.vidasalud.navigation

import android.widget.Toast
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

        // Login
        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register")
                },
                onLoginSuccess = { user ->
                    when (user.rol.lowercase()) {
                        "admin" -> {
                            navController.navigate("perfil_admin/${user.nombre}") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        "cliente" -> {
                            navController.navigate("catalogo/${user.nombre}") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        else -> {
                            Toast.makeText(
                                navController.context,
                                "Rol desconocido: ${user.rol}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        // Registro
        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Perfil Admin
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

        // Perfil Cliente
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
                onVerCarrito = {
                    navController.navigate("carrito")
                },
                viewModel = carritoViewModel
            )
        }

        // Catalogo
        composable(
            "catalogo/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"
            CatalogoScreen(
                nombre = nombre,
                onVerPerfil = {
                    navController.navigate("perfil_cliente/$nombre")
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

        // Carrito
        composable("carrito") {
            CarritoScreen(
                viewModel = carritoViewModel,
                onVolverAlCatalogo = {
                    navController.popBackStack()
                },
                onConfirmarPago = {
                    navController.navigate("pago") {
                        popUpTo("catalogo") { inclusive = false }
                    }
                }
            )
        }

        // Confirmacion de Pago
        composable("pago") {
            PagoConfirmacionScreen(
                nombreUsuario = "Cliente",
                onVolverAlCatalogo = {
                    navController.navigate("catalogo/Cliente") {
                        popUpTo("login") { inclusive = false }
                    }
                }
            )
        }
    }
}
