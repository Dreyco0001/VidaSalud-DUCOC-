
package com.example.vidasalud.ui.screens.registroDatos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vidasalud.ui.screens.compartido.BarraNavegacionPrincipal
import com.example.vidasalud.viewmodel.RegistroDatosViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatosScreen(
    navController: NavController,
    userName: String,
    userRole: String,
    registroDatosViewModel: RegistroDatosViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observar el estado de guardado del ViewModel
    val saveStatus by registroDatosViewModel.saveStatus
    LaunchedEffect(saveStatus) {
        saveStatus?.let {
            snackbarHostState.showSnackbar(it)
            registroDatosViewModel.resetSaveStatus() // Limpiar el estado después de mostrar el mensaje
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            }
            Spacer(modifier = Modifier.height(32.dp))
            SleepDataEntry()
            Spacer(modifier = Modifier.height(32.dp))
            MacroNutrientTracker()
            Spacer(modifier = Modifier.height(16.dp))
            PlanesEjercicioButton(navController, userName, userRole)
        }
    }
}

@Composable
fun PlanesEjercicioButton(navController: NavController, nombre: String, rol: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("catalogo/${nombre}/${rol}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Planes de ejercicio",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Ver planes de ejercicio",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Ver planes",
                tint = Color.Black
            )
        }
    }
}


@Composable
fun SleepDataEntry(
    viewModel: RegistroDatosViewModel = viewModel()
) {
    var sleepHours by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val currentDay = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    var selectedDay by remember { mutableStateOf(currentDay) }

    Column {
        Text(
            "Ingrese sueño:",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = sleepHours,
            onValueChange = { sleepHours = it },
            label = { Text("Horas de sueño (ej: 6.5)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box {
            OutlinedTextField(
                value = selectedDay,
                onValueChange = { },
                readOnly = true,
                label = { Text("Día de la semana") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        Modifier.clickable { expanded = !expanded }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day) },
                        onClick = {
                            selectedDay = day
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val hours = sleepHours.toFloatOrNull()
                if (hours != null) {
                    val dayAbbreviation = when (selectedDay) {
                        "Lunes" -> "L"
                        "Martes" -> "M"
                        "Miércoles" -> "X"
                        "Jueves" -> "J"
                        "Viernes" -> "V"
                        "Sábado" -> "S"
                        "Domingo" -> "D"
                        else -> ""
                    }
                    if (dayAbbreviation.isNotEmpty()) {
                        viewModel.saveSleepData(dayAbbreviation, hours)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
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
            modifier = Modifier.size(102.dp),
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
