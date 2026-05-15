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
        SintomaUI(SintomaTipo.COLICA, Icons.Default.SentimentDissatisfied, OrangeSintoma),
        SintomaUI(SintomaTipo.CORRIMENTO, Icons.Default.WaterDrop, BlueSintoma),
        SintomaUI(SintomaTipo.SANGRAMENTO, Icons.Default.Adjust, RedSintoma),
        SintomaUI(SintomaTipo.SINTOMA_URINARIO, Icons.Default.Whatshot, GreenSintoma),
        SintomaUI(SintomaTipo.HUMOR_TPM, Icons.Default.Favorite, PurpleSintoma),
        SintomaUI(SintomaTipo.FOGACHOS, Icons.Default.Thermostat, PinkHotSintoma),
        SintomaUI(SintomaTipo.SUOR_NOTURNO, Icons.Default.Nightlight, IndigoSintoma),
        SintomaUI(SintomaTipo.OUTRO, Icons.Default.Description, GreySintoma)
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
                text = "Novo Registro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RosaPrimario,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = RosaClaro,
                    focusedBorderColor = RosaSecundario
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "O que você está sentindo?",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.height(380.dp)) {
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
                            color = if (isSelected) RosaClaro.copy(alpha = 0.3f) else Color.White,
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
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isSelected) item.cor else item.cor.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.tipo.label,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) Color.Black else Color.Gray,
                                    lineHeight = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                            .height(45.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHighlighted) RosaSecundario else Color.White)
                            .border(1.dp, if (isHighlighted) RosaSecundario else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = RosaClaro,
                    focusedBorderColor = RosaSecundario
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            val carregando = salvarState is SalvarState.Carregando
            Button(
                onClick = {
                    sintomaSelecionado?.let { tipo ->
                        viewModel.salvarRegistro(
                            RegistroSintoma(
                                tipo = tipo.name,
                                intensidade = intensidade,
                                notas = notas
                            )
                        )
                    }
                },
                enabled = sintomaSelecionado != null && !carregando,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RosaSecundario,
                    disabledContainerColor = RosaClaro
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (carregando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Salvar Registro", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Essas informações não substituem avaliação médica. Procure sempre a UBS.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

data class SintomaUI(val tipo: SintomaTipo, val icone: ImageVector, val cor: Color)
