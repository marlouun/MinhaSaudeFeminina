package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.ui.theme.BlueSintoma
import com.example.minhasaudefeminina.ui.theme.GreenSintoma
import com.example.minhasaudefeminina.ui.theme.GreySintoma
import com.example.minhasaudefeminina.ui.theme.IndigoSintoma
import com.example.minhasaudefeminina.ui.theme.LightPinkBackground
import com.example.minhasaudefeminina.ui.theme.OrangeSintoma
import com.example.minhasaudefeminina.ui.theme.PinkHotSintoma
import com.example.minhasaudefeminina.ui.theme.PurpleSelected
import com.example.minhasaudefeminina.ui.theme.PurpleSintoma
import com.example.minhasaudefeminina.ui.theme.RedSintoma
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.CalendarDayType
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import com.example.minhasaudefeminina.viewmodel.localDate
import com.example.minhasaudefeminina.viewmodel.localEndDate
import com.example.minhasaudefeminina.viewmodel.ocorreNaData
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: SintomasViewModel,
    onAddRecord: (LocalDate) -> Unit,
    onEditRecord: (RegistroSintoma) -> Unit
) {
    val month by viewModel.mesExibido.collectAsStateWithLifecycle()
    val records by viewModel.registrosSintomas.collectAsStateWithLifecycle()
    val cycleSummary by viewModel.cycleSummary.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(month) {
        if (YearMonth.from(selectedDate) != month) {
            selectedDate = if (month == YearMonth.now()) LocalDate.now() else month.atDay(1)
        }
    }

    val selectedRecords = remember(records, selectedDate) {
        records.filter { it.ocorreNaData(selectedDate) }
    }
    val selectedIsPrediction = cycleSummary.nextExpectedDate == selectedDate &&
        selectedRecords.none { it.tipo == SintomaTipo.MENSTRUACAO }

    Column(modifier = Modifier.fillMaxSize().background(LightPinkBackground)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
            Text(
                text = "♀ Minha Saúde Feminina",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RosaPrimario
            )
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CalendarCard(
                    month = month,
                    selectedDate = selectedDate,
                    records = records,
                    onPrevious = viewModel::mesAnterior,
                    onNext = viewModel::mesProximo,
                    onSelect = { selectedDate = it },
                    typesForDay = viewModel::getTiposParaDia
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Registros do dia", fontWeight = FontWeight.Bold, color = RosaPrimario)
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { onAddRecord(selectedDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = RosaSecundario),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Text("Adicionar", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }

            if (selectedIsPrediction) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RosaClaro.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(13.dp).border(2.dp, PurpleSelected, CircleShape))
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text("Possível próxima menstruação", fontWeight = FontWeight.Bold, color = RosaPrimario)
                                Text(
                                    "Esta data é uma estimativa baseada no seu histórico de registros.",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            if (selectedRecords.isEmpty() && !selectedIsPrediction) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))) {
                        Text(
                            "Nenhum sintoma registrado nesta data.",
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            } else if (selectedRecords.isNotEmpty()) {
                items(selectedRecords.size, key = { selectedRecords[it].id }) { index ->
                    SymptomRecordCard(selectedRecords[index], onEditRecord)
                }
            }

            item {
                CycleEstimateCard(
                    nextDate = cycleSummary.nextExpectedDate,
                    lateDays = cycleSummary.lateDays,
                    estimatedCycleDays = cycleSummary.estimatedCycleDays,
                    basedOnHistory = cycleSummary.basedOnHistory,
                    detectedCycles = cycleSummary.detectedCycles
                )
            }
        }
    }
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    selectedDate: LocalDate,
    records: List<RegistroSintoma>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    typesForDay: (LocalDate) -> List<CalendarDayType>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Mês anterior", tint = RosaPrimario)
                }
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${month.year}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RosaPrimario
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Próximo mês", tint = RosaPrimario)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp)
                }
            }

            val firstDay = month.atDay(1)
            val emptyDays = firstDay.dayOfWeek.value % 7
            val rows = (emptyDays + month.lengthOfMonth() + 6) / 7
            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { column ->
                        val dayNumber = row * 7 + column - emptyDays + 1
                        Box(modifier = Modifier.weight(1f)) {
                            if (dayNumber in 1..month.lengthOfMonth()) {
                                val date = month.atDay(dayNumber)
                                val dayRecords = records.filter { it.ocorreNaData(date) }
                                CalendarDay(
                                    day = dayNumber,
                                    types = typesForDay(date),
                                    selected = date == selectedDate,
                                    records = dayRecords,
                                    onClick = { onSelect(date) }
                                )
                            } else {
                                Spacer(Modifier.aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).background(RosaPrimario, CircleShape))
                Text(" Período registrado", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.size(14.dp))
                Box(Modifier.size(9.dp).border(1.5.dp, PurpleSelected, CircleShape))
                Text(" Previsão", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    types: List<CalendarDayType>,
    selected: Boolean,
    records: List<RegistroSintoma>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                when {
                    selected -> PurpleSelected.copy(alpha = 0.18f)
                    CalendarDayType.MENSTRUACAO in types -> RosaClaro.copy(alpha = 0.55f)
                    CalendarDayType.HOJE in types -> RosaClaro
                    else -> Color.Transparent
                },
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                fontWeight = if (selected || CalendarDayType.HOJE in types || CalendarDayType.MENSTRUACAO in types) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) PurpleSelected else Color.Black
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                if (CalendarDayType.PREVISAO_MENSTRUACAO in types) {
                    Box(Modifier.size(7.dp).border(1.5.dp, PurpleSelected, CircleShape))
                }
                records.take(3).forEach {
                    Box(Modifier.size(4.dp).background(getSintomaColor(it.tipo), CircleShape))
                }
            }
        }
    }
}

