# Chat híbrido Gemini sem segundo login

O app oferece respostas prontas offline apenas quando um botão de sugestão é selecionado. Todo texto digitado é encaminhado à IA após autorização da usuária, sem correspondência por palavras-chave. O acesso ao Gemini ocorre automaticamente com uma identidade anônima do Supabase Auth, **sem exigir e-mail ou senha de acesso à IA**. As contas normais do Android continuam locais.

## Fluxo

- Primeira pergunta digitada: diálogo de consentimento antes do envio de dados para o Google Gemini.
- Depois do consentimento, o app cria automaticamente uma sessão anônima no Supabase Auth.
- A identidade e os tokens de atualização são preservados criptografados no Android Keystore/SharedPreferences, separados por conta local.
- Em perguntas futuras, a sessão é reutilizada ou renovada, preservando a identidade anônima.
- As respostas prontas dos botões continuam ilimitadas e funcionam offline. Digitar até o mesmo texto de um botão usa a IA, evitando correspondências indevidas por palavras isoladas.
- Se Gemini, o servidor ou as cotas falharem, o chat mantém o histórico e exibe orientação educativa ou um erro amigável.
- A pergunta atual e até 6 mensagens recentes da mesma conta local são enviadas à IA após consentimento explícito, incluindo perguntas e respostas prontas/da IA para manter contexto. Cada mensagem de contexto tem no máximo 500 caracteres. O histórico completo continua no aparelho; somente a fase da vida selecionada no perfil acompanha a pergunta como contexto educativo. Nome, e-mail, exames, calendário e sintomas registrados não são enviados. A autorização pode ser revogada em **Desativar IA**. O consentimento anterior foi invalidado para explicar o novo compartilhamento.

## Limites iniciais no banco de dados

- **Sem teto por identidade anônima durante a fase de testes** (removidos 2/minuto e 5/dia).
- 8 consultas por minuto no projeto inteiro, 18 por dia no projeto inteiro.
- O dia é calculado no fuso America/Los_Angeles por corresponder à janela diária de quota do Gemini.
- Reservas são atômicas no PostgreSQL, válidas entre diversas instâncias e contam também consultas que falharem no Gemini.
- Apenas Edge Functions com credencial de serviço executam a RPC de quota; toda chamada normal requer JWT emitido por Supabase Auth e validado com a sessão real.

**Importante:** as contas anônimas podem ser recriadas reinstalando ou limpando o aplicativo. Não há teto por identidade na fase de testes; o limite global é obrigatório para evitar abuso. Para escala pública, adicionar proteção anti-bot adequada à autenticação anônima (ex.: CAPTCHA), controle de criação de sessões e monitoramento de custos. O uso gratuito do Gemini tem quotas próprias que podem mudar.

## Preparação do Supabase — ordem obrigatória

1. Aplique a migração `supabase/migrations/20261020192100_anonymous_chat_access.sql` antes de habilitar o provedor anônimo. Ela restringe a edição de artigos/imagens a uma lista de administradores. Existe um administrador confirmado no projeto, incluído na lista na migração.
2. Confira as regras de acesso (RLS) de `public.articles` e `storage.objects`. Contas anônimas do Supabase têm role `authenticated`, portanto políticas permissivas de usuário autenticado sem verificação de administrador são inseguras.
3. Vá a Supabase > Authentication > Providers > Anonymous Sign-Ins e habilite o provedor anônimo. Esta configuração é necessária para o app criar sessões sem exigir e-mail. Verifique também as políticas de prevenção de bots.
4. Confirme o segredo `GEMINI_API_KEY` em Edge Functions > Secrets. Não publicar a chave em APK, repositório, logs ou mensagens.
5. Publique `supabase/functions/chat-gemini/index.ts` e `handler.ts`, mantendo `verify_jwt=true`. A função só encaminha chamadas válidas ao Gemini após reservar quota na RPC.
6. Instale a versão Android desta PR. O APK anterior, que pede e-mail, continuará usando o fluxo antigo.

## Operação

