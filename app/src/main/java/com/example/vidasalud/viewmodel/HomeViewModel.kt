
package com.example.vidasalud.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val _sleepData = mutableStateOf<List<Float>>(emptyList())
    val sleepData: State<List<Float>> = _sleepData

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchSleepData()
    }

    private fun fetchSleepData() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val userDocRef = firestore.collection("usuario").document(user.uid)
                try {
                    val document = userDocRef.get().await()
                    if (document.exists()) {
                        val days = listOf("SueñoL", "SueñoM", "SueñoX", "SueñoJ", "SueñoV", "SueñoS", "SueñoD")
                        val data = days.map { day ->
                            (document.getDouble(day) ?: 0.0).toFloat()
                        }
                        _sleepData.value = data
                    } else {
                        // El documento no existe, creamos los campos por defecto
                        val defaultSleepData = mapOf(
                            "SueñoL" to 0.0,
                            "SueñoM" to 0.0,
                            "SueñoX" to 0.0,
                            "SueñoJ" to 0.0,
                            "SueñoV" to 0.0,
                            "SueñoS" to 0.0,
                            "SueñoD" to 0.0
                        )
                        userDocRef.set(defaultSleepData, com.google.firebase.firestore.SetOptions.merge()).await()
                        _sleepData.value = defaultSleepData.values.map { it.toFloat() }
                    }
                } catch (e: Exception) {
                    // Handle exception
                }
            }
        }
    }
}
