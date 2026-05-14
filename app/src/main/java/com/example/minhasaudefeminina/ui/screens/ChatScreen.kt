package com.example.minhasaudefeminina.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.model.MensagemChat
import com.example.minhasaudefeminina.ui.theme.*
import com.example.minhasaudefeminina.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    var pergunta by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val sugestoes = listOf("É normal isso?", "Cólica forte", "Papanicolau", "Corrimento", "Contracepção", "IST/DST", "TPM")
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        val total = messages.size + if (isTyping) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBackground)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Box(modifier = Modifier.padding(15.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "♀ Minha Saúde Feminina",
                    fontSize = 20.sp,
                    color = RosaPrimario,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Suggestions
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            items(sugestoes) { sugestao ->
                Surface(
                    onClick = { viewModel.sendMessage(sugestao) },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RosaClaro)
                ) {
                    Text(
                        text = sugestao,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                        color = RosaSecundario,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Input Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pergunta,
                        onValueChange = { pergunta = it },
                        placeholder = { Text("Digite sua pergunta anônima...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = RosaClaro,
                            focusedBorderColor = RosaSecundario
                        )
                    )
                    
                    FloatingActionButton(
                        onClick = { 
                            if (pergunta.isNotBlank()) {
                                viewModel.sendMessage(pergunta)
                                pergunta = ""
                            }
                        },
                        containerColor = Color(0xFFB39DDB),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", modifier = Modifier.size(20.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Perguntas anônimas e seguras • Não substitui consulta médica",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(message: MensagemChat) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(message.enviado_em.toDate())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.is_usuario) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.is_usuario) 20.dp else 0.dp,
                bottomEnd = if (message.is_usuario) 0.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.is_usuario) RosaSecundario else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = message.texto,
                modifier = Modifier.padding(15.dp),
                color = if (message.is_usuario) Color.White else Color.Black,
                fontSize = 15.sp
            )
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, start = 5.dp, end = 5.dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "dot$index")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(
                                durationMillis = 500,
                                delayMillis = index * 150
                            ),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "alpha$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(RosaSecundario.copy(alpha = alpha), CircleShape)
                    )
                }
            }
        }
    }
}
