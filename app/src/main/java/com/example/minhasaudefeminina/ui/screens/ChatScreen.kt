package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minhasaudefeminina.model.MensagemChat
import com.example.minhasaudefeminina.ui.theme.LightPinkBackground
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isTyping.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var text by rememberSaveable { mutableStateOf("") }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    val suggestions = listOf(
        "É normal ter corrimento?",
        "O que fazer com cólica?",
        "Minha menstruação atrasou",
        "Percebi um caroço na mama",
        "Quais métodos contraceptivos existem?",
        "Sinto ardor ao urinar"
    )

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBackground)
            .imePadding()
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Dúvidas e orientações", fontSize = 18.sp, color = RosaPrimario, fontWeight = FontWeight.Bold)
                    Text("Informação geral, não diagnóstico", fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, "Limpar conversa", tint = RosaSecundario)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = MensagemChat::id) { message -> ChatBubble(message) }
            if (isTyping) {
                item {
                    Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = RosaSecundario)
                            Text("Preparando orientação...", modifier = Modifier.padding(start = 8.dp), fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    onClick = { viewModel.sendMessage(suggestion) },
                    label = { Text(suggestion, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = RosaClaro.copy(alpha = 0.35f),
                        labelColor = RosaPrimario
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(500) },
                    placeholder = { Text("Escreva sua dúvida...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    supportingText = { if (text.length >= 450) Text("${text.length}/500") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosaSecundario,
                        unfocusedBorderColor = RosaClaro
                    )
                )
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(text)
                            text = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = RosaSecundario,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Enviar")
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar conversa?") },
            text = { Text("As mensagens salvas neste aparelho serão removidas.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    viewModel.clearHistory()
                }) { Text("Limpar", color = RosaPrimario) }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ChatBubble(message: MensagemChat) {
    val userMessage = message.isUsuario
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (userMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (userMessage) Alignment.End else Alignment.Start) {
            Surface(
                color = if (userMessage) RosaSecundario else Color.White,
                shape = if (userMessage) RoundedCornerShape(16.dp, 16.dp, 3.dp, 16.dp)
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 3.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Text(
                    message.texto,
                    modifier = Modifier.padding(12.dp),
                    color = if (userMessage) Color.White else Color.Black,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.enviadoEm)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}
