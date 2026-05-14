package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.minhasaudefeminina.viewmodel.CalendarDayType
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen(viewModel: SintomasViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val scrollState = rememberScrollState()
    val mesExibido by viewModel.mesExibido.collectAsState()
    val registros by viewModel.registrosSintomas.collectAsState()
    val diasAtraso = viewModel.calcularDiasAtraso()
    val diasProximoCiclo = viewModel.calcularDiasProximoCiclo()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBackground)
            .verticalScroll(scrollState),
    ) {
        // Header com gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(RosaPrimario, RosaSecundario)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Text(
                    text = "♀ Minha Saúde Feminina",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acompanhe seu ciclo com cuidado",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Cards de resumo rápido
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Próxima menstruação",
                    value = when {
                        diasAtraso > 0 -> "Atrasada $diasAtraso d"
                        diasProximoCiclo == 0 -> "Hoje"
                        else -> "Em $diasProximoCiclo dias"
                    },
                    valueColor = if (diasAtraso > 0) Color(0xFFE53935) else RosaPrimario,
                    background = Color.White
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Registros",
                    value = registros.size.toString(),
                    valueColor = RosaSecundario,
                    background = Color.White
                )
            }

            Text(
                text = "📅 Calendário Menstrual",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Calendar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CalendarHeader(
                        mesExibido = mesExibido,
                        onAnterior = { viewModel.mesAnterior() },
                        onProximo = { viewModel.mesProximo() }
                    )
                    CalendarGrid(mesExibido) { data ->
                        viewModel.getTiposParaDia(data)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            CalendarLegend()

            Spacer(modifier = Modifier.height(20.dp))

            // Calculadora de Atraso
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
                                "Calculadora de Atraso",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                when {
                                    diasAtraso > 0 -> "⚠ Atraso de $diasAtraso dias. Procure a UBS."
                                    diasProximoCiclo <= 3 && diasProximoCiclo >= 0 -> "Menstruação prevista em breve."
                                    else -> "Seu ciclo está em dia. ✓"
                                },
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Mais informações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Próxima menstruação",
                    value = if (diasAtraso > 0) "Atrasada" else if (diasProximoCiclo == 0) "Hoje" else "Em $diasProximoCiclo dias",
                    iconId = R.drawable.ic_home
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Registros de Sintomas",
                    value = registros.size.toString(),
                    iconId = R.drawable.ic_favorite
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Aviso médico
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                    Text(
                        text = "Essas informações não substituem avaliação médica. Procure sempre a UBS para confirmação e acompanhamento.",
                        fontSize = 12.sp,
                        color = Color(0xFF795548)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier,
    label: String,
    value: String,
    valueColor: Color,
    background: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun CalendarHeader(mesExibido: YearMonth, onAnterior: () -> Unit, onProximo: () -> Unit) {
    val nomeMes = mesExibido.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAnterior) { Icon(Icons.Default.ChevronLeft, null) }
        Text("$nomeMes ${mesExibido.year}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        IconButton(onClick = onProximo) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
fun CalendarGrid(mesExibido: YearMonth, getTipos: (LocalDate) -> List<CalendarDayType>) {
    val daysOfWeek = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB")
    val firstDayOfMonth = mesExibido.atDay(1)
    val lastDayOfMonth = mesExibido.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
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

        var currentDay = 1 - firstDayOfWeek
        while (currentDay <= lastDayOfMonth.dayOfMonth) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
                for (i in 0..6) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (currentDay in 1..lastDayOfMonth.dayOfMonth) {
                            val data = mesExibido.atDay(currentDay)
                            val tipos = getTipos(data)
                            DayCell(day = currentDay.toString(), tipos = tipos)
                        }
                    }
                    currentDay++
                }
            }
        }
    }
}

@Composable
fun DayCell(day: String, tipos: List<CalendarDayType>) {
    val isHoje = tipos.contains(CalendarDayType.HOJE)
    val isMenstruacao = tipos.contains(CalendarDayType.MENSTRUACAO)
    val isFertil = tipos.contains(CalendarDayType.FERTIL)
    val isOvulacao = tipos.contains(CalendarDayType.OVULACAO)
    val hasSintoma = tipos.contains(CalendarDayType.SINTOMA)
    val isSelected = tipos.contains(CalendarDayType.SELECIONADO)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .background(
                    color = when {
                        isSelected -> PurpleSelected
                        isHoje -> PinkToday
                        isMenstruacao -> RosaPrimario
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day,
                fontSize = 14.sp,
                color = if (isSelected || isHoje || isMenstruacao) Color.White else Color.Black
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.height(6.dp)
        ) {
            if (isFertil) IndicatorDot(YellowFertil)
            if (isOvulacao) IndicatorDot(GreenOvulacao)
            if (hasSintoma) IndicatorDot(RedSintoma)
        }
    }
}

@Composable
fun IndicatorDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .size(4.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun CalendarLegend() {
    val items = listOf(
        Pair("Menstruação", RosaPrimario),
        Pair("Fértil", YellowFertil),
        Pair("Ovulação", GreenOvulacao),
        Pair("Eventos", RosaSecundario),
        Pair("Sintomas", RedSintoma),
        Pair("Hoje", PinkToday),
        Pair("Selecionado", PurpleSelected)
    )

    Column {
        val rows = items.chunked(4)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape))
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
