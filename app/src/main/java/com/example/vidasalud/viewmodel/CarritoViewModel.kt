package com.example.vidasalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import com.example.vidasalud.repository.PlanesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarritoViewModel : ViewModel() {

    private val repo = PlanesRepository()

    // ----- ESTADOS DEL PLAN -----

    private val _likes = MutableStateFlow<List<Like>>(emptyList())
    val likes: StateFlow<List<Like>> = _likes

    private val _comentarios = MutableStateFlow<List<Comentario>>(emptyList())
    val comentarios: StateFlow<List<Comentario>> = _comentarios

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    // ---------------------------------------------------------
    //                CARGAR INFORMACIÓN DEL PLAN
    // ---------------------------------------------------------

    fun cargarLikes(planId: String) {
        viewModelScope.launch {
            try {
                _likes.value = repo.obtenerLikes(planId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun cargarComentarios(planId: String) {
        viewModelScope.launch {
            try {
                _comentarios.value = repo.obtenerComentarios(planId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    // ---------------------------------------------------------
    //                          LIKES
    // ---------------------------------------------------------

    fun darLike(planId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultado = repo.agregarLike(planId, userId)

            if (resultado.isSuccess) {
                cargarLikes(planId)
            } else {
                _error.value = resultado.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }

    fun quitarLike(planId: String, likeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultado = repo.quitarLike(planId, likeId)

            if (resultado.isSuccess) {
                cargarLikes(planId)
            } else {
                _error.value = resultado.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }


    // ---------------------------------------------------------
    //                      COMENTARIOS
    // ---------------------------------------------------------

    fun agregarComentario(planId: String, comentario: Comentario) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultado = repo.agregarComentario(planId, comentario)

            if (resultado.isSuccess) {
                cargarComentarios(planId)
            } else {
                _error.value = resultado.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }

    fun eliminarComentario(planId: String, comentarioId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultado = repo.eliminarComentario(planId, comentarioId)

            if (resultado.isSuccess) {
                cargarComentarios(planId)
            } else {
                _error.value = resultado.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }
}
