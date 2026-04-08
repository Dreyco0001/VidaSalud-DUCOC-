package com.example.vidasalud.ui.screens.perfil

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.vidasalud.pdf.PdfGenerator
import androidx.core.content.edit
import android.content.Intent
import android.content.ClipData

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    nombre: String,
    rol: String,
    onLogout: () -> Unit,
    onVerCatalogo: () -> Unit,
    onGestionAdmin: () -> Unit = {},
    onGestionUsuarios: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            BarraNavegacionPrincipal(
                navController = navController,
                nombreUsuario = nombre,
                rolUsuario = rol,
                rutaActual = "perfil/{nombre}/{rol}"
            )
        }
    ) { paddingValues ->
        ContenidoPerfil(
            modifier = Modifier.padding(paddingValues),
            nombre = nombre,
            rol = rol,
            onLogout = onLogout,
            onVerCatalogo = onVerCatalogo,
            onGestionAdmin = onGestionAdmin,
            onGestionUsuarios = onGestionUsuarios
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ContenidoPerfil(
    modifier: Modifier = Modifier,
    nombre: String,
    rol: String,
    onLogout: () -> Unit,
    onVerCatalogo: () -> Unit,
    onGestionAdmin: () -> Unit,
    onGestionUsuarios: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fotoUri by remember { mutableStateOf<Uri?>(loadProfileImageUri(context)) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var fotoFirebaseUrl by remember { mutableStateOf<String?>(null) }
    var usarFotoRemota by remember { mutableStateOf(false) }
    var urlManual by remember { mutableStateOf("") }
    var mostrarCampoUrl by remember { mutableStateOf(false) }

    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    val regexImagen = Regex(""".*\.(jpg|jpeg|png|gif|webp)(\?.*)?$""", RegexOption.IGNORE_CASE)

    LaunchedEffect(user?.uid) {
        user?.let {
            firestore.collection("usuario").document(it.uid)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let { doc ->
                        fotoFirebaseUrl = doc.getString("fotoPerfil")
                        usarFotoRemota = doc.getBoolean("usarFotoRemota") ?: false
                    }
                }
        }
    }

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

    fun subirFotoAFirebase(uri: Uri) {
        if (user == null) return
        scope.launch(Dispatchers.IO) {
            try {
                val ref = storage.reference.child("perfiles/${user.uid}_${UUID.randomUUID()}.jpg")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()

                val data = mapOf(
                    "correo" to (user.email ?: ""),
                    "nombre" to nombre,
                    "rol" to rol,
                    "fotoPerfil" to url,
                    "usarFotoRemota" to true
                )

                firestore.collection("usuario").document(user.uid).set(data).await()
                fotoFirebaseUrl = url
                usarFotoRemota = true
            } catch (e: Exception) {
                println("❌ Error al subir foto: ${e.message}")
            }
        }
    }

    fun guardarUrlManual() {
        if (user == null || urlManual.isBlank() || !regexImagen.matches(urlManual)) return
        scope.launch(Dispatchers.IO) {
            try {
                firestore.collection("usuario").document(user.uid)
                    .update(mapOf("fotoPerfil" to urlManual, "usarFotoRemota" to true))
                    .await()
                fotoFirebaseUrl = urlManual
                usarFotoRemota = true
                mostrarCampoUrl = false
                urlManual = ""
            } catch (e: Exception) {
                println("❌ Error guardando URL manual: ${e.message}")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let { uri ->
                saveProfileImageUri(context, uri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
                subirFotoAFirebase(uri)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = it
            saveProfileImageUri(context, it)
            context.contentResolver.openInputStream(it)?.use { stream ->
                bitmap = BitmapFactory.decodeStream(stream)
            }
            subirFotoAFirebase(it)
        }
    }

    fun crearFotoUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "perfil_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    fun solicitarPermisoCamara(onGranted: () -> Unit) {
        val permiso = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED)
            onGranted()
        else
            permissionLauncher.launch(arrayOf(permiso))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun solicitarPermisoGaleria(onGranted: () -> Unit) {
        val permiso = Manifest.permission.READ_MEDIA_IMAGES
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED)
            onGranted()
        else
            permissionLauncher.launch(arrayOf(permiso))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                usarFotoRemota && !fotoFirebaseUrl.isNullOrEmpty() ->
                    Image(
                        painter = rememberAsyncImagePainter(fotoFirebaseUrl),
                        contentDescription = "Foto remota",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )

                bitmap != null ->
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Foto local",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )

                else -> Text("👤", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Bienvenido, $nombre", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Rol: ${rol.uppercase()}")

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                solicitarPermisoCamara {
                    val uri = crearFotoUri()
                    fotoUri = uri
                    cameraLauncher.launch(uri)
                }
            }) { Text("📸 Cámara") }

            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    solicitarPermisoGaleria { galleryLauncher.launch("image/*") }
                } else {
                    galleryLauncher.launch("image/*")
                }
            }) { Text("🖼️ Galería") }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { mostrarCampoUrl = !mostrarCampoUrl },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) { Text(if (mostrarCampoUrl) "Cancelar URL" else "Agregar URL Remota") }

        if (mostrarCampoUrl) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = urlManual,
                    onValueChange = { urlManual = it },
                    label = { Text("URL pública de imagen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = urlManual.isNotEmpty() && !regexImagen.matches(urlManual),
                    placeholder = { Text("https://...") }
                )

                Button(
                    onClick = {
                        if (regexImagen.matches(urlManual)) guardarUrlManual()
                        else Toast.makeText(context, "URL inválida", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) { Text("Guardar URL") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = usarFotoRemota,
                onCheckedChange = {
                    usarFotoRemota = it
                    if (user != null)
                        firestore.collection("usuario").document(user.uid)
                            .update("usarFotoRemota", it)
                }
            )
            Text(if (usarFotoRemota) "Usando foto remota" else "Usando foto local")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val uri = PdfGenerator.generarInforme(context, nombre)

                if (uri != null) {

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        clipData = ClipData.newRawUri("", uri)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "No hay app para abrir PDFs",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Generar y abrir informe")
        }

        if (rol.lowercase() == "admin") {

            Button(
                onClick = onGestionAdmin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) { Text("⚙️ Gestionar aplicación", fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGestionUsuarios,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
            ) { Text("🧑‍💻 Gestionar usuarios", fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) {
            Text("Ir al catálogo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Cerrar sesión") }
    }
}

private fun saveProfileImageUri(context: Context, uri: Uri) {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    prefs.edit {putString("profile_image_uri", uri.toString())}
}

private fun loadProfileImageUri(context: Context): Uri? {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("profile_image_uri", null)
    return uriString?.toUri()
}
