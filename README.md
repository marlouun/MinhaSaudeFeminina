# Minha Saúde Feminina

Aplicativo Android de acompanhamento pessoal e educação em saúde feminina, acompanhado de um painel web separado para criação e gerenciamento de artigos.

Esta edição foi reorganizada para funcionar **sem Firebase e sem servidor**. O Android salva os dados no próprio aparelho; o painel administrativo salva os artigos no navegador. A estrutura foi preparada com interfaces de repository para permitir a inclusão de uma API no futuro sem reescrever toda a interface.

> **Aviso de saúde:** o aplicativo oferece organização e informação geral. Ele não realiza diagnóstico, não prescreve tratamento e não substitui consulta, Unidade Básica de Saúde ou serviço de emergência.

## Estado atual

### Aplicativo Android

- conta local com cadastro, login, sessão, alteração de dados, troca de senha, logout e exclusão;
- senha derivada com PBKDF2 e salt aleatório, sem armazenamento em texto puro;
- persistência com Room para usuários, perfil, sintomas, chat e artigos;
- DataStore para a sessão local;
- calendário mensal com registros reais;
- criação, edição e exclusão de sintomas, respeitando a data escolhida;
- estimativa simples do próximo ciclo, identificada como aproximação e não como método contraceptivo;
- perfil com fase da vida, gestação, foto e datas de exames;
- relatório mensal de sintomas;
- chat informativo local, baseado em regras e com histórico persistido;
- artigos locais pesquisáveis, separados por categoria e renderizados a partir de JSON estruturado;
- navegação real com Navigation Compose;
- Violentômetro e acesso aos canais de ajuda já existentes no projeto.

### Painel administrativo web

- configuração e login de administrador local;
- dashboard com total, publicados, rascunhos e conteúdos recentes;
- pesquisa e filtros por categoria e status;
- criação, edição, duplicação, visualização, publicação e exclusão;
- autosave de rascunho;
- editor TipTap com títulos, negrito, itálico, sublinhado, tachado, listas, citação, código, alinhamento, links, separadores e imagens por URL;
- imagem de capa local;
- validação antes da publicação;
- sanitização do documento e bloqueio de links fora de HTTP/HTTPS;
- layout responsivo para computador, tablet e celular;
- armazenamento em IndexedDB por meio do Dexie.

## Limitação importante: os dois projetos ainda não sincronizam

O Android e o painel estão totalmente locais, mas usam armazenamentos diferentes:

| Projeto | Onde salva |
|---|---|
| Android | banco Room e DataStore do aparelho |
| Painel web | IndexedDB e armazenamento local do navegador |

Por isso, publicar um artigo no painel **não o envia automaticamente ao Android**. Essa sincronização só será possível após a criação de uma API ou outro backend compartilhado. A camada de repositories já foi separada para facilitar essa evolução.

Também é importante lembrar:

- desinstalar o aplicativo ou limpar seus dados remove os dados Android;
- limpar os dados do site remove a conta administrativa e os artigos do painel;
- abrir o painel em outro navegador ou computador cria outro armazenamento independente;
- autenticação somente no dispositivo/navegador é uma solução temporária e não equivale à segurança de um servidor.

## Tecnologias

### Android

- Kotlin;
- Jetpack Compose e Material 3;
- Navigation Compose;
- Room;
- DataStore Preferences;
- Coroutines e Flow;
- Coil;
- Gradle, KSP e testes JUnit.

### Painel web

- React e TypeScript;
- Vite;
- React Router;
- TipTap/ProseMirror;
- Dexie e IndexedDB;
- Bootstrap e CSS próprio;
- Web Crypto API;
- Vitest.

## Estrutura principal

```text
MinhaSaudeFeminina/
├── app/                         # aplicativo Android
│   └── src/main/java/.../
│       ├── app/                 # container de dependências
│       ├── data/
│       │   ├── local/           # Room e DataStore
│       │   ├── repository/      # contratos e implementações locais
│       │   └── security/        # derivação local de senha
│       ├── domain/              # validação e formato dos artigos
│       ├── navigation/          # rotas do aplicativo
│       ├── ui/                  # telas e componentes Compose
│       └── viewmodel/           # estado e regras de apresentação
├── admin-web/                   # painel administrativo React
│   ├── public/
│   └── src/
│       ├── components/
│       ├── contexts/
│       ├── db/
│       ├── editor/
│       ├── hooks/
│       ├── layout/
│       ├── pages/
│       ├── repositories/
│       ├── security/
│       ├── types/
│       └── utils/
├── docs/
│   ├── AUDITORIA_INICIAL.md
│   └── FORMATO_ARTIGOS.md
└── .github/workflows/ci.yml
```

## Executar o Android no Android Studio

### Requisitos

