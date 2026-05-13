package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minhasaudefeminina.model.MensagemChat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

class ChatViewModel : ViewModel() {
    private val sessaoId = UUID.randomUUID().toString()

    private val _messages = MutableStateFlow(listOf(
        MensagemChat(
            texto = "Olá! Envie perguntas anônimas ou digite 'É normal isso?' para conversar sobre suas dúvidas",
            is_usuario = false,
            sessao_id = sessaoId
        )
    ))
    val messages: StateFlow<List<MensagemChat>> = _messages

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = MensagemChat(texto = text, is_usuario = true, sessao_id = sessaoId)
        _messages.value = _messages.value + userMsg
        
        viewModelScope.launch {
            delay(800)
            generateResponse(text)
        }
    }

    private fun removerAcentos(texto: String): String {
        val nfdNormalizedString = Normalizer.normalize(texto, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("").lowercase()
    }

    private fun generateResponse(query: String) {
        val q = removerAcentos(query)
        
        val responseText = when {
            q.contains("normal") || q.contains("corrimento") || q.contains("corimento") -> 
                "O muco fisiológico é transparente ou claro, sem odor e não causa coceira. No período fértil, ele se torna elástico como 'clara de ovo'. Se o corrimento for branco/grumoso, amarelo/esverdeado (odor forte) ou acinzentado, procure a UBS imediatamente."
            
            q.contains("colica") || q.contains("dor") || q.contains("barriga") -> 
                "Cólicas leves são comuns na menstruação e podem ser aliviadas com compressas mornas e repouso. Atenção: procure a UBS se houver febre, dor incapacitante, sangramento muito intenso ou se perceber manchas roxas na pele."
            
            q.contains("atraso") || q.contains("gravid") || q.contains("bebe") || q.contains("gestac") -> 
                "A menstruação pode variar (ciclos de 21 a 36 dias). Se o atraso for de 15 dias ou mais, faça um teste de farmácia. Teste positivo ou atraso persistente exigem consulta na UBS para início do pré-natal."
            
            q.contains("xixi") || q.contains("urina") || q.contains("ardor") || q.contains("ardencia") -> 
                "Sentir ardor ou urgência para urinar pode indicar uma infecção urinária (ITU). Se houver sangue na urina, febre ou dor nas costas, busque atendimento na UBS para realizar um exame de urina."
            
            q.contains("menopausa") || q.contains("calor") || q.contains("fogacho") || q.contains("clima") -> 
                "Os fogachos são ondas de calor comuns no climatério (40-65 anos). Use roupas leves, evite gatilhos como álcool/café e hidrate-se bem. A menopausa é confirmada após 12 meses seguidos sem menstruar."
            
            q.contains("preventivo") || q.contains("papanicolau") || q.contains("exame") || q.contains("papar") -> 
                "O Papanicolau deve ser feito por mulheres de 25 a 64 anos. Ele detecta lesões precoces. Procure sua UBS se nunca fez ou se está há mais de um ano sem realizar o exame."
            
            q.contains("mama") || q.contains("caroco") || q.contains("peito") || q.contains("nodulo") -> 
                "O autoexame ajuda a conhecer seu corpo. Procure a UBS se sentir caroços fixos, secreção no mamilo ou alterações na pele da mama. A mamografia de rotina geralmente começa aos 40-50 anos."

            q.contains("violencia") || q.contains("ajuda") || q.contains("medo") || q.contains("abus") -> 
                "Se você se sente ameaçada ou sofre agressões (físicas ou emocionais), ligue para o 180 (sigiloso) ou procure a UBS/Delegacia da Mulher. Você não está sozinha."

            q.contains("contracep") || q.contains("remedio") || q.contains("diu") || q.contains("pilula") || q.contains("injet") ->
                "O SUS oferece diversos métodos: DIU de cobre, pílulas, injetáveis e preservativos. A nova Lei da Laqueadura permite o procedimento a partir dos 21 anos (ou 18 se tiver 2 filhos). Procure a UBS para planejamento familiar."

            else -> "Pesquisei na minha base de conhecimentos: Sua dúvida sobre '$query' é importante. Recomendo anotar seus sintomas no calendário do app e levá-los à sua próxima consulta na UBS para uma avaliação segura."
        }
        
        _messages.value = _messages.value + MensagemChat(texto = responseText, is_usuario = false, sessao_id = sessaoId)
    }
}
