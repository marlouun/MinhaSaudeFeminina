package com.example.minhasaudefeminina.domain.chat

/** Offline answers are selected explicitly by chip ID, never by keyword matching. */
data class PreparedChatQuestion(val id: String, val question: String, val answer: String)
sealed interface ChatInput {
    data class Typed(val text: String) : ChatInput
    data class Suggested(val id: String) : ChatInput
}
data class ChatRoute(val question: String, val preparedAnswer: String?)

object PreparedChatResponses {
    val suggestions = listOf(
        PreparedChatQuestion("discharge", "É normal ter corrimento?",
            "O corrimento pode variar ao longo do ciclo. Mudanças de cor, cheiro, quantidade, coceira ou dor merecem avaliação na UBS. Evite automedicação."),
        PreparedChatQuestion("cramps", "O que fazer com cólica?",
            "Calor local e movimento leve podem ajudar em cólicas leves. Dor incapacitante, febre, desmaio ou piora rápida são sinais para procurar atendimento."),
        PreparedChatQuestion("late_period", "Minha menstruação atrasou",
            "Atrasos podem ter várias causas. Se houver possibilidade de gravidez, faça um teste conforme a orientação do produto e procure a UBS se o resultado for positivo ou a dúvida persistir."),
        PreparedChatQuestion("breast_change", "Percebi um caroço na mama",
            "Uma alteração nova na mama, secreção espontânea, retração da pele ou nódulo persistente deve ser avaliado por um profissional."),
        PreparedChatQuestion("contraception", "Quais métodos contraceptivos existem?",
            "Existem preservativos, DIU, pílulas, implantes e outros métodos. A escolha depende do histórico e das necessidades de cada pessoa. A UBS oferece orientação; não inicie ou interrompa hormônios apenas com base no chat."),
        PreparedChatQuestion("urinary_burning", "Sinto ardor ao urinar",
            "Ardor, urgência ou dor para urinar podem indicar um problema urinário. Febre, sangue na urina ou dor nas costas exigem avaliação rápida."),
    )

    fun route(input: ChatInput): ChatRoute? = when (input) {
        is ChatInput.Typed -> input.text.trim().take(500).takeIf(String::isNotBlank)
            ?.let { ChatRoute(it, null) }
        is ChatInput.Suggested -> suggestions.find { it.id == input.id }
            ?.let { ChatRoute(it.question, it.answer) }
    }

    const val OFFLINE_FALLBACK =
        "Não foi possível consultar a IA agora. As perguntas prontas nos botões continuam disponíveis sem internet. Para orientação individual, procure uma UBS. Em caso de urgência, ligue 192."
    const val DECLINED_FALLBACK =
        "Sua pergunta não foi enviada à IA. Você pode usar as perguntas prontas nos botões sem autorizar o envio. Em uma emergência, ligue 192."
}
