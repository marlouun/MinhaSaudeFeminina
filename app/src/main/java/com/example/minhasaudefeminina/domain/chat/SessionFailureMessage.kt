package com.example.minhasaudefeminina.domain.chat

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.GeneralSecurityException
import javax.net.ssl.SSLException

/** Deliberately never exposes exception messages, tokens or request contents. */
internal fun sessionFailureMessage(error: Exception): String = when (error) {
    is UnknownHostException -> "Não foi possível localizar o servidor da IA (IA-DNS). Verifique a internet do celular e tente novamente."
    is SocketTimeoutException -> "A conexão com a IA demorou demais (IA-TEMPO). Tente novamente com uma conexão estável."
    is SSLException -> "Não foi possível estabelecer uma conexão segura com a IA (IA-TLS). Confira a data e a hora automáticas do celular e a conexão de internet."
    is GeneralSecurityException -> "O celular não conseguiu acessar o armazenamento seguro da sessão (IA-SEGURA). Feche e reabra o aplicativo."
    is IOException -> "Não foi possível conectar ao servidor da IA (IA-REDE). Verifique a internet e tente novamente."
    else -> "Não foi possível preparar a sessão da IA neste aparelho (IA-SESSAO). Feche e reabra o aplicativo; se persistir, informe este código."
}
