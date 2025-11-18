package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Usuario
import com.example.vidasalud.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestionUsuariosViewModel(
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        obtenerUsuarios()
    }

    fun obtenerUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.obtenerUsuarios()  // Result<List<Map<String, Any>>>

                if (result.isSuccess) {
                    val listaMap = result.getOrNull() ?: emptyList()

                    // Convertir Map → Usuario
                    val listaUsuarios = listaMap.map { data ->
                        Usuario(
                            correo = data["correo"] as? String ?: "",
                            nombre = data["nombre"] as? String ?: "",
                            clave = data["clave"] as? String ?: "",
                            rol = data["rol"] as? String ?: "",
                            fotoUrl = data["fotoUrl"] as? String
                        )
                    }

                    _usuarios.value = listaUsuarios
                } else {
                    _error.value = "Error cargando usuarios"
                }

            } catch (e: Exception) {
                _error.value = "Error cargando usuarios: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearUsuario(
        correo: String,
        clave: String,
        nombre: String,
        rol: String,
        fotoUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.crearUsuario(
                    correo = correo,
                    clave = clave,
                    nombre = nombre,
                    rol = rol,
                    fotoUrl = fotoUrl
                )

                if (result.isSuccess) {
                    obtenerUsuarios()
                } else {
                    _error.value =
                        result.exceptionOrNull()?.message ?: "Error creando usuario"
                }

            } catch (e: Exception) {
                _error.value = "Error creando usuario: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarUsuario(
        correo: String,
        nombre: String? = null,
        clave: String? = null,
        rol: String? = null,
        fotoUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.actualizarUsuarioPorCorreo(
                    correo = correo,
                    nombre = nombre,
                    clave = clave,
                    rol = rol,
                    fotoUrl = fotoUrl
                )

                if (result.isSuccess) {
                    obtenerUsuarios()
                } else {
                    _error.value = "Error actualizando usuario"
                }

            } catch (e: Exception) {
                _error.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarUsuario(correo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.eliminarUsuario(correo)

                if (result.isSuccess) {
                    obtenerUsuarios()
                } else {
                    _error.value = "No se pudo eliminar el usuario"
                }

            } catch (e: Exception) {
                _error.value = "Error eliminando usuario: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
