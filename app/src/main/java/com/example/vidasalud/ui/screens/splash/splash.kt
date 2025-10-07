package com.example.vidasalud.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.vidasalud.R

/**
 * Convierte la animación CSS 'scale-in-center' a Jetpack Compose.
 * El logo aparece escalando desde 0.0 hasta 1.0 en 0.5s (500ms).
 *
 * @param onAnimationFinished (Opcional) Lambda a ejecutar cuando la animación termina.
 * * NOTA: Reemplaza R.drawable.your_logo_resource con el ID de tu recurso de logo.
 */
@Composable
fun SplashScreen(onAnimationFinished: (() -> Unit)? = null) {
    // 1. Estado para iniciar la animación (debe ser 'true' para empezar)
    var startAnimation by remember { mutableStateOf(false) }

    // 2. El valor que queremos animar: la Escala
    // Inicialmente es 0.0 (escondido), el destino es 1.0 (tamaño completo).
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f, // 0% (0) -> 100% (1)
        animationSpec = tween(
            durationMillis = 500, // Duración: 0.5s (500ms)
            easing = androidx.compose.animation.core.LinearEasing // Tipo: linear
        ), label = "ScaleAnimation"
    )

    // 3. Efecto que se lanza al inicio de la composición para:
    //    a) Iniciar la animación
    //    b) Notificar que la animación ha terminado (si se proporciona el callback)
    LaunchedEffect(key1 = true) {
        startAnimation = true
        // Espera la duración de la animación antes de llamar al callback
        if (onAnimationFinished != null) {
            delay(500)
            onAnimationFinished()
        }
    }

    // 4. Contenedor y aplicación de la animación
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            // El drawable de tu logo. ¡Asegúrate de cambiarlo!
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de la Aplicación",
            // Aplicamos el modificador .scale() con el valor animado
            modifier = Modifier
                .size(200.dp) // Define un tamaño fijo para el logo
                .scale(scaleAnim) // ¡Aquí aplicamos la animación!
            // La opacidad no necesita ser animada ya que siempre es 1 (por defecto) en Compose
        )
    }
}