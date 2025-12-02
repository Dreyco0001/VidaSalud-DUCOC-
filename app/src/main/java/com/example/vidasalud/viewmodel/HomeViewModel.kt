
package com.example.vidasalud.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Usuario
import com.example.vidasalud.repository.UsuarioRepository
import com.example.vidasalud.ui.screens.home.TipSalud
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usuarioRepository = UsuarioRepository()

    private val _user = MutableStateFlow<Usuario?>(null)
    val user: StateFlow<Usuario?> = _user.asStateFlow()

    private val _sleepData = MutableStateFlow<List<Float>>(emptyList())
    val sleepData: StateFlow<List<Float>> = _sleepData.asStateFlow()

    private val _dynamicHealthTip = MutableStateFlow<TipSalud?>(null)
    val dynamicHealthTip: StateFlow<TipSalud?> = _dynamicHealthTip.asStateFlow()


    init {
        fetchUserData()
        fetchSleepData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestore.collection("usuario").document(userId)
                    .addSnapshotListener { snapshot, _ ->
                        val user = snapshot?.toObject(Usuario::class.java)
                        _user.value = user
                        generateDynamicHealthTip(user)
                    }
            }
        }
    }

    private fun generateDynamicHealthTip(user: Usuario?) {
        if (user?.pesoActual == null || user.fechaPesoActual == null) {
            _dynamicHealthTip.value = null
            return
        }

        val pesoAnterior: Float?
        val fechaAnterior: String?

        if (user.pesoAnteanterior != null && user.fechaPesoAnteanterior != null) {
            pesoAnterior = user.pesoAnteanterior
            fechaAnterior = user.fechaPesoAnteanterior
        } else if (user.pesoAnterior != null && user.fechaPesoAnterior != null) {
            pesoAnterior = user.pesoAnterior
            fechaAnterior = user.fechaPesoAnterior
        } else {
            _dynamicHealthTip.value = null
            return
        }

        val weightDiff = user.pesoActual - pesoAnterior!!
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val dateActual = LocalDateTime.parse(user.fechaPesoActual, formatter).toLocalDate()
        val dateAnterior = LocalDateTime.parse(fechaAnterior!!, formatter).toLocalDate()
        val daysDiff = ChronoUnit.DAYS.between(dateAnterior, dateActual)

        val timeFormatted = when {
            daysDiff > 30 -> {
                val months = daysDiff / 30
                val remainingDays = daysDiff % 30
                if (remainingDays > 0) "$months meses y $remainingDays días" else "$months meses"
            }
            daysDiff > 7 -> {
                val weeks = daysDiff / 7
                val remainingDays = daysDiff % 7
                if (remainingDays > 0) "$weeks semanas y $remainingDays días" else "$weeks semanas"
            }
            else -> "$daysDiff días"
        }

        val message = if (weightDiff > 0) {
            "Has subido %.1f kg en %s".format(weightDiff, timeFormatted)
        } else {
            "Has perdido %.1f kg en %s".format(-weightDiff, timeFormatted)
        }

        _dynamicHealthTip.value = TipSalud(message, Color(0xFFD1C4E9))
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

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                usuarioRepository.updateWeight(userId, weight)
            }
        }
    }
}
