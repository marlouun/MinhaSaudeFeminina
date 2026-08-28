package com.example.minhasaudefeminina.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.ui.theme.BackgroundFeminino
import com.example.minhasaudefeminina.ui.theme.BlueSintoma
import com.example.minhasaudefeminina.ui.theme.GreenSintoma
import com.example.minhasaudefeminina.ui.theme.GreySintoma
import com.example.minhasaudefeminina.ui.theme.IndigoSintoma
import com.example.minhasaudefeminina.ui.theme.OrangeSintoma
import com.example.minhasaudefeminina.ui.theme.PinkHotSintoma
import com.example.minhasaudefeminina.ui.theme.PurpleSintoma
import com.example.minhasaudefeminina.ui.theme.RedSintoma
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.SalvarState
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import com.example.minhasaudefeminina.viewmodel.localDate
import com.example.minhasaudefeminina.viewmodel.localEndDate
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RegistrarSintomaScreen(
    viewModel: SintomasViewModel,
    initialDate: LocalDate,
    recordId: String?,
    onVoltar: () -> Unit,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val records by viewModel.registrosSintomas.collectAsStateWithLifecycle()
    val saveState by viewModel.salvarState.collectAsStateWithLifecycle()
    val existing = remember(records, recordId) { records.firstOrNull { it.id == recordId } }
    val snackbar = remember { SnackbarHostState() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    var date by rememberSaveable { mutableStateOf(initialDate) }
    var endDate by rememberSaveable { mutableStateOf(initialDate) }
    var selectedTypeName by rememberSaveable { mutableStateOf<String?>(null) }
    var intensity by rememberSaveable { mutableIntStateOf(3) }
    var notes by rememberSaveable { mutableStateOf("") }
    var initializedRecord by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val selectedType = selectedTypeName?.let { runCatching { SintomaTipo.valueOf(it) }.getOrNull() }
    val isMenstruation = selectedType == SintomaTipo.MENSTRUACAO

    LaunchedEffect(existing?.id) {
        if (existing != null && initializedRecord != existing.id) {
            date = existing.localDate()
            endDate = existing.localEndDate() ?: date
            selectedTypeName = existing.tipo.name
            intensity = existing.intensidade
            notes = existing.notas.orEmpty()
            initializedRecord = existing.id
        }
    }

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SalvarState.Erro -> {
                snackbar.showSnackbar(state.mensagem)
                viewModel.resetSaveState()
            }
            is SalvarState.Sucesso -> {
                snackbar.showSnackbar(state.message, duration = SnackbarDuration.Short)
                viewModel.resetSaveState()
                onFinished()
            }
            else -> Unit
        }
    }

    fun openStartDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newDate = LocalDate.of(year, month + 1, day)
                date = newDate
                if (endDate.isBefore(newDate)) endDate = newDate
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    fun openEndDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, day -> endDate = LocalDate.of(year, month + 1, day) },
            endDate.year,
            endDate.monthValue - 1,
            endDate.dayOfMonth
        ).apply {
            datePicker.minDate = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null) "Novo registro" else "Editar registro") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    if (recordId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Excluir registro", tint = RedSintoma)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = BackgroundFeminino
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isMenstruation) "Data de início da menstruação" else "Data do registro",
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray
            )
            OutlinedTextField(
                value = date.format(formatter),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = ::openStartDatePicker) {
                        Icon(Icons.Default.CalendarMonth, "Escolher data", tint = RosaPrimario)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable(onClick = ::openStartDatePicker),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosaPrimario,
                    unfocusedBorderColor = RosaClaro
                )
            )

            if (isMenstruation) {
                Text(
                    "Data de término da menstruação",
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = endDate.format(formatter),
                    onValueChange = {},
                    readOnly = true,
                    supportingText = { Text("Você pode editar esta data depois, caso a menstruação ainda esteja em andamento.") },
                    trailingIcon = {
                        IconButton(onClick = ::openEndDatePicker) {
                            Icon(Icons.Default.CalendarMonth, "Escolher data de término", tint = RosaPrimario)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable(onClick = ::openEndDatePicker),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosaPrimario,
                        unfocusedBorderColor = RosaClaro
                    )
                )
            }

            Text(
                "O que você está sentindo?",
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp),
                color = Color.Gray
            )

            symptomOptions().chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        SymptomOption(
                            item = item,
                            selected = selectedType == item.type,
                            onClick = {
                                selectedTypeName = item.type.name
                                if (item.type == SintomaTipo.MENSTRUACAO && endDate.isBefore(date)) endDate = date
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            Text(
                "Intensidade: $intensity/5",
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp),
                color = Color.Gray
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(
                                if (value <= intensity) RosaSecundario else Color.White,
                                RoundedCornerShape(11.dp)
                            )
                            .border(
                                1.dp,
                                if (value <= intensity) RosaSecundario else Color.LightGray,
                                RoundedCornerShape(11.dp)
                            )
                            .clickable { intensity = value },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            value.toString(),
                            color = if (value <= intensity) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                "Notas (opcional)",
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 6.dp),
                color = Color.Gray
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it.take(1_000) },
                placeholder = { Text("Descreva como você se sente...") },
                supportingText = { Text("${notes.length}/1000") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosaPrimario,
                    unfocusedBorderColor = RosaClaro
                )
            )

            val loading = saveState is SalvarState.Carregando
            Button(
                onClick = {
                    viewModel.saveRecord(
                        recordId = recordId,
                        date = date,
                        endDate = if (isMenstruation) endDate else null,
                        type = selectedType,
                        intensity = intensity,
                        notes = notes
                    )
                },
                enabled = selectedType != null && !loading,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 10.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosaSecundario)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text(if (recordId == null) "Salvar registro" else "Salvar alterações", fontSize = 17.sp)
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 30.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.65f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFFF9A825), modifier = Modifier.size(18.dp))
                    Text(
                        "O registro ajuda a acompanhar seu padrão, mas não substitui avaliação médica.",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir registro?") },
            text = { Text("Esta ação remove o registro deste aparelho e não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        recordId?.let(viewModel::deleteRecord)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedSintoma)
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

private data class SymptomOptionData(val type: SintomaTipo, val icon: ImageVector, val color: Color)

private fun symptomOptions() = listOf(
    SymptomOptionData(SintomaTipo.MENSTRUACAO, Icons.Default.Bloodtype, RosaPrimario),
    SymptomOptionData(SintomaTipo.COLICA, Icons.Default.SentimentDissatisfied, OrangeSintoma),
    SymptomOptionData(SintomaTipo.CORRIMENTO, Icons.Default.WaterDrop, BlueSintoma),
    SymptomOptionData(SintomaTipo.SANGRAMENTO, Icons.Default.Adjust, RedSintoma),
    SymptomOptionData(SintomaTipo.SINTOMA_URINARIO, Icons.Default.Whatshot, GreenSintoma),
    SymptomOptionData(SintomaTipo.HUMOR_TPM, Icons.Default.Favorite, PurpleSintoma),
    SymptomOptionData(SintomaTipo.FOGACHOS, Icons.Default.Thermostat, PinkHotSintoma),
    SymptomOptionData(SintomaTipo.SUOR_NOTURNO, Icons.Default.Nightlight, IndigoSintoma),
    SymptomOptionData(SintomaTipo.OUTRO, Icons.Default.Description, GreySintoma)
)

@Composable
private fun SymptomOption(
    item: SymptomOptionData,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.height(105.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) RosaClaro.copy(alpha = 0.55f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) RosaSecundario else Color(0xFFE5E5E5))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(28.dp))
            Text(
                item.type.label,
                modifier = Modifier.padding(top = 7.dp),
                fontSize = 10.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
