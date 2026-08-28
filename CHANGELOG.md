# Changelog

As mudanças importantes do projeto são registradas neste arquivo.

## [2.0.0-local] — 2026-08-24

### Adicionado

- banco Room para usuários, perfis, sintomas, mensagens e artigos;
- DataStore para manter a sessão local;
- interfaces de repository e container simples de dependências;
- cadastro, login, logout e restauração de sessão sem Firebase;
- edição de dados da conta, alteração de senha e exclusão local;
- criação, edição e exclusão de registros de sintomas;
- calendário mensal alimentado por dados persistidos;
- perfil persistente com fase da vida, gestação, foto e datas de exames;
- relatório de sintomas filtrado pelo mês atual;
- histórico persistente para o chat informativo local;
- artigos estruturados em JSON e renderizador Android;
- pesquisa e filtros de artigos no Android;
- navegação com rotas e histórico usando Navigation Compose;
- testes unitários de validação e derivação de senha;
- auditoria técnica inicial em `docs/AUDITORIA_INICIAL.md`;
- documentação do formato de artigos em `docs/FORMATO_ARTIGOS.md`;
- workflow de testes e build no GitHub Actions.

### Painel administrativo

- novo projeto React, TypeScript e Vite em `admin-web`;
- configuração e login de administrador local;
- persistência de artigos em IndexedDB/Dexie;
- dashboard com indicadores e atualizações recentes;
- listagem com pesquisa e filtros;
- criação, edição, duplicação, publicação e exclusão;
- autosave de rascunho;
- editor TipTap com títulos, estilos, listas, citação, links, código, alinhamento e imagens;
- imagem de capa local;
- visualização antes da publicação;
- sanitização de documentos e URLs;
- testes Vitest;
- layout responsivo e arquivo de redirecionamento para SPA.

### Corrigido

- falha que podia indicar sucesso ao salvar um sintoma mesmo quando a operação remota falhava;
- data escolhida no formulário de sintomas, que antes era substituída pelo horário atual;
- instanciação duplicada de estado de autenticação dentro do perfil;
- relatório que dizia representar o mês, mas contava todos os registros;
- informações fixas de gestação exibidas como se fossem dados reais;
- tela vazia causada por captura genérica de exceções na `MainActivity`;
- navegação baseada somente em um enum em memória;
- inconsistências entre o README antigo e o código executado;
- compatibilidade do Gradle Wrapper com runners Linux;
- exigência explícita de opt-in para APIs experimentais do Material 3.

### Alterado

- Firebase Authentication, Realtime Database e Firestore deixaram de ser dependências operacionais;
- ViewModels deixaram de acessar diretamente uma tecnologia de armazenamento;
- artigos deixaram de ficar concentrados como texto hardcoded nas telas;
- o chat passou a deixar explícito que oferece informação geral e não diagnóstico;
- senha local passou a usar derivação PBKDF2 com salt aleatório;
- arquivos de IDE, builds, credenciais e configurações locais passaram a ser ignorados pelo Git.

### Removido

- `app/google-services.json` do versionamento;
- dependências Firebase e plugin Google Services;
- pasta `.idea` versionada;
- botão de login com Google que não possuía implementação real;
- uso operacional de banco remoto nesta edição local.

### Segurança

- senhas não são armazenadas em texto puro;
- backup automático Android desabilitado;
- tráfego HTTP em texto claro bloqueado no Android;
- sanitização de links e conteúdo estruturado do painel;
- remoção de segredos e arquivos locais do controle de versão.

## [1.0.0] — versão inicial

- protótipo Android em Kotlin e Jetpack Compose;
- autenticação Firebase;
- armazenamento parcial no Firebase Realtime Database;
- telas de calendário, sintomas, chat, conteúdos, perfil, conta e Violentômetro.
