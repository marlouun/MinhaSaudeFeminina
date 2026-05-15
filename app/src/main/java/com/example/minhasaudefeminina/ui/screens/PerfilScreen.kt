package com.example.minhasaudefeminina.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.minhasaudefeminina.ui.theme.*
import com.example.minhasaudefeminina.viewmodel.PerfilViewModel
import com.example.minhasaudefeminina.model.FaseVida

@Composable
fun PerfilScreen(viewModel: PerfilViewModel) {
    val authViewModel: com.example.minhasaudefeminina.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val scrollState = rememberScrollState()
    val faseVida by viewModel.faseVida.collectAsState()
    val isGestante by viewModel.isGestante.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    
    var showViolentometro by remember { mutableStateOf(false) }
    var showMinhaConta by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setPhotoUri(it) }
    }

    if (showViolentometro) {
        ViolentometroScreen(onVoltar = { showViolentometro = false })
    } else if (showMinhaConta) {
        MinhaContaScreen(viewModel = authViewModel, onVoltar = { showMinhaConta = false })
    } else {
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
                // User Info
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(110.dp),
                                shape = CircleShape,
                                color = RosaClaro,
                                border = androidx.compose.foundation.BorderStroke(3.dp, RosaSecundario)
                            ) {
                                if (photoUri != null) {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Foto de Perfil",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = RosaPrimario)
                                    }
                                }
                            }
                            FloatingActionButton(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.size(36.dp),
                                containerColor = RosaSecundario,
                                contentColor = Color.White,
                                shape = CircleShape
                            ) { Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp)) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sua Conta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- BOTÃO MINHA CONTA ---
                Card(
                    onClick = { showMinhaConta = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = RosaSecundario)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gerenciar Minha Conta", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- GESTANTE TRACKER (Se ativo) ---
                if (isGestante) {
                    SectionCard(title = "Trilha da Gestante", icon = Icons.Default.ChildCare) {
                        Column {
                            Text("Você está na 24ª semana", fontWeight = FontWeight.Bold, color = RosaPrimario)
                            LinearProgressIndicator(
                                progress = { 0.6f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = RosaSecundario,
                                trackColor = RosaClaro
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Próxima consulta: 15/06 na UBS", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fase da Vida Section
                SectionCard(title = "Fase da Vida", icon = Icons.AutoMirrored.Filled.ListAlt) {
                    val fases = FaseVida.entries.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        fases.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { item ->
                                    val isSelected = faseVida == item
                                    val icon = when(item) {
                                        FaseVida.ADOLESCENCIA -> Icons.Default.School
                                        FaseVida.IDADE_REPRODUTIVA -> Icons.Default.Favorite
                                        FaseVida.GESTACAO -> Icons.Default.ChildCare
                                        FaseVida.CLIMATERIO -> Icons.Default.WbSunny
                                        FaseVida.MENOPAUSA -> Icons.Default.SelfImprovement
                                        FaseVida.SENESCENCIA -> Icons.Default.Spa
                                    }
                                    FaseItem(
                                        label = item.label,
                                        icon = icon,
                                        isSelected = isSelected,
                                        onClick = { viewModel.setFaseVida(item) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Relatório de Sintomas (DINÂMICO)
                val registros by viewModel.registrosSintomas.collectAsState()
                
                SectionCard(title = "Relatório do Mês", icon = Icons.Default.BarChart) {
                    val contagemSintomas = registros.groupBy { it.tipo }
                        .mapValues { it.value.size }
                    
                    if (contagemSintomas.isEmpty()) {
                        Text("Nenhum sintoma registrado neste mês.", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            contagemSintomas.forEach { (tipo, qtd) ->
                                val enumTipo = try { com.example.minhasaudefeminina.model.SintomaTipo.valueOf(tipo) } catch(e: Exception) { null }
                                ReportItem(
                                    label = enumTipo?.label ?: tipo,
                                    count = qtd,
                                    color = getSintomaColor(tipo)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings
                SectionCard(title = "Configurações", icon = Icons.Default.Settings) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Estou gestante", modifier = Modifier.weight(1f))
                        Switch(checked = isGestante, onCheckedChange = { viewModel.setGestante(it) }, colors = SwitchDefaults.colors(checkedTrackColor = RosaSecundario))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showViolentometro = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSintoma),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Warning, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Violentômetro", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout (Padronizado)
                TextButton(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = RosaPrimario)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sair da Conta", color = RosaPrimario, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(RosaClaro, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(18.dp), tint = RosaPrimario)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RosaPrimario)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun FaseItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) RosaClaro else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) RosaSecundario else Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = if (isSelected) RosaPrimario else Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) RosaPrimario else Color.Black)
        }
    }
}

@Composable
fun ReportItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text("$count registros", fontSize = 12.sp, color = Color.Gray)
    }
}
