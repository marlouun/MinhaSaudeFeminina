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

    private val _messages = MutableStateFlow(
        listOf(
            MensagemChat(
                texto = "Olá! 👋 Sou sua assistente de saúde feminina. Pode me perguntar sobre ciclo menstrual, sintomas, exames preventivos e muito mais. Suas perguntas são anônimas e seguras.",
                is_usuario = false,
                sessao_id = sessaoId
            )
        )
    )
    val messages: StateFlow<List<MensagemChat>> = _messages

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = MensagemChat(texto = text, is_usuario = true, sessao_id = sessaoId)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _isTyping.value = true
            delay(900)
            _isTyping.value = false
            generateResponse(text)
        }
    }

    private fun removerAcentos(texto: String): String {
        val nfd = Normalizer.normalize(texto, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfd).replaceAll("").lowercase()
    }

    private fun generateResponse(query: String) {
        val q = removerAcentos(query)

        val responseText = when {
            // Saudações
            q.matches(Regex("(oi|ola|boa tarde|bom dia|boa noite|tudo bem|tudo bom).*")) ->
                "Olá! 😊 Estou aqui para te ajudar com dúvidas sobre saúde feminina. O que você gostaria de saber?"

            // Corrimento
            q.contains("normal") || q.contains("corrimento") || q.contains("corimento") ->
                "O muco fisiológico é transparente ou claro, sem odor e não causa coceira. No período fértil, ele fica elástico como 'clara de ovo'.\n\n🔴 Procure a UBS se o corrimento for:\n• Branco e grumoso (candidíase)\n• Amarelo/esverdeado com odor forte\n• Acinzentado com cheiro fétido"

            // Cólica / dor
            q.contains("colica") || q.contains("dor") && (q.contains("barriga") || q.contains("utero") || q.contains("menstrua")) ->
                "Cólicas leves são comuns e podem ser aliviadas com:\n• Compressa morna no abdômen\n• Repouso\n• Analgésicos comuns\n\n⚠️ Procure a UBS se houver febre, dor incapacitante ou sangramento muito intenso. Cólicas que não melhoram com remédio podem indicar endometriose."

            // Gravidez / atraso
            q.contains("atraso") || q.contains("gravid") || q.contains("bebe") || q.contains("gestac") || q.contains("prenatal") ->
                "A menstruação pode variar (ciclos de 21 a 36 dias são normais).\n\n📋 Se o atraso for de 15 dias ou mais, faça um teste de farmácia. Resultado positivo ou atraso persistente: procure a UBS para iniciar o pré-natal o quanto antes."

            // Sintoma urinário
            q.contains("xixi") || q.contains("urina") || q.contains("ardor") || q.contains("ardencia") || q.contains("infeccao urinaria") ->
                "Ardor ou urgência para urinar pode indicar Infecção Urinária (ITU).\n\n⚠️ Procure a UBS se houver:\n• Sangue na urina\n• Febre ou calafrios\n• Dor nas costas\n\n💧 Beber bastante água é a melhor prevenção!"

            // Menopausa / climatério
            q.contains("menopausa") || q.contains("calor") || q.contains("fogacho") || q.contains("climaterio") ->
                "Os fogachos são ondas de calor comuns no climatério (40–65 anos).\n\n✅ Dicas de alívio:\n• Roupas leves e ambientes ventilados\n• Evite álcool e café\n• Hidrate-se bem\n• Exercícios físicos regulares\n\nA menopausa é confirmada após 12 meses seguidos sem menstruar."

            // Papanicolau / preventivo
            q.contains("preventivo") || q.contains("papanicolau") || q.contains("papar") || q.contains("colo") && q.contains("utero") ->
                "O Papanicolau detecta lesões precoces no colo do útero.\n\n📅 Quem deve fazer:\n• Mulheres de 25 a 64 anos que já iniciaram atividade sexual\n• A cada 3 anos após dois exames normais consecutivos\n\n⚠️ Procure a UBS se estiver há mais de 1 ano sem realizar o exame."

            // Mama
            q.contains("mama") || q.contains("caroco") || q.contains("peito") || q.contains("nodulo") || q.contains("mamografia") ->
                "O autoexame ajuda a conhecer seu corpo. Fique atenta a:\n• Caroços fixos e indolores\n• Alterações na pele ou mamilo\n• Secreção espontânea\n\n📋 A mamografia de rotina é recomendada a partir dos 40–50 anos. Procure a UBS para solicitação."

            // Violência
            q.contains("violencia") || q.contains("ajuda") && (q.contains("medo") || q.contains("agress")) || q.contains("abus") ->
                "Você não está sozinha. 💜\n\n📞 Canais de ajuda:\n• Ligue 180 (sigiloso, 24h)\n• UBS mais próxima\n• Delegacia da Mulher\n\nA violência pode ser física, psicológica, sexual ou patrimonial. Todas são crimes."

            // Contracepção
            q.contains("contracep") || q.contains("diu") || q.contains("pilula") || q.contains("injet") || q.contains("anticoncepcional") ->
                "O SUS oferece métodos gratuitos:\n• DIU de cobre (sem hormônios)\n• Pílulas combinadas e minipílula\n• Injetáveis mensais e trimestrais\n• Preservativos\n\n🏥 Procure a UBS para planejamento familiar e escolher o método ideal para você."

            // TPM / humor
            q.contains("tpm") || q.contains("humor") || q.contains("irritada") || q.contains("choro") ->
                "A TPM é causada pela queda hormonal antes da menstruação, afetando a serotonina.\n\n✅ O que pode ajudar:\n• Alimentação equilibrada\n• Reduzir café e açúcar\n• Exercícios físicos\n\n⚠️ Se a TPM estiver afetando muito sua vida, pode ser TDPM. Busque apoio psicológico na UBS."

            // IST / DST
            q.contains("ist") || q.contains("dst") || q.contains("sifilis") || q.contains("hiv") || q.contains("hpv") ->
                "As ISTs (Infecções Sexualmente Transmissíveis) muitas vezes não têm sintomas.\n\n🔬 O SUS oferece testes rápidos gratuitos para HIV, Sífilis e Hepatites na UBS.\n\n✅ O preservativo é o único método que protege contra ISTs. O HPV tem vacina disponível no SUS para meninas de 9 a 14 anos."

            // Endometriose
            q.contains("endometriose") ->
                "A endometriose é uma condição em que o tecido do útero cresce fora dele, causando dores intensas.\n\n⚠️ Sinais de alerta:\n• Cólicas incapacitantes\n• Dor durante a relação sexual\n• Dificuldade para engravidar\n\nProcure a UBS para encaminhamento ao ginecologista."

            // Resposta padrão
            else ->
                "Sua dúvida sobre \"$query\" é importante! 📝\n\nRecomendo:\n1. Anotar seus sintomas no calendário do app\n2. Observar a frequência e intensidade\n3. Levar essas informações à sua próxima consulta na UBS\n\nPosso te ajudar com mais alguma dúvida?"
        }

        _messages.value = _messages.value + MensagemChat(
            texto = responseText,
            is_usuario = false,
            sessao_id = sessaoId
        )
    }
}
