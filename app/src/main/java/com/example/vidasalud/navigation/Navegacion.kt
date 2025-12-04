package com.example.vidasalud.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vidasalud.ui.screens.catalogo.CatalogoScreen
import com.example.vidasalud.ui.screens.gestionPlanes.GestionPlanesScreen
import com.example.vidasalud.ui.screens.gestionUsuarios.GestionUsuariosScreen
import com.example.vidasalud.ui.screens.home.HomeScreen // <-- Importación añadida
import com.example.vidasalud.ui.screens.login.LoginScreen
import com.example.vidasalud.ui.screens.pago.PagoConfirmacionScreen
import com.example.vidasalud.ui.screens.perfil.PerfilScreen
import com.example.vidasalud.ui.screens.registro.RegistroScreen
import com.example.vidasalud.ui.screens.carrito2.CarritoScreen2
import com.example.vidasalud.ui.screens.registroDatos.RegistroDatosScreen
import com.example.vidasalud.ui.screens.social.FeedScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { user ->
                    val nombreEncoded = Uri.encode(user.nombre)
                    val rolEncoded = Uri.encode(user.rol)
                    // Al iniciar sesión, ahora vamos a HOME
                    navController.navigate("home/$nombreEncoded/$rolEncoded") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- RUTA HOME RESTAURADA ---
        composable(
            route = "home/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->
            val nombre = entry.arguments?.getString("nombre") ?: "Usuario"
            val rol = entry.arguments?.getString("rol") ?: "cliente"
            HomeScreen(navController = navController, userName = nombre, userRole = rol)
        }

        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = "registroDatos/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->
            val nombre = entry.arguments?.getString("nombre") ?: "Usuario"
            val rol = entry.arguments?.getString("rol") ?: "cliente"
            RegistroDatosScreen(navController = navController, userName = nombre, userRole = rol)
        }

        composable(
            route = "feed/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->
            val nombre = entry.arguments?.getString("nombre") ?: "Invitado"
            val rol = entry.arguments?.getString("rol") ?: "cliente"
            FeedScreen(currentUserName = nombre, currentIsAdmin = rol == "admin", onBack = { navController.popBackStack() })
        }

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
                navController = navController,
                nombre = nombre,
                rol = rol,
                onLogout = {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onVerCatalogo = {
                    navController.navigate("catalogo/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                },
                onGestionAdmin = {
                    navController.navigate("gestion_planes/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                },
                onGestionUsuarios = {
                    navController.navigate("gestion_usuarios/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                }
            )
        }

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
                navController = navController,
                nombre = nombre,
                rol = rol
            )
        }

        composable("carrito2") {
            CarritoScreen2()
        }

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

        composable(
            route = "gestion_planes/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->
            GestionPlanesScreen(onVolver = { navController.popBackStack() })
        }

        composable(
            route = "gestion_usuarios/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->
            GestionUsuariosScreen(onVolver = { navController.popBackStack() })
        }
    }
}