Para interromper imediatamente novas chamadas (sem apagar contas):

```sql
update private.chat_settings set enabled = false where singleton = true;
```

Para autorizar outro administrador de conteúdo, conceda explicitamente o acesso à conta desejada em `private.article_admins` usando seu UUID verificado, não por metadados editáveis por usuárias.

As tabelas de quota guardam somente identificadores de conta e contagens, sem mensagens, dados de saúde ou endereços IP. O segredo do Gemini não sai do servidor. O painel administrativo continua exigindo conta de e-mail e senha separada; **somente o segundo login da IA foi removido**.

## Verificação necessária

- Executar os testes Deno: `deno test supabase/functions/chat-gemini/handler_test.ts`.
- Testar o APK: pergunta pronta offline, consentimento aceito/recusado, criação anônima automática, continuidade após reiniciar o app, expiração/renovação da sessão, bloqueio da sexta pergunta diária, limite global.
- Conferir no Supabase que uma sessão anônima não consegue criar, editar ou excluir artigos ou imagens, mas a conta administrativa mantém essas operações.
- Verificar CI de Android e do handler após as últimas alterações.
- Antes de uso público com dados reais de saúde, revisar LGPD, política de privacidade, revogação do consentimento, termos de tratamento de dados do Gemini e conteúdo clínico.

O modelo é `gemini-3.5-flash-lite`. Consulte o plano e as quotas atuais no Google AI Studio antes de abrir acesso a todas as pessoas.


## Validação de infraestrutura — 20/10/2026

- Projeto ACTIVE_HEALTHY; migrações `secure_chat_quota` e `anonymous_chat_access` confirmadas.
- Anonymous Sign-Ins habilitado. Criação limitada a **5 identidades/hora/IP**, além das quotas por identidade e globais.
- CAPTCHA continua desativado: ativá-lo exige integração de desafio no cliente; ativar apenas o botão impediria o app e o painel de autenticar. Antes de exposição pública, integrar proteção anti-bot e política de expurgo de contas anônimas. Limites por IP não impedem abuso distribuído nem substituem CAPTCHA/atestado do dispositivo.
- `chat-gemini` publicada na **versão 4**, `verify_jwt=true`, modelo `gemini-3.5-flash-lite`.
- Chamada real por sessão anônima: HTTP 200, pergunta fictícia de aritmética. Sem token e token inválido: HTTP 401.
- Renovação real de sessão: HTTP 200, mesmo UUID de usuária.
- Conta anônima só lê os 6 artigos publicados; tentativa real de inserção administrativa: HTTP 403/RLS.
- Painel web autenticado mostra os mesmos 7 artigos do banco, incluindo 1 rascunho; administrador permitido pela lista privada. Escrita administrativa validada em transação com rollback, sem alterar os artigos.
- Testes SQL transacionais validaram 2/min e 5/dia por identidade, 8/min e 18/dia globais, sessão inexistente e suspensão central. Contagens reais preservadas por rollback. A reserva usa `pg_advisory_xact_lock` compartilhado entre instâncias.
- Testes automatizados de sessão cobrem persistência entre instâncias, expiração, HTTP 401 antecipado, falha de rede, revogação e chamadas concorrentes. O Android Keystore real depende de teste em dispositivo.
- Não há emulador/dispositivo disponível neste ambiente. Teste de consentimento, histórico, ausência de internet, interface e instalação do APK permanece pendente.

## Restrição de privacidade e liberação pública

**Uso apenas de desenvolvimento, com perguntas fictícias não sensíveis. Não liberar para dados reais de saúde.**

