package com.example.minhasaudefeminina.domain.chat

import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Respostas educativas que continuam disponíveis sem internet.
 * Retorna null quando a pergunta deve ser encaminhada à IA (com consentimento).
 */
object PreparedChatResponses {
    fun findAnswer(question: String): String? {
        val q = normalize(question.trim())
        if (q.isBlank()) return null

        return when {
            q.contains("suicid") || q.contains("quero morrer") ||
                q.contains("me matar") || q.contains("tirar minha vida") -> 
                "Sinto muito que você esteja passando por isso. Se houver perigo imediato, ligue 192 (SAMU) ou vá a uma emergência. Para conversar gratuitamente, ligue 188 (CVV). Procure também alguém de confiança que possa ficar com você."

            Regex("\\b(hemorragia|sangramento intenso|sangramento muito forte|desmaio|desmaiei|convulsao|falta de ar|dor no peito)\\b")
                .containsMatchIn(q) ||
                (q.contains("gravid") && q.contains("sangramento")) ->
                "Esses sintomas podem exigir atendimento urgente. Procure uma emergência ou ligue 192 (SAMU), especialmente se houver sangramento importante, desmaio, falta de ar ou dor intensa. O chat não consegue avaliar a gravidade à distância."

            Regex("^(oi|ola|bom dia|boa tarde|boa noite)([!,.?\\s]|$)").containsMatchIn(q) ->
                "Olá! Posso explicar informações gerais e ajudar você a organizar sintomas. Em caso de urgência, procure atendimento imediatamente."

            q.contains("violencia") || q.contains("agress") || q.contains("abuso") ->
                "Você merece estar em segurança. Ligue 180 para orientação sobre violência contra a mulher. Se houver risco imediato, ligue 190 ou procure um local seguro."

            q.contains("corrimento") ->
                "Mudanças de cor, cheiro, quantidade, coceira ou dor merecem avaliação na UBS. Evite automedicação e registre quando os sintomas começaram."

            q.contains("colica") || (q.contains("dor") && q.contains("menstr")) ->
                "Calor local e movimento leve podem ajudar em cólicas leves. Dor incapacitante, febre, desmaio ou piora rápida são sinais para procurar atendimento."

            q.contains("atraso") || q.contains("gravid") ->
                "Atrasos podem ter várias causas. Se houver possibilidade de gravidez, faça um teste conforme a orientação do produto e procure a UBS se o resultado for positivo ou a dúvida persistir."

            q.contains("urina") || q.contains("xixi") || q.contains("ardor") ->
                "Ardor, urgência ou dor para urinar podem indicar um problema urinário. Febre, sangue na urina ou dor nas costas exigem avaliação rápida."

            q.contains("anticoncepc") || q.contains("diu") || q.contains("pilula") ->
                "A escolha do método depende do histórico e das necessidades de cada pessoa. A UBS oferece orientação de planejamento reprodutivo; não inicie ou interrompa hormônios apenas com base no chat."

            q.contains("mama") || q.contains("caroco") || q.contains("nodulo") ->
                "Uma alteração nova na mama, secreção espontânea, retração da pele ou nódulo persistente deve ser avaliado por um profissional."

            else -> null
        }
    }

    const val OFFLINE_FALLBACK =
        "Não foi possível consultar a IA agora. Anote quando sua dúvida ou sintoma começou, a frequência, a intensidade e os sinais associados. Se houver piora ou preocupação, procure uma UBS. Em caso de urgência, ligue 192."

    const val DECLINED_FALLBACK =
        "Tudo bem! Sem enviar sua dúvida à IA, posso oferecer apenas orientações gerais. Anote os sintomas e suas mudanças e procure a UBS para avaliação individual. Em uma emergência, ligue 192."

    private fun normalize(text: String): String {
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            .matcher(nfd).replaceAll("").lowercase()
    }
}
