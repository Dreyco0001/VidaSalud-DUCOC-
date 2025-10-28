package com.example.vidasalud.ui.screens.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vidasalud.viewmodel.CarritoViewModel

@Composable
fun CarritoScreen(
    rol: String = "cliente",
    nombre: String = "Cliente",
    onVolverAlCatalogo: () -> Unit = {},
    onConfirmarPago: () -> Unit = {},
    onLogout: () -> Unit = {},
    onVerPerfil: () -> Unit = {},
    viewModel: CarritoViewModel
) {
    val carrito by viewModel.carrito.collectAsState()
    val total by remember { derivedStateOf { viewModel.obtenerTotal() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔹 HEADER con usuario, perfil y logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onVolverAlCatalogo) {
                Text("← Volver al Catálogo")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Hola, $nombre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onVerPerfil) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }

                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Delete, contentDescription = "Cerrar Sesión", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Título
        Text(
            "Mi Carrito",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (carrito.isEmpty()) {
            // 🔹 Carrito vacío
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Carrito vacío",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "El carrito está vacío",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // 🔹 Lista de productos
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(carrito, key = { it.producto.id }) { item ->
                    ItemCarrito(
                        item = item,
                        onEliminar = { viewModel.eliminarProductoDelCarrito(item.producto) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Resumen del pedido
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Productos:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            carrito.sumOf { it.cantidad }.toString(),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$${String.format("%.2f", total)}",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.vaciarCarrito() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Vaciar carrito",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vaciar Todo")
                        }

                        Button(
                            onClick = {
                                viewModel.confirmarCompra()
                                onConfirmarPago()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Confirmar Compra")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCarrito(
    item: com.example.vidasalud.model.ItemCarrito,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.producto.nombre, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Precio: $${item.producto.precio} c/u")
                Text("Cantidad: ${item.cantidad}")
                Text(
                    "Subtotal: $${String.format("%.2f", item.producto.precio * item.cantidad)}",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar producto", tint = Color.Red)
            }
        }
    }
}
