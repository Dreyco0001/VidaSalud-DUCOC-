package com.example.vidasalud.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.viewmodel.CarritoViewModel

@Composable
fun CatalogoScreen(
    viewModel: CarritoViewModel = viewModel(),
    rol: String = "cliente",        // "admin" o "cliente"
    nombre: String = "Cliente",
    onVerPerfil: () -> Unit = {},
    onLogout: () -> Unit = {},
    onVerCarrito: () -> Unit = {}
) {
    val productos by viewModel.productos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val carrito by viewModel.carrito.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con botones globales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Catálogo de Productos",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón perfil
                Button(
                    onClick = onVerPerfil,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Perfil")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón logout
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Cerrar sesión")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón carrito con badge
                BadgedBox(
                    badge = {
                        if (carrito.isNotEmpty()) {
                            Badge { Text(carrito.sumOf { it.cantidad }.toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onVerCarrito) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(productos) { producto ->
                    ProductoItem(
                        producto = producto,
                        onAgregar = { viewModel.agregarAlCarrito(producto) },
                        onEliminar = { viewModel.removerDelCarrito(producto) },
                        cantidadEnCarrito = carrito.find { it.producto.id == producto.id }?.cantidad ?: 0
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun ButtonDefaults.buttonColors(containerColor: Any): ButtonColors {
    return TODO("Provide the return value")
}

@Composable
fun Color(x0: Long) {
    TODO("Not yet implemented")
}

@Composable
fun ProductoItem(
    producto: com.example.vidasalud.model.Producto,
    onAgregar: () -> Unit,
    onEliminar: () -> Unit,
    cantidadEnCarrito: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${producto.precio}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (cantidadEnCarrito > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }

                        Text(
                            text = cantidadEnCarrito.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = onAgregar, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }
                } else {
                    Button(onClick = onAgregar) {
                        Text("Agregar al carrito")
                    }
                }
            }
        }
    }
}
