# Chat híbrido Gemini

O app responde localmente às perguntas já previstas e só consulta o Gemini quando não há resposta pronta e a usuária autoriza. A chave do Google deve permanecer exclusivamente no backend Supabase Edge Function.

## Para ativar
1. Reativar o projeto Supabase se estiver pausado.
2. Criar uma chave Gemini no Google AI Studio, conferindo a elegibilidade ao plano gratuito.
3. Cadastrar GEMINI_API_KEY em Supabase > Edge Functions > Secrets. Nunca colocar essa chave no APK, no GitHub ou em uma mensagem.
4. Implantar supabase/functions/chat-gemini/index.ts como a função chat-gemini (verify_jwt=false porque as contas do app ainda são locais).
5. Fazer build do Android e testar perguntas locais, perguntas novas, consentimento e ausência de conexão.

## PRIVACIDADE E SEGURANÇA
O login Android é exclusivamente local, portanto NÃO existe autenticação confiável da usuária no backend. A publishable key é pública. O limite por IP em memória é apenas uma proteção provisória, não uma quota confiável entre instâncias. NÃO lançar publicamente sem autenticação no servidor, cotas persistentes, monitoramento de custos e prevenção de abuso.

Os dados enviados à IA são a pergunta e até seis mensagens recentes, nunca dados do perfil/calendário. O consentimento é salvo por conta local no aparelho e a pergunta não é enviada em caso de recusa. Para produção, implementar revogação do consentimento, política de privacidade e análise de LGPD, especialmente para dados sensíveis de saúde, além de revisão por profissionais.

A IA é exclusivamente educativa: não diagnostica ou prescreve. Os prompts de segurança podem falhar. Serviços sem chave ou indisponíveis respondem com orientação genérica local.

## Verificação
Rodar ./gradlew testDebugUnitTest e ./gradlew assembleDebug; testar manualmente em dispositivo Android.