package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.R
import com.example.minhasaudefeminina.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBackground)
            .verticalScroll(scrollState)
    ) {
        // Top Logo Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "♀ Minha Saúde Feminina",
                    fontSize = 20.sp,
                    color = RosaPrimario,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📅 Calendario Menstrual",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Calendar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CalendarHeader()
                    CalendarGrid()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            CalendarLegend()

            Spacer(modifier = Modifier.height(20.dp))

            // Delay Calculator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFA500), Color(0xFFFF4500))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🕒", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Calculadora de Atraso Menstrual",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Atraso maior que 15 dias? Saiba quando fazer o teste",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Mais informacoes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Proxima menstruacao",
                    value = "--",
                    iconId = R.drawable.ic_home // Placeholder
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Registros de Sintomas",
                    value = "0",
                    iconId = R.drawable.ic_favorite // Placeholder
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Medical Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                    Text(
                        text = "Essas informacoes nao substituem avaliacao medica. Procure sempre a UBS para confirmacao e acompanhamento.",
                        fontSize = 12.sp,
                        color = Color(0xFF795548)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Extra space for FAB
        }
    }
}

@Composable
fun CalendarHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) { Icon(Icons.Default.ChevronLeft, null) }
        Text("Maio 2026", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        IconButton(onClick = {}) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
fun CalendarGrid() {
    val days = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB")
    
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Mock calendar rows
        val weeks = listOf(
            listOf("", "", "", "", "", "1", "2"),
            listOf("3", "4", "5", "6", "7", "8", "9"),
            listOf("10", "11", "12", "13", "14", "15", "16"),
            listOf("17", "18", "19", "20", "21", "22", "23"),
            listOf("24", "25", "26", "27", "28", "29", "30"),
            listOf("31", "", "", "", "", "", "")
        )
        
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.isNotEmpty()) {
                            val isToday = day == "12"
                            val isSelected = day == "25"
                            
                            Box(
                                modifier = Modifier
                                    .size(35.dp)
                                    .background(
                                        color = when {
                                            isToday -> PinkToday
                                            isSelected -> PurpleSelected
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    fontSize = 14.sp,
                                    color = if (isToday || isSelected) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarLegend() {
    val items = listOf(
        Pair("Menstruacao", RosaPrimario),
        Pair("Fertil", YellowFertil),
        Pair("Ovulacao", GreenOvulacao),
        Pair("Eventos", RosaSecundario),
        Pair("Sintomas", RedSintoma),
        Pair("Hoje", PinkToday),
        Pair("Selecionado", PurpleSelected)
    )
    
    Column {
        val rows = items.chunked(5)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, title: String, value: String, iconId: Int) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(LightPinkBackground, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconId),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = RosaSecundario
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
