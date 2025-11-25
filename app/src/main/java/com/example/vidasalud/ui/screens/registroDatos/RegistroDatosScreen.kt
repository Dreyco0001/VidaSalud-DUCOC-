package com.example.vidasalud.ui.screens.registroDatos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatosScreen(navController: NavController, userName: String, userRole: String) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraNavegacionPrincipal(
                navController = navController,
                nombreUsuario = userName,
                rolUsuario = userRole,
                rutaActual = "registroDatos/{nombre}/{rol}"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrar",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .offset(y = 4.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp)) // Aumentado el espacio
            // contenido real de aqui pa abajo
            MacroNutrientTracker()
        }
    }
}

@Composable
fun MacroNutrientTracker() {
    var proteinas by remember { mutableStateOf(120) }
    var carbohidratos by remember { mutableStateOf(80) }
    var grasas by remember { mutableStateOf(40) }

    val total = proteinas + carbohidratos + grasas

    val proteinasAngle = if (total > 0) 360f * proteinas / total else 0f
    val carbohidratosAngle = if (total > 0) 360f * carbohidratos / total else 0f
    val grasasAngle = if (total > 0) 360f * grasas / total else 0f

    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFFFFD54F), // Yellow
        Color(0xFF64B5F6)  // Blue
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(102.dp), // Tamaño reducido en un 20% más
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                drawArc(
                    color = colors[0],
                    startAngle = startAngle,
                    sweepAngle = proteinasAngle,
                    useCenter = false,
                    style = Stroke(width = 80f, cap = StrokeCap.Round)
                )
                startAngle += proteinasAngle
                drawArc(
                    color = colors[1],
                    startAngle = startAngle,
                    sweepAngle = carbohidratosAngle,
                    useCenter = false,
                    style = Stroke(width = 80f, cap = StrokeCap.Round)
                )
                startAngle += carbohidratosAngle
                drawArc(
                    color = colors[2],
                    startAngle = startAngle,
                    sweepAngle = grasasAngle,
                    useCenter = false,
                    style = Stroke(width = 80f, cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            MacroRow("Proteínas", proteinas, colors[0], { if (proteinas > 0) proteinas-- }, { proteinas++ })
            Spacer(modifier = Modifier.height(8.dp))
            MacroRow("Carbohidratos", carbohidratos, colors[1], { if (carbohidratos > 0) carbohidratos-- }, { carbohidratos++ })
            Spacer(modifier = Modifier.height(8.dp))
            MacroRow("Grasas", grasas, colors[2], { if (grasas > 0) grasas-- }, { grasas++ })
        }
    }
}

@Composable
fun MacroRow(label: String, value: Int, color: Color, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2
            )
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $value gr",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onDecrement, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Remove, contentDescription = "Restar")
        }
        IconButton(onClick = onIncrement, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Aumentar")
        }
    }
}