package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.model.FaseVida
import com.example.minhasaudefeminina.ui.theme.*

@Composable
fun HomeScreen() {
    val userNome = "Ana"
    val faseVida = FaseVida.IDADE_REPRODUTIVA
    val diasParaMenstruacao = 12
    val dicaDia = "Mantenha-se hidratada! Beber água ajuda a reduzir o inchaço e as cólicas durante o período menstrual."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundFeminino)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(30.dp)) {
                Text(
                    text = "Olá, $userNome!",
                    fontSize = 28.sp,
                    color = RosaPrimario,
                    fontWeight = FontWeight.Bold // Placeholder para Leckerli One
                )
                Text(
                    text = "Como está o seu autocuidado hoje?",
                    fontSize = 16.sp,
                    color = RosaSecundario
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            
            // Card Fase de Vida
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(modifier = Modifier.width(8.dp).fillMaxHeight().background(TomAcento))
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Você está na fase: ${faseVida.label}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosaPrimario
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Acompanhe seu ciclo e conheça melhor seu corpo a cada fase.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Card Ciclo
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Resumo do Ciclo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RosaPrimario
                    )
                    Row(
                        modifier = Modifier.padding(vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .border(4.dp, RosaClaro, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$diasParaMenstruacao dias", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RosaPrimario)
                                Text("para iniciar", fontSize = 10.sp, color = RosaSecundario)
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text("Fase Folicular", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RosaSecundario)
                            Text("Ciclo regular (28 dias)", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Card Dica
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = RosaClaro),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dica de Saúde", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RosaPrimario)
                        Text("💡", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = dicaDia,
                        fontSize = 14.sp,
                        color = RosaPrimario,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