- Android Studio atualizado;
- JDK 17, normalmente já incluído no Android Studio;
- Android SDK 35 instalado;
- emulador Android ou celular com Android 7.0/API 24 ou superior.

### Passo a passo

1. Clone ou baixe este repositório.
2. Abra o Android Studio.
3. Clique em **Open** e selecione a pasta raiz `MinhaSaudeFeminina` — não selecione somente a pasta `app`.
4. Aguarde o **Gradle Sync** terminar. Na primeira vez, o Android Studio precisará baixar dependências.
5. Abra o **Device Manager** e inicie um emulador, ou conecte um celular com depuração USB habilitada.
6. Na barra superior, escolha a configuração `app` e o dispositivo desejado.
7. Clique no botão **Run** ▶.

Não é necessário criar projeto no Firebase nem adicionar `google-services.json`.

### Validar pelo terminal

No Windows PowerShell ou Prompt de Comando:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

No Linux ou macOS:

```bash
chmod +x ./gradlew
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

O APK de depuração é gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Executar o painel web

### Requisitos

- Node.js 22.12 ou superior;
- npm.

### Passo a passo

```bash
cd admin-web
npm install
npm run dev
```

Abra o endereço exibido pelo Vite, normalmente `http://localhost:5173`.

No primeiro acesso, o painel solicitará a criação de um administrador local. Use um e-mail válido e uma senha com pelo menos oito caracteres, uma letra e um número.

### Testar e gerar a versão de produção

```bash
cd admin-web
npm test
npm run typecheck
npm run build
```

Os arquivos de produção ficam em `admin-web/dist`.

Para Cloudflare Pages, uma configuração possível é:

```text
Root directory: admin-web
Build command: npm run build
Build output directory: dist
```

O arquivo `public/_redirects` já trata as rotas de uma aplicação SPA.

## Arquitetura de dados

A interface não acessa Room, IndexedDB ou autenticação diretamente. Ela depende de contratos como:

- `AuthRepository`;
- `ProfileRepository`;
- `SymptomRepository`;
- `ArticleRepository`;
- `ChatRepository`;
- `AdminAuthRepository` no painel.

Hoje esses contratos são implementados localmente. No futuro, novas implementações poderão consumir uma API, mantendo os ViewModels e a maior parte das telas.

## Formato compartilhado dos artigos

O conteúdo dos artigos não é salvo como HTML livre. Ele usa JSON estruturado compatível com TipTap/ProseMirror e identificado como `tiptap-json-v1`.

Esse formato permite representar parágrafos, títulos, listas, citações, links e outras marcações de forma previsível. O painel sanitiza o documento antes de persistir e o Android interpreta apenas elementos conhecidos.

A especificação detalhada está em [`docs/FORMATO_ARTIGOS.md`](docs/FORMATO_ARTIGOS.md).

## Segurança local

As medidas implementadas reduzem riscos básicos nesta fase:

- senhas não são salvas em texto puro;
- cada senha usa salt aleatório;
- Android e painel usam derivação PBKDF2;
- links de artigos são limitados a HTTP/HTTPS;
- nós desconhecidos do documento são removidos no painel;
- segredos, keystores, `.env`, `google-services.json`, builds e pastas internas da IDE são ignorados pelo Git;
- o Android bloqueia tráfego HTTP em texto claro por padrão;
- o backup Android foi desabilitado para evitar cópia automática dos dados locais.

Essas medidas não transformam o armazenamento local em um backend seguro. Uma versão multiusuário deverá usar autenticação e autorização no servidor, HTTPS, gestão de sessão, logs e regras de acesso.

## Testes e integração contínua

O workflow `.github/workflows/ci.yml` executa:

- testes unitários Android;
- geração do APK de depuração;
- testes Vitest do painel;
- build de produção do painel;
- upload do APK, relatórios e `dist` como artefatos do GitHub Actions quando o build é aprovado.

## Documentação adicional

- [`docs/AUDITORIA_INICIAL.md`](docs/AUDITORIA_INICIAL.md): problemas encontrados na versão anterior e estratégia adotada;
- [`docs/FORMATO_ARTIGOS.md`](docs/FORMATO_ARTIGOS.md): contrato do conteúdo estruturado;
- [`admin-web/README.md`](admin-web/README.md): detalhes específicos do painel;
- [`CHANGELOG.md`](CHANGELOG.md): resumo das mudanças.

## Próximas evoluções recomendadas

1. Criar uma API compartilhada com autenticação e autorização reais.
2. Sincronizar artigos publicados com o Android.
3. Sincronizar dados da usuária somente após definir requisitos de privacidade, consentimento e proteção de dados.
4. Adicionar recuperação segura de conta no servidor.
5. Criar migrações versionadas para Room e para o contrato da API.
6. Ampliar testes instrumentados, testes de interface e acessibilidade.
7. Submeter conteúdos de saúde a revisão profissional antes de publicação pública.
