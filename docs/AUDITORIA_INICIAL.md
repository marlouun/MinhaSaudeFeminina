# Auditoria inicial do projeto Minha Saude Feminina

Data da auditoria: 24 de agosto de 2026
Branch de trabalho: `codex/offline-local-admin`

Este documento registra o estado encontrado **antes** da migracao para armazenamento local.

## Situacao atual

### Funciona

- O projeto possui uma interface Android em Kotlin e Jetpack Compose.
- As telas principais existem: login, calendario, registro de sintomas, chat, conteudos, perfil, conta e violentometro.
- O calendario consegue exibir os registros que chegam ao estado da tela.
- O violentometro abre o discador para o numero 180.
- O chat responde por regras locais, sem chamar uma inteligencia artificial externa.

### Funciona parcialmente

- Login e cadastro dependem completamente do Firebase Authentication.
- Sintomas e perfil dependem do Firebase Realtime Database e de um endereco de banco fixo no codigo.
- A foto de perfil fica apenas em memoria e desaparece ao recriar a tela/processo.
- A area de conteudos abre artigos, mas todos estao escritos diretamente em um unico arquivo Kotlin.
- A navegacao troca um enum em memoria; nao existe historico de navegacao real.
- O relatorio de sintomas soma todos os registros, apesar de a tela dizer que representa o mes.

### Nao funciona ou nao existe

- O botao de login com Google e apenas visual.
- Recuperacao de senha, embora citada no README, nao foi implementada.
- Edicao e exclusao de registros de sintomas nao existem.
- Edicao completa do perfil e exclusao da conta nao existem.
- Datas de Papanicolau e mamografia nao sao persistidas.
- O painel administrativo web nao existe.
- Nao ha sincronizacao possivel entre um painel e o Android sem uma API futura.
- Nao ha busca de artigos nem renderizacao segura de links formatados.
- Nao ha pipeline de build/teste no GitHub Actions.

## Bugs encontrados

1. Ao falhar o envio de um sintoma ao Firebase, o `catch` marcava a operacao como sucesso. A interface podia afirmar que salvou um dado que nunca foi gravado.
2. O formulario sempre substituia a data do registro pelo horario atual.
3. A tela de gestacao exibia semana e proxima consulta fixas, sem dados do usuario.
4. O README descrevia Firestore, Navigation Compose, recuperacao de senha e outras funcoes que nao correspondiam ao codigo.
5. Uma nova instancia de `AuthViewModel` era criada dentro do perfil, duplicando estado.
6. O projeto versionava `google-services.json` e varios arquivos internos do Android Studio.
7. A `MainActivity` escondia falhas de composicao em um `try/catch`, deixando a tela vazia em vez de corrigir a causa.
8. O aplicativo declarava que conteudos eram validados, mas as fontes e atualizacoes nao eram gerenciadas como dados.

## Problemas de arquitetura

- ViewModels acessavam Firebase diretamente.
- Nao existiam interfaces de repository.
- Nao existiam Room, DataStore ou uma camada local estruturada.
- A logica de navegacao estava concentrada na Activity.
- Conteudos, estado e regras de negocio estavam misturados em Composables e arquivos extensos.
- Listeners do Realtime Database nao eram removidos explicitamente.

## Firebase/banco utilizado

- Firebase Authentication para conta, sessao e senha.
- Firebase Realtime Database para perfil, gestacao e sintomas.
- Firestore aparecia nas dependencias e no README, mas nao era a fonte usada pelos ViewModels auditados.
- O endereco do Realtime Database estava fixo nos ViewModels.

## Dados sem persistencia confiavel

- Foto de perfil.
- Conversas do chat.
- Datas de exames.
- Estado de navegacao apos encerramento.
- Artigos como dados editaveis.
- Qualquer salvamento quando o Firebase estivesse indisponivel.

## Estrategia adotada

- Room para usuarios locais, perfis, sintomas, mensagens e artigos.
- DataStore para manter o identificador da sessao local.
- PBKDF2 com salt aleatorio para nao armazenar senha em texto puro.
- Interfaces de repository para permitir uma futura troca por API/Firebase.
- Navigation Compose para rotas e back stack.
- Conteudo de artigos no formato JSON estruturado compativel com TipTap/ProseMirror.
- Painel web separado com IndexedDB e a mesma estrutura de artigo.

## Limite importante

Esta versao sera totalmente local. Os dados do Android ficam no aparelho, e os dados do painel ficam no navegador em que foram criados. Sem uma API/backend, os dois projetos nao sincronizam entre si.
