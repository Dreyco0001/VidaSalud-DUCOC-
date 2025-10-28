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

        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register")
                },
                onLoginSuccess = { user ->
                    // Navegar según el rol pasando el nombre como parámetro
                    when (user.rol) {
                        "admin" -> navController.navigate("perfil_admin/${user.nombre}")
                       // modificar este pedazo para que mande a catalogo, tambien nos toca agregar catalogo como opcion
                        else -> navController.navigate("perfil_cliente/${user.nombre}")
                    }
                }
            )
        }


        composable("register") {
            //el registro no guarda de momento en el que escribi el comentario
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            "perfil_admin/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Administrador"
            PerfilAdminScreen(
                nombre = nombre,
                onLogout = {
                    // Volver al login limpiando el back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true}
                    }
                }
            )
        }


        composable(
            "perfil_cliente/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Cliente"
            PerfilClienteScreen(
                nombre = nombre,
                onLogout = {

                    // Volver al login limpiando el back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }, //Agregar navegacion al carrito
                onVerCarrito = {
                    navController.navigate("carrito")
                },

                viewModel = carritoViewModel
            )
        }

        //Agregar pantalla de carrito
        composable("carrito") {




            CarritoScreen(
                onVolverAlCatalogo = {
                    navController.popBackStack()
                },
                onConfirmarPago = {
                    navController.navigate("pago") {
                        popUpTo("perfil_cliente") { inclusive = false }
                    }
                },
                viewModel = carritoViewModel
            )
        }

        // Agregar pantalla de confirmación de pago
        composable("pago") { backStackEntry ->
            // Obtener el nombre del usuario de alguna manera
            // Por ahora usamos un valor por defecto


            PagoConfirmacionScreen(
                nombreUsuario = "Cliente",
                onVolverAlCatalogo = {
                    navController.navigate("perfil_cliente/Cliente") {
                        popUpTo("login") { inclusive = false}
                    }
                }
            )
        }
    }
}