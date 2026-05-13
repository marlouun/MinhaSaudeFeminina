package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.ui.theme.*
import com.example.minhasaudefeminina.viewmodel.SalvarState
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegistrarSintomaScreen(viewModel: SintomasViewModel) {
    val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    var sintomaSelecionado by remember { mutableStateOf<SintomaTipo?>(null) }
    var intensidade by remember { mutableIntStateOf(3) }
    var notas by remember { mutableStateOf("") }

    val alertas by viewModel.alertas.collectAsState()
    val salvarState by viewModel.salvarState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val sintomas = listOf(
        SintomaUI(SintomaTipo.MENSTRUACAO, Icons.Default.Bloodtype, RosaPrimario),
        SintomaUI(SintomaTipo.COLICA, Icons.Default.SentimentDissatisfied, Color(0xFFFFA726)),
        SintomaUI(SintomaTipo.CORRIMENTO, Icons.Default.WaterDrop, Color(0xFF42A5F5)),
        SintomaUI(SintomaTipo.SANGRAMENTO, Icons.Default.Adjust, Color(0xFFEF5350)),
        SintomaUI(SintomaTipo.SINTOMA_URINARIO, Icons.Default.Whatshot, Color(0xFFFF7043)),
        SintomaUI(SintomaTipo.HUMOR_TPM, Icons.Default.Favorite, Color(0xFF7E57C2)),
        SintomaUI(SintomaTipo.FOGACHOS, Icons.Default.Thermostat, Color(0xFFEC407A)),
        SintomaUI(SintomaTipo.SUOR_NOTURNO, Icons.Default.Nightlight, Color(0xFF5C6BC0)),
        SintomaUI(SintomaTipo.OUTRO, Icons.Default.Description, Color(0xFF78909C))
    )

    LaunchedEffect(alertas) {
        if (alertas.isNotEmpty()) {
            snackbarHostState.showSnackbar(alertas.joinToString("\n"))
            viewModel.limparAlertas()
        }
    }

    LaunchedEffect(salvarState) {
        if (salvarState is SalvarState.Sucesso) {
            snackbarHostState.showSnackbar("Registro salvo com sucesso!")
            sintomaSelecionado = null
            intensidade = 3
            notas = ""
            viewModel.resetarSalvarState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
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
                text = "Data",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            OutlinedTextField(
                value = dataFormatada,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                readOnly = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = RosaClaro)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tipo de Sintoma",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            // Symptom Grid
            Box(modifier = Modifier.height(350.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(sintomas) { item ->
                        val isSelected = sintomaSelecionado == item.tipo
                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { sintomaSelecionado = item.tipo },
                            shape = RoundedCornerShape(15.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, if (isSelected) RosaSecundario else Color(0xFFEEEEEE)),
                            shadowElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    item.icone,
                                    null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isSelected) item.cor else item.cor.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.tipo.label,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) Color.Black else Color.Gray,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Intensity
            Text(
                text = "Intensidade: $intensidade/5",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..5).forEach { num ->
                    val isHighlighted = num <= intensidade
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isHighlighted) PinkToday else Color(0xFFF5F5F5))
                            .clickable { intensidade = num },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString(),
                            color = if (isHighlighted) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Notas (opcional)",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                placeholder = { Text("Descreva como se sente...", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = RosaClaro)
            )

            Spacer(modifier = Modifier.height(30.dp))

            val carregando = salvarState is SalvarState.Carregando
            Button(
                onClick = {
                    sintomaSelecionado?.let { tipo ->
                        viewModel.salvarRegistro(
                            RegistroSintoma(
                                usuario_id = "user-id",
                                data = Timestamp.now(),
                                tipo = tipo.name,
                                intensidade = intensidade,
                                notas = notas
                            )
                        )
                    }
                },
                enabled = sintomaSelecionado != null && !carregando,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosaClaro),
                shape = RoundedCornerShape(15.dp)
            ) {
                if (carregando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Salvar Registro", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠", fontSize = 12.sp, color = Color(0xFFFBC02D))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Essas informações não substituem avaliação médica. Procure sempre a UBS.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

data class SintomaUI(val tipo: SintomaTipo, val icone: ImageVector, val cor: Color)
