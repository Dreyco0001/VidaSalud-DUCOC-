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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.model.Producto
import com.example.vidasalud.viewmodel.CarritoViewModel

@Composable
fun CatalogoScreen(
    viewModel: CarritoViewModel = viewModel(),
    nombre: String = "Cliente",
    rol: String = "cliente",
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
        // Header principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hola, $nombre 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón perfil
                Button(
                    onClick = onVerPerfil,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) { Text("Perfil", fontSize = 14.sp) }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón cerrar sesión
                Button(
                    onClick = onLogout,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Cerrar sesión", fontSize = 14.sp) }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón carrito
                BadgedBox(
                    badge = {
                        if (carrito.isNotEmpty()) {
                            Badge { Text(carrito.sumOf { it.cantidad }.toString()) }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onVerCarrito,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de gestión para admin
        if (rol.equals("admin", ignoreCase = true)) {
            Button(
                onClick = { /* Abrir gestión de productos */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
            ) {
                Text("🛠️ Gestionar productos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contenido principal: lista de productos
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos) { producto ->
                    ProductoItem(
                        producto = producto,
                        onAgregar = { viewModel.agregarAlCarrito(producto) },
                        onEliminar = { viewModel.removerDelCarrito(producto) },
                        cantidadEnCarrito = carrito.find { it.producto.id == producto.id }?.cantidad ?: 0
                    )
                }
            }
        }
    }
}

@Composable
fun ProductoItem(
    producto: Producto,
    onAgregar: () -> Unit,
    onEliminar: () -> Unit,
    cantidadEnCarrito: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                producto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$${producto.precio}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (cantidadEnCarrito > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                        Text(
                            cantidadEnCarrito.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onAgregar, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }
                } else {
                    Button(
                        onClick = onAgregar,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Agregar al carrito", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
