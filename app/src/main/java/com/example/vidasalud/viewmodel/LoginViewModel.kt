package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Usuario
import com.example.vidasalud.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repositorio = AuthRepository()
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _cargaLogin = MutableStateFlow(false)
    val cargaLogin: StateFlow<Boolean> = _cargaLogin

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    fun login(correo: String, clave: String) {
        viewModelScope.launch {
            _cargaLogin.value = true
            try {
                _usuario.value = repositorio.login(correo, clave)
            } catch (e: FirebaseAuthInvalidUserException) {
                _loginError.value = "El usuario no existe"
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _loginError.value = "La contraseña es incorrecta"
            } catch (e: Exception) {
                _loginError.value = "Ocurrió un error inesperado"
            } finally {
                _cargaLogin.value = false
            }
        }
    }

    fun clearUser() {
        _usuario.value = null
    }

    fun clearError() {
        _loginError.value = null
    }
}
