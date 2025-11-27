
package com.example.vidasalud.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RegistroDatosViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _saveStatus = mutableStateOf<String?>(null)
    val saveStatus: State<String?> = _saveStatus

    fun saveSleepData(day: String, hours: Float) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val userDocRef = firestore.collection("usuario").document(user.uid)
                val fieldName = "Sueño${day.first()}"
                try {
                    userDocRef.update(fieldName, hours.toDouble()).await()
                    _saveStatus.value = "¡Guardado con éxito!"
                } catch (e: Exception) {
                    _saveStatus.value = "Error al guardar: ${e.message}"
                }
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}
