package com.example.vidasalud.viewmodel

import android.util.Log
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
                Log.d("VM_USUARIOS", "Solicitando usuarios al repo...")

                val result = repository.obtenerUsuarios()

                if (result.isSuccess) {

                    val listaMap = result.getOrNull() ?: emptyList()
                    Log.d("VM_USUARIOS", "Lista recibida: ${listaMap.size} usuarios")

                    val listaUsuarios = listaMap.map { map ->
                        Log.d("VM_USUARIOS", "Usuario bruto Firestore: $map")

                        Usuario(
                            uid = map["uid"]?.toString() ?: "",
                            correo = map["correo"]?.toString() ?: "",
                            nombre = map["nombre"]?.toString() ?: "",
                            clave = map["clave"]?.toString() ?: "",
                            rol = map["rol"]?.toString() ?: "",
                            fotoUrl = when {
                                map["fotoUrl"]?.toString()?.isNotEmpty() == true -> map["fotoUrl"].toString()
                                map["fotoPerfil"]?.toString()?.isNotEmpty() == true -> map["fotoPerfil"].toString()
                                else -> null
                            }
                        )
                    }

                    _usuarios.value = listaUsuarios

                } else {
                    val ex = result.exceptionOrNull()
                    Log.e("VM_USUARIOS", "Falla del repo: ", ex)
                    _error.value = "Error cargando usuarios 1: ${ex?.message ?: "sin detalle"}"
                }

            } catch (e: Exception) {
                Log.e("VM_USUARIOS", "EXCEPCIÓN en obtenerUsuarios()", e)
                _error.value = "Error cargando usuarios 2: ${e.localizedMessage}"
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
                    _error.value = result.exceptionOrNull()?.message ?: "Error creando usuario"
                }

            } catch (e: Exception) {
                _error.value = "Error creando usuario: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarUsuario(
        uid: String,
        nombre: String? = null,
        clave: String? = null,
        rol: String? = null,
        fotoUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updates = mutableMapOf<String, Any?>()

                nombre?.let { updates["nombre"] = it }
                clave?.let { updates["clave"] = it }
                rol?.let { updates["rol"] = it }
                fotoUrl?.let { updates["fotoUrl"] = it }

                if (updates.isEmpty()) {
                    _error.value = "No hay cambios para actualizar"
                    return@launch
                }

                val result = repository.actualizarUsuarioPorUid(
                    uid = uid,
                    updates = updates
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




    fun eliminarUsuario(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.eliminarUsuario(uid)

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
