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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.ui.theme.BackgroundFeminino
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegistrarSintomaScreen(viewModel: SintomasViewModel) {
    var dataSelecionada by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var sintomaSelecionado by remember { mutableStateOf<SintomaTipo?>(null) }
    var intensidade by remember { mutableIntStateOf(3) }
    var notas by remember { mutableStateOf("") }
    
    val alertas by viewModel.alertas.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(alertas) {
        if (alertas.isNotEmpty()) {
            snackbarHostState.showSnackbar(alertas.joinToString("\n"))
            viewModel.limparAlertas()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundFeminino,
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
                text = "Registrar Sintoma",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RosaPrimario,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Campo Data
            OutlinedTextField(
                value = dataSelecionada,
                onValueChange = { },
                label = { Text("Data do Registro") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosaPrimario,
                    unfocusedBorderColor = RosaClaro
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Como você está se sentindo?",
                fontSize = 16.sp,
                color = RosaSecundario,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            // Grade de Sintomas
            Box(modifier = Modifier.height(250.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(SintomaTipo.entries) { sintoma ->
                        val isSelected = sintomaSelecionado == sintoma
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sintomaSelecionado = sintoma },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RosaSecundario else Color.White,
                            border = BorderStroke(1.dp, RosaClaro)
                        ) {
                            Text(
                                text = sintoma.label,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Intensidade
            Text(
                text = "Intensidade: $intensidade",
                fontSize = 16.sp,
                color = RosaSecundario,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..5).forEach { num ->
                    val isSelected = intensidade == num
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) RosaPrimario else Color.White)
                            .border(1.dp, RosaClaro, CircleShape)
                            .clickable { intensidade = num },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString(),
                            color = if (isSelected) Color.White else RosaPrimario,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notas
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas Opcionais") },
                placeholder = { Text("Descreva como se sente...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosaPrimario,
                    unfocusedBorderColor = RosaClaro
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Alerta Médico Obrigatório
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F4)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, RosaPrimario)
            ) {
                Row(modifier = Modifier.padding(15.dp)) {
                    Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(RosaPrimario))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Essas informações não substituem avaliação médica. Procure sempre a UBS.",
                        color = RosaPrimario,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Botão Salvar
            Button(
                onClick = {
                    sintomaSelecionado?.let {
                        viewModel.salvarRegistro(
                            RegistroSintoma(
                                usuarioId = "user-id",
                                tipo = it,
                                intensidade = intensidade,
                                notas = notas
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosaPrimario),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Salvar Registro", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
