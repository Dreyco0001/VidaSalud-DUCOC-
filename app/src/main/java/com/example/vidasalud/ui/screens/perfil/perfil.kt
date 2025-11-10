package com.example.vidasalud.ui.screens.perfil

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.InputStream

@Composable
fun PerfilScreen(
    nombre: String,
    rol: String,
    onLogout: () -> Unit,
    onVerCatalogo: () -> Unit
) {
    val context = LocalContext.current

    // Cargar URI guardada (persistente)
    var fotoUri by remember {
        mutableStateOf(loadProfileImageUri(context))
    }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Si existe una URI guardada, cargar su imagen al entrar
    LaunchedEffect(fotoUri) {
        fotoUri?.let {
            try {
                val stream: InputStream? = context.contentResolver.openInputStream(it)
                bitmap = BitmapFactory.decodeStream(stream)
                stream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let { uri ->
                saveProfileImageUri(context, uri) // Guardar persistente
                val stream: InputStream? = context.contentResolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(stream)
                stream?.close()
            }
        }
    }

    // Launcher galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            fotoUri = selectedUri
            saveProfileImageUri(context, selectedUri) // Guardar persistente
            val stream: InputStream? = context.contentResolver.openInputStream(selectedUri)
            bitmap = BitmapFactory.decodeStream(stream)
            stream?.close()
        }
    }

    // Crear URI para cámara
    fun crearFotoUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "perfil_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Imagen de perfil
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            // Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Bienvenido, $nombre",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Rol: ${rol.uppercase()}")

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(onClick = {
                val uri = crearFotoUri()
                fotoUri = uri
                cameraLauncher.launch(uri)
            }) {
                Text("📸 Cámara")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("🖼️ Galería")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) {
            Text("Ir al catálogo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// Funciones para guardar/cargar la URI de forma persistente
private fun saveProfileImageUri(context: Context, uri: Uri) {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("profile_image_uri", uri.toString()).apply()
}

private fun loadProfileImageUri(context: Context): Uri? {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("profile_image_uri", null)
    return uriString?.let { Uri.parse(it) }
}
