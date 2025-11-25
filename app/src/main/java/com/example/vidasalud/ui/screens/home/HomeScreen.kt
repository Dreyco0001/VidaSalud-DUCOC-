package com.example.vidasalud.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate

data class TipSalud(
    val message: String,
    val color: Color
)

val TipsSalud = listOf(
    TipSalud("Evita tomar cafeína después de las 7 pm", Color(0xFFFFBACD)),
    TipSalud("Has quemado más calorías esta semana versus la semana anterior, ¡Sigue así!", Color(
        0xFFFFF9C4
    )
    ),
    TipSalud("Prueba irte a dormir más temprano para cumplir tu objetivo de sueño.", Color(
        0xFFC7EAE7
    )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userName: String, userRole: String) {
    var rutaActual by remember { mutableStateOf("home/{nombre}/{rol}") }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.White,
        topBar = {
            HomeTopBar(
                userName = userName,
                userRole = userRole,
                navController = navController,
                onProfileClick = {
                    rutaActual = "perfil/{nombre}/{rol}"
                    navController.navigate("perfil/$userName/$userRole")
                }
            )
        },
        bottomBar = {
            BarraNavegacionPrincipal(
                navController = navController,
                nombreUsuario = userName,
                rolUsuario = userRole,
                rutaActual = rutaActual
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HealthCarousel()
            Spacer(modifier = Modifier.height(24.dp))
            SleepChartCard()
            Spacer(modifier = Modifier.height(16.dp))
            StepsCard()
            Spacer(modifier = Modifier.height(16.dp))
            WeightCard()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HomeTopBar(
    userName: String,
    userRole: String,
    navController: NavController,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = "¡Bienvenido/a, $userName!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumen",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage()
            }
        }
    }
}

@Composable
fun ProfileImage() {
    val user = FirebaseAuth.getInstance().currentUser
    var photoUrl by remember { mutableStateOf<String?>(null) }

    if (user != null) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("usuario").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    photoUrl = document.getString("fotoPerfil")
                }
            }
    }

    if (photoUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(photoUrl),
            contentDescription = "Foto de perfil",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Perfil",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthCarousel() {
    val pageCount = Int.MAX_VALUE
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = Modifier.height(140.dp)
        ) { page ->
            val actualIndex = (page - initialPage).mod(TipsSalud.size)
            
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TipsSalud[actualIndex].color)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = TipsSalud[actualIndex].message,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(TipsSalud.size) { iteration ->
                val actualCurrentPage = (pagerState.currentPage - initialPage).mod(TipsSalud.size)
                
                val color = if (actualCurrentPage == iteration) Color.DarkGray else Color.LightGray
                val size = if (actualCurrentPage == iteration) 10.dp else 8.dp
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(size)
                )
            }
        }
    }
}


@Composable
fun SleepChartCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Sueño (últimos 7 días)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            SleepChart()
        }
    }
}

@Composable
fun SleepChart() {
    val sleepData = listOf(3f, 5f, 6f, 4f, 7f, 12f, 8f)
    val today = LocalDate.now()
    val daysOfWeek = (0..6).map { today.minusDays(it.toLong()).dayOfWeek }
        .reversed()

    val dayLabels = daysOfWeek.map {
        when (it) {
            DayOfWeek.MONDAY -> "L"
            DayOfWeek.TUESDAY -> "M"
            DayOfWeek.WEDNESDAY -> "X"
            DayOfWeek.THURSDAY -> "J"
            DayOfWeek.FRIDAY -> "V"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "D"
        }
    }

    Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxHours = sleepData.maxOrNull() ?: 1f
            val spacing = size.width / (sleepData.size - 1)
            val path = androidx.compose.ui.graphics.Path()

            sleepData.forEachIndexed { index, hours ->
                val x = index * spacing
                val y = size.height * (1 - (hours / maxHours) * 0.9f) - (size.height * 0.05f)
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFFB39DDB),
                style = Stroke(width = 8f, pathEffect = PathEffect.cornerPathEffect(16f))
            )
            sleepData.forEachIndexed { index, hours ->
                val x = index * spacing
                val y = size.height * (1 - (hours / maxHours) * 0.9f) - (size.height * 0.05f)
                drawCircle(
                    color = Color(0xFF7E57C2),
                    radius = 8f,
                    center = Offset(x, y)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        dayLabels.forEach { label ->
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun StepsCard() {
    InfoCard(
        icon = Icons.Default.DirectionsWalk,
        title = "Pasos",
        value = "8,450"
    )
}

@Composable
fun WeightCard() {
    InfoCard(
        icon = Icons.Default.Person,
        title = "Peso",
        value = "60 kg"
    )
}

@Composable
fun InfoCard(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.DarkGray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = Color.DarkGray, fontWeight = FontWeight.Medium)
            }
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black.copy(0.8f))
        }
    }
}
