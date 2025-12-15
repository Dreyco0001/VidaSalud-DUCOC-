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

    // --------------------------------------------------
    // LIMPIAR ERROR (🔥 ESTO SOLUCIONA TU CRASH)
    // --------------------------------------------------
    fun clearError() {
        _error.value = null
    }

    // --------------------------------------------------
    // CARGAR NOTIFICACIONES
    // ADMIN: TODAS | CLIENTE: SOLO LAS SUYAS
    // --------------------------------------------------
    fun cargarNotificaciones(rol: String) {
        viewModelScope.launch {
            try {
                _notificaciones.value = repo.obtenerNotificaciones(rol)
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error desconocido"
            }
        }
    }

    // --------------------------------------------------
    // TOMAR PLAN (CLIENTE O ADMIN)
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
    // CANCELAR PLAN (ANTES DE 12 HORAS)
    // --------------------------------------------------
    fun cancelarPlan(id: String, rol: String) {
        viewModelScope.launch {
            val result = repo.cancelarPlan(id, rol)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            } else {
                cargarNotificaciones(rol)
            }
        }
    }

    // --------------------------------------------------
    // ELIMINAR PLAN (SOLO ADMIN)
    // --------------------------------------------------
    fun eliminarPlanAdmin(id: String, rol: String) {
        viewModelScope.launch {
            if (rol != "admin") return@launch

            val result = repo.eliminarNotificacion(id, rol)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            } else {
                cargarNotificaciones(rol)
            }
        }
    }
}
