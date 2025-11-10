package com.example.vidasalud.ui.screens.perfil

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

@Composable
fun PerfilScreen(
    nombre: String,
    rol: String,
    onLogout: () -> Unit,
    onVerCatalogo: () -> Unit
) {
    val context = LocalContext.current

    var fotoUri by remember { mutableStateOf(loadProfileImageUri(context)) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Carga persistente
    LaunchedEffect(fotoUri) {
        fotoUri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Permiso launcher general
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // Solo mostramos si se otorga o no
        if (result.values.all { it }) {
            println("✅ Permisos otorgados")
        } else {
            println("❌ Permisos denegados")
        }
    }

    // 📸 Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let { uri ->
                saveProfileImageUri(context, uri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
            }
        }
    }

    // 🖼️ Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = it
            saveProfileImageUri(context, it)
            context.contentResolver.openInputStream(it)?.use { stream ->
                bitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

    // 👉 Crea URI temporal
    fun crearFotoUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "perfil_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!
    }

    // 👉 Pedir permisos cámara
    fun solicitarPermisoCamara(onGranted: () -> Unit) {
        val permiso = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            permissionLauncher.launch(arrayOf(permiso))
        }
    }

    // 👉 Pedir permisos galería
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun solicitarPermisoGaleria(onGranted: () -> Unit) {
        val permiso = Manifest.permission.READ_MEDIA_IMAGES
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            permissionLauncher.launch(arrayOf(permiso))
        }
    }

    // 🧩 UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Imagen o placeholder
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
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
        Text("Bienvenido, $nombre", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Rol: ${rol.uppercase()}")
        Spacer(modifier = Modifier.height(24.dp))

        Row {
            // 📸 Cámara
            Button(onClick = {
                solicitarPermisoCamara {
                    val uri = crearFotoUri()
                    fotoUri = uri
                    cameraLauncher.launch(uri)
                }
            }) {
                Text("📸 Cámara")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 🖼️ Galería
            Button(onClick = {
                solicitarPermisoGaleria {
                    galleryLauncher.launch("image/*")
                }
            }) {
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

// Guardar URI
@SuppressLint("UseKtx")
private fun saveProfileImageUri(context: Context, uri: Uri) {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("profile_image_uri", uri.toString()).apply()
}

// Cargar URI
private fun loadProfileImageUri(context: Context): Uri? {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("profile_image_uri", null)
    return uriString?.toUri()
}
