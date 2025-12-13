package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Usuario
import com.example.vidasalud.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repositorio = AuthRepository()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _cargaLogin = MutableStateFlow(false)
    val cargaLogin: StateFlow<Boolean> = _cargaLogin

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin

    fun login(correo: String, clave: String) {
        viewModelScope.launch {
            _cargaLogin.value = true
            _errorLogin.value = null

            try {
                val result = repositorio.login(correo, clave)

                if (result == null) {
                    _usuario.value = null
                    _errorLogin.value = "Correo o contraseña incorrectos"
                } else {
                    _usuario.value = result
                }

            } catch (e: Exception) {
                // 🔥 AQUÍ ESTABA EL CRASH
                _usuario.value = null
                _errorLogin.value = "Correo o contraseña incorrectos"
            } finally {
                _cargaLogin.value = false
            }
        }
    }

    fun clearError() {
        _errorLogin.value = null
    }

    fun clearUser() {
        _usuario.value = null
    }
}
