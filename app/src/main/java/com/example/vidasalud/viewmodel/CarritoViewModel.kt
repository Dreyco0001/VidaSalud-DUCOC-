package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.ItemCarrito
import com.example.vidasalud.model.Producto
import com.example.vidasalud.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarritoViewModel : ViewModel() {
    private val repository = ProductoRepository()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _carrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val carrito: StateFlow<List<ItemCarrito>> = _carrito

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private var ultimoDocumento: Any? = null
    private val limiteProductos = 10

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val resultado = repository.obtenerProductos(limite = limiteProductos)
                _productos.value = resultado.productos
                ultimoDocumento = resultado.ultimoDocumento
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cargarMasProductos() {
        if (_cargando.value) return

        _cargando.value = true
        viewModelScope.launch {
            try {
                val resultado = repository.obtenerMasProductos(
                    limite = limiteProductos,
                    ultimoDocumento = ultimoDocumento
                )
                if (resultado.productos.isNotEmpty()) {
                    _productos.value = _productos.value + resultado.productos
                    ultimoDocumento = resultado.ultimoDocumento
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _cargando.value = false
            }
        }
    }

    fun agregarAlCarrito(producto: Producto) {
        viewModelScope.launch {
            try {
                // Verificar stock actualizado
                val productoActualizado = repository.obtenerProductoPorId(producto.id)
                if (productoActualizado == null || productoActualizado.stock <= 0) return@launch

                val carritoActual = _carrito.value.toMutableList()
                val itemExistente = carritoActual.find { it.producto.id == producto.id }

                // Verificar stock disponible
                val stockDisponible = productoActualizado.stock - (itemExistente?.cantidad ?: 0)
                if (stockDisponible <= 0) return@launch

                if (itemExistente != null) {
                    itemExistente.cantidad++
                    // Actualizar stock en Firestore
                    repository.actualizarStock(producto.id, productoActualizado.stock - 1)
                } else {
                    carritoActual.add(ItemCarrito(producto = productoActualizado, cantidad = 1))
                    // Actualizar stock en Firestore
                    repository.actualizarStock(producto.id, productoActualizado.stock - 1)
                }

                _carrito.value = carritoActual
                actualizarProductosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun removerDelCarrito(producto: Producto) {
        viewModelScope.launch {
            try {
                val carritoActual = _carrito.value.toMutableList()
                val itemExistente = carritoActual.find { it.producto.id == producto.id }

                if (itemExistente != null) {
                    if (itemExistente.cantidad > 1) {
                        itemExistente.cantidad--
                        // Aumentar stock en Firestore
                        val productoActualizado = repository.obtenerProductoPorId(producto.id)
                        if (productoActualizado != null) {
                            repository.actualizarStock(producto.id, productoActualizado.stock + 1)
                        }
                    } else {
                        carritoActual.remove(itemExistente)
                        // Aumentar stock en Firestore
                        val productoActualizado = repository.obtenerProductoPorId(producto.id)
                        if (productoActualizado != null) {
                            repository.actualizarStock(producto.id, productoActualizado.stock + 1)
                        }
                    }
                }

                _carrito.value = carritoActual
                actualizarProductosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun eliminarProductoDelCarrito(producto: Producto) {
        viewModelScope.launch {
            try {
                val carritoActual = _carrito.value.toMutableList()
                val itemExistente = carritoActual.find { it.producto.id == producto.id }

                if (itemExistente != null) {
                    carritoActual.remove(itemExistente)
                    // Restaurar todo el stock en Firestore
                    val productoActualizado = repository.obtenerProductoPorId(producto.id)
                    if (productoActualizado != null) {
                        repository.actualizarStock(
                            producto.id,
                            productoActualizado.stock + itemExistente.cantidad
                        )
                    }
                }

                _carrito.value = carritoActual
                actualizarProductosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun vaciarCarrito() {
        viewModelScope.launch {
            try {
                // Restaurar stock de todos los productos en el carrito
                _carrito.value.forEach { item ->
                    val productoActualizado = repository.obtenerProductoPorId(item.producto.id)
                    if (productoActualizado != null) {
                        repository.actualizarStock(
                            item.producto.id,
                            productoActualizado.stock + item.cantidad
                        )
                    }
                }

                _carrito.value = emptyList()
                actualizarProductosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun confirmarCompra() {
        viewModelScope.launch {
            try {
                // Aquí puedes implementar la lógica de confirmación de compra
                // Por ahora solo limpiamos el carrito
                _carrito.value = emptyList()
                // Recargar productos para actualizar stocks
                cargarProductos()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    private suspend fun actualizarProductosEnCatalogo() {
        // Recargar productos para reflejar cambios de stock
        val resultado = repository.obtenerProductos(limite = _productos.value.size + limiteProductos)
        _productos.value = resultado.productos
        ultimoDocumento = resultado.ultimoDocumento
    }

    fun obtenerTotal(): Double {
        return _carrito.value.sumOf { it.producto.precio * it.cantidad }
    }
}