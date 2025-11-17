package com.example.vidasalud.ui.screens.gestionPlanes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.repository.PlanEjercicio
import com.example.vidasalud.repository.PlanesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestionPlanesViewModel(
    private val repository: PlanesRepository = PlanesRepository()
) : ViewModel() {

    private val _planes = MutableStateFlow<List<PlanEjercicio>>(emptyList())
    val planes: StateFlow<List<PlanEjercicio>> = _planes

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
                _planes.value = result ?: emptyList()  // 🔒 Nunca null
            } catch (e: Exception) {
                _error.value = "Error al cargar planes: ${e.localizedMessage ?: "Error desconocido"}"
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
                    obtenerPlanes()
                } else {
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                }

            } catch (e: Exception) {
                _error.value = "Error al crear plan: ${e.localizedMessage ?: "Error desconocido"}"
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
                    obtenerPlanes()
                } else {
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                }

            } catch (e: Exception) {
                _error.value = "Error al actualizar plan: ${e.localizedMessage ?: "Error desconocido"}"
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
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                }

            } catch (e: Exception) {
                _error.value = "Error al eliminar plan: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
