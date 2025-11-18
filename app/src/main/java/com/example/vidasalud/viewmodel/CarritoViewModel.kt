package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.ItemCarrito
import com.example.vidasalud.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarritoViewModel : ViewModel() {

    private val _carrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val carrito: StateFlow<List<ItemCarrito>> = _carrito

    // Agregar producto al carrito
    fun agregarAlCarrito(producto: Producto) {
        viewModelScope.launch {
            val lista = _carrito.value.toMutableList()
            val itemExistente = lista.find { it.producto.id == producto.id }

            if (itemExistente != null) {
                itemExistente.cantidad++
            } else {
                lista.add(ItemCarrito(producto, cantidad = 1))
            }

            _carrito.value = lista
        }
    }

    // Remover 1 unidad del carrito
    fun removerDelCarrito(producto: Producto) {
        viewModelScope.launch {
            val lista = _carrito.value.toMutableList()
            val itemExistente = lista.find { it.producto.id == producto.id }

            if (itemExistente != null) {
                if (itemExistente.cantidad > 1) {
                    itemExistente.cantidad--
                } else {
                    lista.remove(itemExistente)
                }
            }

            _carrito.value = lista
        }
    }

    // Eliminar totalmente un producto del carrito
    fun eliminarProductoDelCarrito(producto: Producto) {
        viewModelScope.launch {
            val lista = _carrito.value.toMutableList()
            lista.removeAll { it.producto.id == producto.id }
            _carrito.value = lista
        }
    }

    // Vaciar carrito
    fun vaciarCarrito() {
        viewModelScope.launch {
            _carrito.value = emptyList()
        }
    }

    // Confirmar compra (solo limpia el carrito por ahora)
    fun confirmarCompra() {
        viewModelScope.launch {
            _carrito.value = emptyList()
        }
    }

    // Total del carrito
    fun obtenerTotal(): Double {
        return _carrito.value.sumOf { it.producto.precio * it.cantidad }
    }

    // Cantidad total de items
    fun cantidadTotalItems(): Int {
        return _carrito.value.sumOf { it.cantidad }
    }
}
