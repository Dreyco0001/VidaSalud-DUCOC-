package com.example.vidasalud.ui.screens.gestionPlanes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Plan
import com.example.vidasalud.repository.PlanesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestionPlanesViewModel(private val repository: PlanesRepository = PlanesRepository()) : ViewModel() {

    private val _planes = MutableStateFlow<List<Plan>>(emptyList())
    val planes: StateFlow<List<Plan>> = _planes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        obtenerPlanes()
    }

    fun obtenerPlanes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _planes.value = repository.obtenerPlanes().map {
                    Plan(
                        id = it.id,
                        nombre = it.nombre,
                        duracion = it.duracion,
                        nivel = it.nivel,
                        objetivo = it.objetivo,
                        imagenUrl = it.imagenUrl
                    )
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar planes: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearPlan(
        nombre: String,
        duracion: Int,
        nivel: String,
        objetivo: String,
        imagenBytes: ByteArray? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.crearPlan(nombre, duracion, nivel, objetivo, imagenBytes)
                if (result.isSuccess) {
                    obtenerPlanes() // Refresh list
                } else {
                    _error.value = "Error al crear plan: ${result.exceptionOrNull()?.localizedMessage}"
                }
            } catch (e: Exception) {
                _error.value = "Error al crear plan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarPlan(
        id: String,
        nombre: String? = null,
        duracion: Int? = null,
        nivel: String? = null,
        objetivo: String? = null,
        imagenBytes: ByteArray? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.actualizarPlan(id, nombre, duracion, nivel, objetivo, imagenBytes)
                if (result.isSuccess) {
                    obtenerPlanes() // Refresh list
                } else {
                    _error.value = "Error al actualizar plan: ${result.exceptionOrNull()?.localizedMessage}"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar plan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarPlan(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.eliminarPlan(id)
                if (result.isSuccess) {
                    obtenerPlanes() // Refresh list
                } else {
                    _error.value = "Error al eliminar plan: ${result.exceptionOrNull()?.localizedMessage}"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar plan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
