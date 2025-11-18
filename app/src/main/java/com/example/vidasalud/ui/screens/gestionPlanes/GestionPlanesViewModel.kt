package com.example.vidasalud.ui.screens.gestionPlanes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Plan
import com.example.vidasalud.repository.PlanesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestionPlanesViewModel(
    private val repository: PlanesRepository = PlanesRepository()
) : ViewModel() {

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
                val result = repository.obtenerPlanes()
                _planes.value = result.ifEmpty { emptyList() }

            } catch (e: Exception) {
                _error.value = "Error cargando planes: ${e.localizedMessage}"
                _planes.value = emptyList()
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
        imagenUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.crearPlan(nombre, duracion, nivel, objetivo, imagenUrl)

                if (result.isSuccess) {
                    obtenerPlanes()
                } else {
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "No se pudo crear el plan"
                }

            } catch (e: Exception) {
                _error.value = "Error creando plan: ${e.localizedMessage}"
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
        imagenUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.actualizarPlan(id, nombre, duracion, nivel, objetivo, imagenUrl)

                if (result.isSuccess) {
                    obtenerPlanes()
                } else {
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "No se pudo actualizar el plan"
                }

            } catch (e: Exception) {
                _error.value = "Error actualizando plan: ${e.localizedMessage}"
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
                    obtenerPlanes()
                } else {
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "No se pudo eliminar el plan"
                }

            } catch (e: Exception) {
                _error.value = "Error eliminando plan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
