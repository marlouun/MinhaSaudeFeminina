# Chat híbrido Gemini

As perguntas previstas continuam sendo respondidas offline. Perguntas novas só são enviadas após consentimento e login separado no acesso à IA. O login e os dados locais do aplicativo continuam locais.

## Proteção implementada

- `chat-gemini` exige `verify_jwt=true` e valida o token com Supabase Auth (`/auth/v1/user`). Chaves públicas não são identidade.
- Contas devem ter e-mail confirmado, não podem ser anônimas e precisam estar habilitadas em `private.chat_access`.
- Cada chamada também confere a existência/validade da sessão em `auth.sessions` e o bloqueio da conta. A autorização não vem de `user_metadata`.
- Reservas atômicas no Postgres impedem ultrapassar cotas com chamadas simultâneas ou em várias instâncias. Máximos: 2/minuto e 5/dia por conta; 8/minuto e 18/dia no projeto inteiro. O dia acompanha `America/Los_Angeles`, usado pela cota diária Gemini.
- Tentativas com falha no provedor também contam. Sem banco/segredo/autorização, o serviço não chama Gemini.
- O corpo real é limitado a 18 KB; pergunta e mensagens têm até 500 caracteres e histórico até 6 mensagens.
- A senha não é salva. O token fica somente na memória e é descartado ao sair da tela do chat ou ao expirar. Não há chave Gemini, service role ou refresh token no APK.
- As tabelas de controle guardam apenas UUID de conta e contadores, sem perguntas, respostas ou IPs. Contadores antigos são removidos após dois dias.

## Estado verificado em 20/10/2026

- Supabase ativo no plano Free; `GEMINI_API_KEY` cadastrado.
- Migração aplicada após autorização explícita; permissões, RLS, bloqueio por autorização, cotas por minuto/dia e interrupção do serviço conferidos com testes transacionais revertidos.
- Edge Function `chat-gemini` ativa, versão 3, com `verify_jwt=true`.
- Requisições sem token, com token inválido e com chave anon pública foram recusadas com 401.
- Teste real autenticado no dashboard: HTTP 200, resposta "Dois mais dois é igual a quatro." usando `gemini-3.5-flash-lite`.
- 10 testes do handler aprovados. CI Android e painel web aprovados no commit anterior (as últimas alterações afetam somente backend, seus testes e esta documentação).
- Nenhuma conta normal foi liberada automaticamente para IA. É necessário cadastrar/autorizar as contas do piloto e instalar o APK da PR. O fluxo em dispositivo Android ainda não foi testado.
- Nenhum faturamento pago foi ativado e a PR não foi mesclada.

## Configuração e ativação

1. O segredo `GEMINI_API_KEY` deve estar em Edge Functions > Secrets. O modelo é `gemini-3.5-flash-lite`. O modelo anterior `gemini-2.5-flash-lite` retornou 404 no teste real deste projeto.
2. Migração aplicada neste projeto: `supabase/migrations/20261020120412_secure_chat_quota.sql`. Em outro ambiente, aplicar somente após autorização para criar as tabelas e permissões. Não altera tabelas de artigos ou dados locais Android.
3. Criar uma conta normal de acesso à IA no Supabase Auth, com e-mail confirmado. Não usar a conta administrativa do painel no celular.
4. Autorizar uma conta específica pelo UUID em uma operação administrativa:

```sql
insert into private.chat_access(user_id, enabled)
values ('UUID_DA_CONTA_AUTH', true)
on conflict (user_id) do update set enabled = excluded.enabled;
```

5. Publicar os arquivos `index.ts` e `handler.ts` de `supabase/functions/chat-gemini`, mantendo `verify_jwt=true`.
6. Gerar o APK da PR e testar com a conta autorizada. O APK antigo não envia JWT e será recusado.

Não cadastrar automaticamente contas existentes nem habilitar cadastro anônimo. A liberação é individual durante o piloto, em função da cota total pequena. A senha/e-mail de acesso à IA são diferentes do cadastro local.

## Operação

Para suspender novas consultas sem apagar dados:

```sql
update private.chat_settings set enabled = false where singleton;
```

Para revogar apenas uma conta:

```sql
update private.chat_access set enabled = false where user_id = 'UUID_DA_CONTA_AUTH';
```

O operador pode executar uma pergunta fictícia pelo testador do dashboard usando a credencial `service_role` que o próprio dashboard administra. O handler aceita essa credencial secreta para chamadas de servidor; aplica a mesma reserva persistente (ator `server`, também limitado a 5/dia). Ela nunca deve ser copiada para o aplicativo ou para mensagens. Tokens arbitrários com role forjada não são aceitos. Para chaves administrativas legadas rotacionadas, a própria RPC restrita valida a credencial com as permissões PostgREST antes de reservar a cota, sem confiar em roles decodificadas.

## Verificação

- `deno check supabase/functions/chat-gemini/index.ts`
- `deno test supabase/functions/chat-gemini/handler_test.ts`
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- Testes reais: sem token → 401; token inválido → 401; conta não autorizada/revogada → 403; limite excedido → 429 sem chamar Gemini; conta liberada → resposta válida.
- No dispositivo: perguntas prontas/offline, consentimento recusado, login IA cancelado, sessão expirada, falha de rede e consulta nova.

O fluxo local/Android e a disponibilidade real da API precisam ser verificados além dos testes simulados. Publicar o backend não instala o APK nem faz merge da PR.

## Privacidade

São enviados ao Gemini a pergunta e até seis mensagens recentes, nunca perfil ou calendário. A IA é educativa; não diagnostica ou prescreve. Serviços e prompts podem falhar. Antes de uso público com dados reais de saúde, revisar política de privacidade, revogação do consentimento, condições de tratamento do plano gratuito, LGPD e conteúdo clínico. O piloto deve usar perguntas fictícias e não sensíveis até essa revisão.
