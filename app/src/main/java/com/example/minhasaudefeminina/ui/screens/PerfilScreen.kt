package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.ui.theme.*
import com.example.minhasaudefeminina.viewmodel.PerfilViewModel
import com.example.minhasaudefeminina.model.FaseVida

@Composable
fun PerfilScreen(viewModel: PerfilViewModel) {
    val scrollState = rememberScrollState()
    val faseVida by viewModel.faseVida.collectAsState()
    val isGestante by viewModel.isGestante.collectAsState()
    val dataPapanicolau by viewModel.dataPapanicolau.collectAsState()
    val dataMamografia by viewModel.dataMamografia.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBackground)
            .verticalScroll(scrollState)
    ) {
        // Header
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 1.dp) {
            Box(modifier = Modifier.padding(15.dp), contentAlignment = Alignment.Center) {
                Text("♀ Minha Saúde Feminina", fontSize = 20.sp, color = RosaPrimario, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text("Meu Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PurpleSelected)

            Spacer(modifier = Modifier.height(20.dp))

            // User Info
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color(0xFFD1C4E9),
                            border = androidx.compose.foundation.BorderStroke(2.dp, PurpleSelected)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("100 x 100", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        FloatingActionButton(
                            onClick = { },
                            modifier = Modifier.size(32.dp),
                            containerColor = PurpleSelected,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) { Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp)) }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Maria Silva", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("maria.silva@email.com", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Fase da Vida Section (Ajuste Visual Fixo)
            SectionCard(title = "Fase da Vida", icon = Icons.AutoMirrored.Filled.ListAlt) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fases = FaseVida.entries
                    val chunkedFases = fases.chunked(2)
                    
                    chunkedFases.forEach { rowFases ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowFases.forEach { fase ->
                                val isSelected = faseVida == fase
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(65.dp), // Um pouco mais alto para evitar quebra feia
                                    onClick = { viewModel.setFaseVida(fase) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) PurpleSelected else Color.LightGray.copy(alpha = 0.5f)
                                    ),
                                    shadowElevation = if (isSelected) 4.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            if (fase == FaseVida.IDADE_REPRODUTIVA) Icons.Default.Favorite else Icons.Default.Spa,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) PurpleSelected else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            fase.label,
                                            fontSize = 10.sp,
                                            color = if (isSelected) PurpleSelected else Color.Gray,
                                            lineHeight = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Exames Preventivos
            SectionCard(title = "Exames Preventivos", icon = Icons.Default.MonitorHeart) {
                ExameField(label = "Papanicolau (25-64 anos)", value = dataPapanicolau, onValueChange = { viewModel.setDataPapanicolau(it) })
                Spacer(modifier = Modifier.height(15.dp))
                ExameField(label = "Mamografia (a partir dos 40 anos)", value = dataMamografia, onValueChange = { viewModel.setDataMamografia(it) })
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Gestação
            SectionCard(title = "Gestação", icon = Icons.Default.ChildCare) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estou gestante", fontWeight = FontWeight.Bold)
                        Text("Ativar jornada da gestante", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(checked = isGestante, onCheckedChange = { viewModel.setGestante(it) }, colors = SwitchDefaults.colors(checkedTrackColor = RosaSecundario))
                }
            }

            // Violentometro
            Spacer(modifier = Modifier.height(15.dp))
            SectionCard(title = "Violentômetro", icon = Icons.Default.Warning) {
                Text("Identifique sinais de violência e busque ajuda.", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { /* Abrir Violentometro UI */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSintoma),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ver Violentômetro", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Privacy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp), tint = RosaSecundario)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Seus dados são armazenados com segurança. Sua privacidade é nossa prioridade (LGPD).",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(Color(0xFFF3E5F5), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(18.dp), tint = PurpleSelected)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(15.dp))
            content()
        }
    }
}

@Composable
fun ExameField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            if (value.isEmpty()) {
                Text("Não informado", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.background(Color(0xFFEEEEEE), RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text("dd/mm/aaaa", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = RosaClaro, focusedBorderColor = RosaSecundario)
        )
    }
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean) {
    var isChecked by remember { mutableStateOf(checked) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(checked = isChecked, onCheckedChange = { isChecked = it }, colors = SwitchDefaults.colors(checkedTrackColor = RosaSecundario))
    }
}
