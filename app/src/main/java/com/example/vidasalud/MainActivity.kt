package com.example.vidasalud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.vidasalud.ui.screens.splash.SplashScreen
import com.example.vidasalud.ui.screens.login.LoginScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()  // Lo comento para evitar error
        setContent {
            MiApp()
        }
    }
}

@Composable
fun MiApp() {
    var showLogin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000L)
        showLogin = true
    }

    MaterialTheme {
        Surface {
            if (!showLogin) {
                SplashScreen()
            } else {
                LoginScreen()
            }
        }
    }
}
