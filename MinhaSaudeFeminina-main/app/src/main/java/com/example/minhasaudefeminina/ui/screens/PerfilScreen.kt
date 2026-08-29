package com.example.minhasaudefeminina.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.minhasaudefeminina.model.FaseVida
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.model.Usuario
import com.example.minhasaudefeminina.ui.theme.LightPinkBackground
import com.example.minhasaudefeminina.ui.theme.RedSintoma
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.PerfilViewModel
import com.example.minhasaudefeminina.viewmodel.localDate
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    user: Usuario,
    onOpenAccount: () -> Unit,
    onOpenViolentometer: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val records by viewModel.registrosSintomas.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.setPhotoUri(it.toString())
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val currentMonthRecords = remember(records) {
        records.filter { YearMonth.from(it.localDate()) == YearMonth.now() }
    }

    Box(modifier = Modifier.fillMaxSize().background(LightPinkBackground)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
                Text(
                    "♀ Minha Saúde Feminina",
                    modifier = Modifier.padding(16.dp),
                    color = RosaPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(108.dp),
                            shape = CircleShape,
                            color = RosaClaro,
                            border = androidx.compose.foundation.BorderStroke(3.dp, RosaSecundario)
                        ) {
                            if (profile.fotoUri != null) {
                                AsyncImage(
                                    model = profile.fotoUri,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = RosaPrimario, modifier = Modifier.size(56.dp))
                                }
                            }
                        }
                        IconButton(
                            onClick = { photoLauncher.launch(arrayOf("image/*")) },
                            modifier = Modifier.size(38.dp).background(RosaSecundario, CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, "Escolher foto", tint = Color.White, modifier = Modifier.size(19.dp))
                        }
                    }
                    Text(user.nome, modifier = Modifier.padding(top = 10.dp), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(user.email, color = Color.Gray, fontSize = 13.sp)
                }

                Card(
                    onClick = onOpenAccount,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = RosaSecundario)
                        Text("Gerenciar minha conta", modifier = Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                    }
                }

                ProfileSection("Fase da vida", Icons.Default.Favorite) {
                    FaseVida.entries.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { stage ->
                                val selected = stage == profile.faseVida
                                Surface(
                                    onClick = { viewModel.setFaseVida(stage) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) RosaClaro else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) RosaSecundario else Color.LightGray.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            stage.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) RosaPrimario else Color.DarkGray
                                        )
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Estou gestante", modifier = Modifier.weight(1f))
                        Switch(
                            checked = profile.estaGestante,
                            onCheckedChange = viewModel::setGestante,
                            colors = SwitchDefaults.colors(checkedTrackColor = RosaSecundario)
                        )
                    }
                    if (profile.estaGestante) {
                        Text(
                            "Ative esta opção apenas para personalizar o perfil. O app não calcula idade gestacional sem dados clínicos.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                ProfileSection("Exames e acompanhamento", Icons.Default.HealthAndSafety) {
                    ExamDateRow(
                        label = "Último Papanicolau",
                        timestamp = profile.dataPapanicolau,
                        onChoose = {
                            showProfileDatePicker(context, profile.dataPapanicolau, viewModel::setPapanicolauDate)
                        },
                        onClear = { viewModel.setPapanicolauDate(null) }
                    )
                    Spacer(Modifier.height(10.dp))
                    ExamDateRow(
                        label = "Última mamografia",
                        timestamp = profile.dataMamografia,
                        onChoose = {
                            showProfileDatePicker(context, profile.dataMamografia, viewModel::setMamografiaDate)
                        },
                        onClear = { viewModel.setMamografiaDate(null) }
                    )
                }

                ProfileSection("Relatório deste mês", Icons.Default.BarChart) {
                    val counts = currentMonthRecords.groupingBy { it.tipo }.eachCount()
                    if (counts.isEmpty()) {
                        Text("Nenhum sintoma registrado neste mês.", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        counts.entries.sortedByDescending { it.value }.forEach { (type, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(9.dp).background(getSintomaColor(type), CircleShape))
                                Text(type.label, modifier = Modifier.weight(1f).padding(start = 9.dp))
                                Text("$count", color = Color.Gray)
                            }
                        }
                    }
                }

                Button(
                    onClick = onOpenViolentometer,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSintoma),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Icon(Icons.Default.Warning, null)
                    Text("Ver Violentômetro", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = RosaPrimario)
                    Text("Sair da conta", modifier = Modifier.padding(start = 8.dp), color = RosaPrimario, fontWeight = FontWeight.Bold)
                }
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(RosaClaro, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(19.dp), tint = RosaPrimario)
                }
                Text(title, modifier = Modifier.padding(start = 10.dp), color = RosaPrimario, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ExamDateRow(label: String, timestamp: Long?, onChoose: () -> Unit, onClear: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DateRange, null, tint = RosaSecundario)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(
                timestamp?.let(::formatProfileDate) ?: "Não informado",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        TextButton(onClick = onChoose) { Text(if (timestamp == null) "Adicionar" else "Alterar") }
        if (timestamp != null) {
            IconButton(onClick = onClear) { Icon(Icons.Default.ChevronRight, "Limpar data", tint = Color.LightGray) }
        }
    }
}

private fun showProfileDatePicker(
    context: android.content.Context,
    currentTimestamp: Long?,
    onSelected: (Long?) -> Unit
) {
    val current = currentTimestamp?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onSelected(
                LocalDate.of(year, month + 1, day)
                    .atTime(12, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
        },
        current.year,
        current.monthValue - 1,
        current.dayOfMonth
    ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
}

private fun formatProfileDate(timestamp: Long): String = Instant.ofEpochMilli(timestamp)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
