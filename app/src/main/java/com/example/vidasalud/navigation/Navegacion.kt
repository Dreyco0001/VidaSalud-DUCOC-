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
import com.example.vidasalud.ui.screens.gestionUsuarios.GestionUsuariosScreen
import com.example.vidasalud.ui.screens.home.HomeScreen
import com.example.vidasalud.ui.screens.login.LoginScreen
import com.example.vidasalud.ui.screens.pago.PagoConfirmacionScreen
import com.example.vidasalud.ui.screens.perfil.PerfilScreen
import com.example.vidasalud.ui.screens.registro.RegistroScreen
import com.example.vidasalud.ui.screens.registroDatos.RegistroDatosScreen
import com.example.vidasalud.ui.screens.social.FeedScreen
import com.example.vidasalud.viewmodel.GestionPlanesViewModel

@Composable
fun AppNavegacion() {

    val navController = rememberNavController()
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
                    // Navega a home pasando NOMBRE y ROL
                    val nombre = Uri.encode(user.nombre)
                    val rol = Uri.encode(user.rol)
                    navController.navigate("home/$nombre/$rol") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- RUTAS PRINCIPALES (AHORA TODAS RECIBEN NOMBRE Y ROL) ---

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
        ) {
            FeedScreen()
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
                navController = navController, // Parámetro añadido
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
                },
                onGestionUsuarios = {
                    navController.navigate("gestion_usuarios/${Uri.encode(nombre)}/${Uri.encode(rol)}")
                }
            )
        }


        // ... (El resto de las rutas no necesitan cambios)
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
                    Text("Error: parámetro \'nombre\' no recibido.")

                rol != "admin" ->
                    Text("Acceso denegado — Solo admin puede entrar.")

                else ->
                    GestionPlanesScreen(
                        onVolver = { navController.popBackStack() }
                    )
            }
        }

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

        composable(
            route = "gestion_usuarios/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entry ->

            val nombre = entry.arguments?.getString("nombre") ?: ""
            val rol = entry.arguments?.getString("rol") ?: "cliente"

            when {
                nombre.isBlank() ->
                    Text("Error: parámetro \'nombre\' no recibido.")

                rol != "admin" ->
                    Text("Acceso denegado — Solo admin puede entrar.")

                else ->
                    GestionUsuariosScreen(
                        onVolver = { navController.popBackStack() }
                    )
            }
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
    }
}