@Composable
private fun SymptomRecordCard(record: RegistroSintoma, onEdit: (RegistroSintoma) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(record) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(getSintomaColor(record.tipo), CircleShape))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(record.tipo.label, fontWeight = FontWeight.SemiBold)
                if (record.tipo == SintomaTipo.MENSTRUACAO) {
                    val start = record.localDate()
                    val end = record.localEndDate() ?: start
                    val days = ChronoUnit.DAYS.between(start, end).toInt() + 1
                    Text(
                        "Período: ${start.format(formatter)} a ${end.format(formatter)} ($days dia(s))",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text("Intensidade ${record.intensidade}/5", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text("Intensidade ${record.intensidade}/5", fontSize = 12.sp, color = Color.Gray)
                }
                record.notas?.let { Text(it, fontSize = 13.sp, color = Color.DarkGray, maxLines = 2) }
            }
            Icon(Icons.Default.Edit, contentDescription = "Editar registro", tint = RosaSecundario)
        }
    }
}

@Composable
private fun CycleEstimateCard(
    nextDate: LocalDate?,
    lateDays: Int,
    estimatedCycleDays: Int,
    basedOnHistory: Boolean,
    detectedCycles: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RosaClaro.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = RosaPrimario)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("Estimativa do ciclo", fontWeight = FontWeight.Bold, color = RosaPrimario)
                Text(
                    when {
                        nextDate == null -> "Registre uma menstruação para gerar uma estimativa."
                        lateDays > 0 -> "A previsão era ${nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ($lateDays dia(s) atrás)."
                        else -> "Possível próxima menstruação: ${nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                    },
                    fontSize = 14.sp
                )
                if (nextDate != null) {
                    Text(
                        if (basedOnHistory) {
                            "Média estimada de $estimatedCycleDays dias com base em $detectedCycles ciclos registrados."
                        } else {
                            "Ainda há pouco histórico; por enquanto usamos uma referência de 28 dias."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
                Text(
                    "A previsão é aproximada e pode variar; não é diagnóstico nem método contraceptivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getSintomaColor(tipo: SintomaTipo): Color = when (tipo) {
    SintomaTipo.MENSTRUACAO -> RosaPrimario
    SintomaTipo.COLICA -> OrangeSintoma
    SintomaTipo.CORRIMENTO -> BlueSintoma
    SintomaTipo.SANGRAMENTO -> RedSintoma
    SintomaTipo.SINTOMA_URINARIO -> GreenSintoma
    SintomaTipo.HUMOR_TPM -> PurpleSintoma
    SintomaTipo.FOGACHOS -> PinkHotSintoma
    SintomaTipo.SUOR_NOTURNO -> IndigoSintoma
    SintomaTipo.OUTRO -> GreySintoma
}