package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {
    private val repositorio = UsuarioRepository()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    private val _errorMensaje = MutableStateFlow("")
    val errorMensaje: StateFlow<String> = _errorMensaje

    fun registroUsuario(correo: String, clave: String, confirmarClave: String, nombre: String) {
        if (correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty() || nombre.isEmpty()) {
            _errorMensaje.value = "Todos los campos son obligatorios"
            return
        }

        if (clave != confirmarClave) {
            _errorMensaje.value = "Las contraseñas no coinciden"
            return
        }

        if (clave.length < 6) {
            _errorMensaje.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        _cargando.value = true
        _errorMensaje.value = ""

        viewModelScope.launch {
            _cargando.value = true
            _errorMensaje.value = "" // Limpiar errores previos

            // Llamar a la función del repositorio que hemos creado
            repositorio.registrarUsuario(correo, clave, nombre)
                .onSuccess {
                    // Si el registro y guardado en Firestore fue exitoso
                    _registroExitoso.value = true
                }
                .onFailure { exception ->
                    // Si hubo un error (ej: email ya en uso, no hay internet, etc.)
                    _errorMensaje.value = exception.message ?: "Error desconocido en el registro"
                }

            _cargando.value = false
        }
    }

    fun limpiarRegistro() {
        _registroExitoso.value = false
        _errorMensaje.value = ""
    }
}