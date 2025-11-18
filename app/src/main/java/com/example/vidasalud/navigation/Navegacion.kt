package com.example.vidasalud.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vidasalud.ui.screens.carrito.CarritoScreen
import com.example.vidasalud.ui.screens.catalogo.CatalogoScreen
import com.example.vidasalud.ui.screens.gestionPlanes.GestionPlanesScreen
import com.example.vidasalud.ui.screens.login.LoginScreen
import com.example.vidasalud.ui.screens.pago.PagoConfirmacionScreen
import com.example.vidasalud.ui.screens.perfil.PerfilScreen
import com.example.vidasalud.ui.screens.registro.RegistroScreen
import com.example.vidasalud.ui.screens.gestionPlanes.GestionPlanesViewModel

@Composable
fun AppNavegacion() {

    val navController = rememberNavController()

    // 👉 ESTE es ahora el único ViewModel global
    val planesViewModel: GestionPlanesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { user ->
                    val nombre = Uri.encode(user.nombre)
                    val rol = Uri.encode(user.rol)
                    navController.navigate("perfil/$nombre/$rol") {
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

        // PERFIL
        composable(
            route = "perfil/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->
            val nombre = entry.arguments?.getString("nombre") ?: "Usuario"
            val rol = entry.arguments?.getString("rol") ?: "cliente"

            PerfilScreen(
                nombre = nombre,
                rol = rol,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerCatalogo = {
                    navController.navigate("catalogo/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                },
                onGestionAdmin = {
                    navController.navigate("gestion_planes/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                }
            )
        }

        // GESTIÓN DE PLANES
        composable(
            route = "gestion_planes/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->

            val nombre = rememberSaveable { entry.arguments?.getString("nombre") ?: "" }
            val rol = rememberSaveable { entry.arguments?.getString("rol") ?: "cliente" }

            when {
                nombre.isBlank() ->
                    Text("Error: parámetro 'nombre' no recibido.")

                rol != "admin" ->
                    Text("Acceso denegado — Solo admin puede entrar.")

                else ->
                    GestionPlanesScreen(
                        onVolver = { navController.popBackStack() }
                    )
            }
        }

        // CATALOGO
        composable(
            route = "catalogo/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->

            val nombre = entry.arguments?.getString("nombre") ?: "Cliente"
            val rol = entry.arguments?.getString("rol") ?: "cliente"

            CatalogoScreen(
                nombre = nombre,
                rol = rol,
                onVerPerfil = { navController.navigate("perfil/$nombre/$rol") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerCarrito = {
                    navController.navigate("carrito/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                }
            )
        }

        // CARRITO — AHORA USA GestionPlanesViewModel
        composable(
            route = "carrito/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->

            val nombre = entry.arguments?.getString("nombre") ?: "Usuario"
            val rol = entry.arguments?.getString("rol") ?: "cliente"

            CarritoScreen(
                nombre = nombre,
                rol = rol,

                // 👉 reemplazo total: ahora recibe tu VIEWMODEL PRINCIPAL
                viewModel = planesViewModel,

                onVerPerfil = { navController.navigate("perfil/$nombre/$rol") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVerDetallePlan = { plan ->
                    println("Detalle plan: ${plan.nombre}")
                },
                onModificarPlan = { plan ->
                    println("Modificar plan: ${plan.nombre}")
                },
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
