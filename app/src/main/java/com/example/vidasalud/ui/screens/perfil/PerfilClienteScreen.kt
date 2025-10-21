package com.example.vidasalud.ui.screens.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidasalud.viewmodel.CarritoViewModel

@Composable
fun PerfilClienteScreen(
    nombre: String = "Cliente",
    onLogout: () -> Unit = {},
    onVerCarrito: () -> Unit = {},
    viewModel: CarritoViewModel
) {
    val productos by viewModel.productos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val carrito by viewModel.carrito.collectAsState()

    // Scroll infinito - cargar más productos cuando se llega al final
    val lazyListState = rememberLazyListState()

    // Observar cuando llegamos al final de la lista
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItem = visibleItems.last()
                    val loadMoreThreshold = 5 // Cargar más cuando faltan 5 elementos

                    if (lastVisibleItem.index >= productos.size - loadMoreThreshold) {
                        viewModel.cargarMasProductos()
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header fijo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Información del perfil y carrito
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Bienvenido $nombre",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            "Rol: Cliente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    // Botón del carrito con badge
                    BadgedBox(
                        badge = {
                            val cantidadTotal = carrito.sumOf { it.cantidad }
                            if (cantidadTotal > 0) {
                                Badge {
                                    Text(cantidadTotal.toString())
                                }
                            }
                        }
                    ) {
                        FilledTonalButton(
                            onClick = onVerCarrito,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Carrito")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de cerrar sesión
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Título del catálogo
        Text(
            "Catálogo de Productos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de productos con scroll infinito
        if (cargando && productos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (productos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay productos disponibles")
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos) { producto ->
                    ItemProducto(
                        producto = producto,
                        cantidadEnCarrito = carrito.find { it.producto.id == producto.id }?.cantidad ?: 0,
                        onAgregar = {
                            if (producto.stock > 0) {
                                viewModel.agregarAlCarrito(producto)
                            }
                        },
                        onRemover = { viewModel.removerDelCarrito(producto) }
                    )
                }

                // Indicador de carga para scroll infinito
                item {
                    if (cargando && productos.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemProducto(
    producto: com.example.vidasalud.model.Producto,
    cantidadEnCarrito: Int,
    onAgregar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${"%.2f".format(producto.precio)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar stock y estado
            if (producto.stock <= 0) {
                Text(
                    text = "Agotado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            } else if (producto.stock < 10) {
                Text(
                    text = "Últimas ${producto.stock} unidades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Stock disponible: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controles de cantidad
            if (cantidadEnCarrito > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "En carrito: $cantidadEnCarrito",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onRemover,
                            modifier = Modifier.size(36.dp),
                            enabled = cantidadEnCarrito > 0
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Quitar uno")
                        }

                        IconButton(
                            onClick = onAgregar,
                            modifier = Modifier.size(36.dp),
                            enabled = producto.stock > cantidadEnCarrito
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar uno")
                        }
                    }
                }
            } else {
                Button(
                    onClick = onAgregar,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = producto.stock > 0
                ) {
                    Text(if (producto.stock > 0) "Agregar al Carrito" else "Agotado")
                }
            }
        }
    }
}