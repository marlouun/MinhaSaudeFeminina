# Chat híbrido Gemini sem segundo login

O app mantém respostas prontas offline e só envia perguntas abertas após a autorização da usuária. O acesso ao Gemini ocorre automaticamente com uma identidade anônima do Supabase Auth, **sem exigir e-mail ou senha de acesso à IA**. As contas normais do Android continuam locais.

## Fluxo

- Primeira pergunta não prevista: diálogo de consentimento antes do envio de dados para o Google Gemini.
- Depois do consentimento, o app cria automaticamente uma sessão anônima no Supabase Auth.
- A identidade e os tokens de atualização são preservados criptografados no Android Keystore/SharedPreferences, separados por conta local.
- Em perguntas futuras, a sessão é reutilizada ou renovada, preservando a cota diária daquele identificador.
- As respostas prontas continuam ilimitadas e funcionam offline.
- Se Gemini, o servidor ou as cotas falharem, o chat mantém o histórico e exibe orientação educativa ou um erro amigável.
- Somente a pergunta e até seis mensagens recentes do chat são enviadas à IA após consentimento; nenhum perfil ou calendário é enviado automaticamente.

## Limites iniciais no banco de dados

- 2 consultas por minuto por identidade anônima, 5 por dia.
- 8 consultas por minuto no projeto inteiro, 18 por dia no projeto inteiro.
- O dia é calculado no fuso America/Los_Angeles por corresponder à janela diária de quota do Gemini.
- Reservas são atômicas no PostgreSQL, válidas entre diversas instâncias e contam também consultas que falharem no Gemini.
- Apenas Edge Functions com credencial de serviço executam a RPC de quota; toda chamada normal requer JWT emitido por Supabase Auth e validado com a sessão real.

**Importante:** as contas anônimas podem ser recriadas reinstalando ou limpando o aplicativo. Por isso a cota por identidade não comprova quem é uma pessoa, e o limite global é obrigatório para evitar abuso. Para escala pública, adicionar proteção anti-bot adequada à autenticação anônima (ex.: CAPTCHA), controle de criação de sessões e monitoramento de custos. O uso gratuito do Gemini tem quotas próprias que podem mudar.

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
