package com.example.minhasaudefeminina.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.ui.theme.RosaPrimario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolentometroScreen(onVoltar: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Violentômetro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "A violência escala. Identifique os sinais e proteja-se.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Nivel 1: Alerta (Amarelo)
            item {
                ViolenciaLevelCard(
                    title = "TENHA CUIDADO - Amarelo",
                    color = Color(0xFFFFD54F),
                    items = listOf(
                        "Bromas ferinas", "Chantagem", "Mentir/Enganar", 
                        "Ignorar/Lei do Gelo", "Ciúmes excessivos", "Culpar", 
                        "Desqualificar", "Ridicularizar/Ofender", "Intimidar/Ameaçar"
                    )
                )
            }

            // Nivel 2: Reaja (Laranja)
            item {
                ViolenciaLevelCard(
                    title = "REAJA - Laranja",
                    color = Color(0xFFFF8A65),
                    items = listOf(
                        "Humilhar em público", "Controlar/Proibir",
                        "Destruir bens pessoais", "Tocar sem consentimento",
                        "Beliscar/Arranhar", "Empurrar/Sacudir", "Tapa"
                    )
                )
            }

            // Nivel 3: Perigo (Vermelho)
            item {
                ViolenciaLevelCard(
                    title = "PERIGO - Vermelho",
                    color = Color(0xFFE57373),
                    items = listOf(
                        "Chutar", "Isolar/Cárcere", "Ameaçar com armas", 
                        "Ameaçar de morte", "Forçar relação sexual", 
                        "Abuso sexual", "Mutilar", "Feminicídio"
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:180")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosaPrimario),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(Icons.Default.Phone, null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("LIGAR PARA O 180", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "A ligação é gratuita e anônima.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            item { Spacer(modifier = Modifier.height(50.dp)) }
        }
    }
}

@Composable
fun ViolenciaLevelCard(title: String, color: Color, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, color),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = color.darkenSimple(), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(item, fontSize = 14.sp, color = Color.Black)
                }
            }
        }
    }
}

fun Color.darkenSimple(): Color {
    return Color(
        red = this.red * 0.7f,
        green = this.green * 0.7f,
        blue = this.blue * 0.7f,
        alpha = 1f
    )
}
