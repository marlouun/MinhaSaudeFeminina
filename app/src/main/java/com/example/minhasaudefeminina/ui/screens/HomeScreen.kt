package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.ui.theme.*
import com.example.minhasaudefeminina.viewmodel.CalendarDayType
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen(viewModel: SintomasViewModel) {
    val mesExibido by viewModel.mesExibido.collectAsState()
    val registros by viewModel.registrosSintomas.collectAsState()
    var diaSelecionado by remember { mutableStateOf(LocalDate.now()) }
    var dragAmountTotal by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBackground)
    ) {
        // --- HEADER ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(15.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "♀ Minha Saúde Feminina",
                    fontSize = 20.sp,
                    color = RosaPrimario,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragAmountTotal > 50) viewModel.mesAnterior()
                            else if (dragAmountTotal < -50) viewModel.mesProximo()
                            dragAmountTotal = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAmountTotal += dragAmount.x
                        }
                    )
                }
        ) {
            // --- CALENDAR HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.mesAnterior() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Anterior", tint = RosaPrimario)
                }
                Text(
                    text = "${mesExibido.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${mesExibido.year}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RosaPrimario
                )
                IconButton(onClick = { viewModel.mesProximo() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Próximo", tint = RosaPrimario)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- CALENDAR GRID ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Days of week
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { day ->
                            Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    val firstDay = mesExibido.atDay(1)
                    val daysInMonth = mesExibido.lengthOfMonth()
                    val emptyDays = firstDay.dayOfWeek.value % 7

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(emptyDays) { Spacer(Modifier.size(40.dp)) }
                        items((1..daysInMonth).toList()) { day ->
                            val date = mesExibido.atDay(day)
                            val types = viewModel.getTiposParaDia(date)
                            val isSelected = diaSelecionado == date
                            
                            val symptomsForThisDay = registros.filter {
                                try {
                                    Instant.ofEpochMilli(it.data_timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
                                } catch (e: Exception) { false }
                            }

                            DayItem(
                                day = day,
                                types = types,
                                isSelected = isSelected,
                                symptoms = symptomsForThisDay,
                                onClick = { diaSelecionado = date }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- INFO SECTION (SINTOMAS DO DIA SELECIONADO) ---
            Text("Sintomas registrados em ${diaSelecionado.dayOfMonth}/${diaSelecionado.monthValue}", fontWeight = FontWeight.Bold, color = RosaPrimario)
            
            val sintomasHoje = registros.filter { 
                try {
                    Instant.ofEpochMilli(it.data_timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == diaSelecionado
                } catch (e: Exception) { false }
            }

            if (sintomasHoje.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                ) {
                    Text("Nenhum registro para este dia.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                sintomasHoje.forEach { registro ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(getSintomaColor(registro.tipo), CircleShape))
                            Spacer(modifier = Modifier.width(10.dp))
                            val enumTipo = try { SintomaTipo.valueOf(registro.tipo) } catch(e: Exception) { null }
                            Text(enumTipo?.label ?: registro.tipo, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Nível ${registro.intensidade}/5", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- CALC CARD ---
            Spacer(modifier = Modifier.height(10.dp))
            InfoCard(
                title = "Próxima Menstruação",
                value = "Em breve",
                icon = Icons.Default.CalendarToday,
                color = RosaClaro
            )
        }
    }
}

@Composable
fun DayItem(
    day: Int, 
    types: List<CalendarDayType>, 
    isSelected: Boolean, 
    symptoms: List<com.example.minhasaudefeminina.model.RegistroSintoma>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> PurpleSelected.copy(alpha = 0.2f)
                    types.contains(CalendarDayType.HOJE) -> RosaClaro
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                color = if (isSelected) PurpleSelected else Color.Black,
                fontWeight = if (isSelected || types.contains(CalendarDayType.HOJE)) FontWeight.Bold else FontWeight.Normal
            )
            // Color dots for each symptom
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                symptoms.take(3).forEach { symptom ->
                    Box(modifier = Modifier.size(4.dp).background(getSintomaColor(symptom.tipo), CircleShape))
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = RosaPrimario)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

fun getSintomaColor(tipo: String): Color {
    return when(tipo) {
        SintomaTipo.MENSTRUACAO.name -> RosaPrimario
        SintomaTipo.COLICA.name -> OrangeSintoma
        SintomaTipo.CORRIMENTO.name -> BlueSintoma
        SintomaTipo.SANGRAMENTO.name -> RedSintoma
        SintomaTipo.SINTOMA_URINARIO.name -> GreenSintoma
        SintomaTipo.HUMOR_TPM.name -> PurpleSintoma
        SintomaTipo.FOGACHOS.name -> PinkHotSintoma
        SintomaTipo.SUOR_NOTURNO.name -> IndigoSintoma
        else -> GreySintoma
    }
}
