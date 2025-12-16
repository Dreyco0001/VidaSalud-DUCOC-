package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.NotificacionPlan
import com.example.vidasalud.model.Plan
import com.example.vidasalud.repository.NotificacionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificacionesViewModel(
    private val repo: NotificacionesRepository = NotificacionesRepository()
) : ViewModel() {

    private val _notificaciones = MutableStateFlow<List<NotificacionPlan>>(emptyList())
    val notificaciones: StateFlow<List<NotificacionPlan>> = _notificaciones

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() {
        _error.value = null
    }

    // --------------------------------------------------
    // CARGAR NOTIFICACIONES
    // --------------------------------------------------
    fun cargarNotificaciones(rol: String) {
        viewModelScope.launch {
            try {
                _notificaciones.value = repo.obtenerNotificaciones(rol)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar notificaciones"
            }
        }
    }

    // --------------------------------------------------
    // TOMAR PLAN
    // --------------------------------------------------
    fun tomarPlan(
        plan: Plan,
        rolUsuario: String,
        onOk: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.tomarPlan(plan, rolUsuario)
            if (result.isSuccess) {
                onOk()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    // --------------------------------------------------
    // CANCELAR PLAN
    // --------------------------------------------------
    fun cancelarPlan(
        id: String,
        rol: String,
        onOk: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.cancelarPlan(id, rol)
            if (result.isSuccess) {
                onOk()
                cargarNotificaciones(rol)
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    // --------------------------------------------------
    // ELIMINAR NOTIFICACIÓN (ADMIN O DUEÑO)
    // --------------------------------------------------
    fun eliminarNotificacion(
        id: String,
        rol: String,
        onOk: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.eliminarNotificacion(id, rol)
            if (result.isSuccess) {
                onOk()
                cargarNotificaciones(rol)
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}