Os [termos do Gemini](https://ai.google.dev/gemini-api/terms), consultados em 20/10/2026, informam que o serviço gratuito pode usar entradas/saídas para melhorar produtos, inclusive com revisão humana, e proíbem submeter dados pessoais, confidenciais ou sensíveis. Também restringem clientes acessíveis a menores de 18 anos e vedam aconselhamento médico/prática clínica. O aviso no Android esclarece esses pontos; consentimento não elimina as restrições do provedor.

Antes de uso público: selecionar condições/provedor adequados a dados sensíveis, definir base legal aplicável da LGPD, política de privacidade, controlador/operadores, retenção/eliminação e transferência internacional, confirmar público adulto e revisar conteúdo com responsável. Nenhum faturamento foi habilitado. Após consentimento atualizado, o app envia somente a pergunta e o contexto recente do chat. Não envia perfil, calendário ou sintomas registrados; o texto livre ainda pode conter dados sensíveis e exige uma solução de privacidade antes da liberação pública.

## Espaçamento do teclado no chat

O recuo do teclado é tratado uma única vez no Scaffold pai, que consome seus insets antes de medir a conversa. A navegação inferior fica oculta enquanto o teclado está aberto no chat. O campo não reserva espaço para um contador vazio. `adjustResize` foi explicitado. A correção exige conferência no mesmo aparelho da captura; não há teste visual em dispositivo neste ambiente.

## Diagnóstico do teste em celular — versão 2.1-chat-anon (código 3)

Foi relatado retorno local de falha às 19h55. Nos registros consultados não apareceu uma nova sessão anônima ou chamada à função correspondente. Isso não confirma a causa no aparelho. O APK passa a distinguir DNS, timeout, TLS, rede, armazenamento seguro e falha de cadastro/renovação por códigos sem expor tokens, perguntas ou mensagens internas de exceção. O tratamento não desativa validação TLS nem autenticação e não registra dados pessoais. A causa final depende de repetir o teste neste APK e informar o código exibido; o teste real anterior de backend continua aprovado.

## Contexto na transição de respostas prontas para IA — 2.2-chat-contexto

- Corrigido o envio vazio de histórico: um pedido como “explique melhor” agora inclui a pergunta anterior e a resposta pronta.
- O ViewModel lê a conversa persistida no Room antes de gravar a nova pergunta. Não depende do cache visual, que pode estar vazio na recriação da tela; a pergunta atual não é duplicada.
- Apenas mensagens da conta local atual, até 6 de 500 caracteres, em ordem, com papéis `user`/`model`. Saudações iniciais isoladas são omitidas. Limpar conversa remove o contexto anterior.
- O aviso explica o envio do contexto; quem já autorizou verá novamente a solicitação (`ai_consent_v3`). Recusar continua impedindo envio e respostas prontas continuam offline e ilimitadas.
- Backend v4 já aceita esse contrato; não há mudança de segredo, autenticação, quotas ou banco. Foram adicionados testes Android da montagem do contexto e teste Deno da entrega em ordem ao provedor.
- Esta mudança não remove a restrição do Gemini gratuito a dados pessoais/sensíveis: continue usando apenas perguntas fictícias ou gerais, sem dados reais de saúde.

## Ajustes para a fase de testes — 2.3-chat-fases

- Removidos os limites individuais de chamadas na RPC, mantendo reserva atômica, autenticação, sessão válida, suspensão central e teto global de **8/minuto e 18/dia**. Contadores individuais servem apenas a contagem operacional, sem bloquear por usuária.
- Perguntas digitadas nunca selecionam resposta pronta, nem mesmo por texto idêntico ao botão. O catálogo dos seis botões possui IDs explícitos. Indisponibilidade/rejeição de consentimento gera orientação local, não uma resposta temática escolhida por palavras-chave.
- A fase da vida é lida novamente do perfil local a cada consulta e enviada no campo `lifeStage`. O backend aceita apenas os seis valores definidos no app e contextualiza suas instruções; não aceita texto arbitrário como instrução de sistema. A fase não comprova idade exata, gravidez ou diagnóstico, e não substitui avaliação profissional.
- O consentimento foi atualizado (`ai_consent_v4`) e precisa ser aceito novamente para o envio da fase selecionada. O restante do perfil não é enviado.
- Testes de conteúdo/fases usam somente dados fictícios. Permanecem as restrições de uso público, dados sensíveis e público adulto do provedor gratuito; a seleção de Adolescência como tema educativo não libera uso por menores.
