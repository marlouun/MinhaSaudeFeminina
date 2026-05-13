# APP Minha Saúde Feminina

<div align="center">

**Aplicativo Android de saúde feminina com foco em educação, acompanhamento menstrual e suporte à saúde da mulher**


</div>

---

##  Sobre

**Minha Saúde Feminina** é um aplicativo Android desenvolvido em Kotlin com Jetpack Compose que visa empoderar mulheres através de informações confiáveis sobre saúde feminina, acompanhamento do ciclo menstrual e acesso a conteúdos educacionais validados.

O aplicativo foi desenvolvido com base nas diretrizes do **Ministério da Saúde** e nos **Protocolos da Atenção Básica**, oferecendo informações seguras e acessíveis para todas as fases da vida da mulher.

###  Objetivos

- Democratizar o acesso à informação sobre saúde feminina
- Auxiliar no acompanhamento do ciclo menstrual
- Educar sobre prevenção e cuidados com a saúde
- Combater a desinformação através de conteúdo validado
- Promover o autocuidado e a autonomia feminina

---

##  Funcionalidades

###  Calendário Menstrual
- Visualização mensal do ciclo com marcadores visuais
- Identificação de períodos de menstruação, ovulação e fase fértil
- Calculadora de atraso menstrual com alertas
- Registro de sintomas diários (cólica, corrimento, humor, etc.)
- Histórico completo de registros

###  Chat de Dúvidas
- Sistema de perguntas e respostas sobre saúde feminina
- Sugestões rápidas de tópicos comuns
- Interface conversacional intuitiva
- Respostas baseadas em informações validadas

###  Educação em Saúde
- Artigos completos sobre diversos temas:
  - **Menstruação**: Ciclo, cólicas, corrimento, sangramento
  - **Gestação**: Pré-natal, amamentação, puerpério
  - **Contracepção**: Métodos disponíveis no SUS, laqueadura
  - **Prevenção**: Papanicolau, saúde das mamas, infecções urinárias
  - **Climatério**: Menopausa, fogachos, TPM
- Conteúdo organizado por categorias
- Referências e fontes oficiais
- Interface de leitura otimizada

###  Perfil do Usuário
- Gerenciamento de dados pessoais
- Configuração de fase de vida (adolescência, idade reprodutiva, gestação, climatério, etc.)
- Registro de exames importantes (Papanicolau, mamografia)
- Autenticação segura com Firebase

### Autenticação
- Login e cadastro com e-mail e senha
- Integração com Firebase Authentication
- Proteção de dados pessoais
- Recuperação de senha

---

##  Tecnologias

### Core
- **Kotlin** - Linguagem de programação
- **Jetpack Compose** - UI moderna e declarativa
- **Material Design 3** - Design system

### Arquitetura
- **MVVM** (Model-View-ViewModel)
- **StateFlow** - Gerenciamento de estado reativo
- **ViewModel** - Ciclo de vida consciente

### Backend & Dados
- **Firebase Authentication** - Autenticação de usuários
- **Firebase Firestore** - Banco de dados NoSQL em tempo real
- **Firebase BOM** - Gerenciamento de versões

### Bibliotecas Android
- **AndroidX Core KTX** - Extensões Kotlin para Android
- **Lifecycle Runtime KTX** - Componentes de ciclo de vida
- **Activity Compose** - Integração de Activities com Compose
- **Material Icons Extended** - Ícones do Material Design
- **Navigation Compose** - Navegação entre telas

### Build & Ferramentas
- **Gradle KTS** - Build system com Kotlin DSL
- **Android Gradle Plugin** - Compilação Android
- **Desugar JDK Libs** - Suporte a APIs Java 8+ em versões antigas do Android

### Requisitos
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Java Version**: 11

---

##  Instalação

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 11 ou superior
- Conta no Firebase (para configuração do backend)

### Passo a Passo

1. **Clone o repositório**
```bash
git clone https://github.com/seu-usuario/minhasaudefeminina.git
cd minhasaudefeminina
```

2. **Configure o Firebase**
   - Crie um projeto no [Firebase Console](https://console.firebase.google.com/)
   - Adicione um app Android com o package name: `com.example.minhasaudefeminina`
   - Baixe o arquivo `google-services.json`
   - Coloque o arquivo em `app/google-services.json`
   - Ative **Authentication** (Email/Password) e **Firestore** no console

3. **Abra o projeto no Android Studio**
```bash
# No terminal do Android Studio
./gradlew build
```

4. **Execute o aplicativo**
   - Conecte um dispositivo Android ou inicie um emulador
   - Clique em "Run" (▶️) ou pressione `Shift + F10`

---

##  Estrutura do Projeto

```
app/src/main/java/com/example/minhasaudefeminina/
│
├── 📱 MainActivity.kt                    # Activity principal
│
├── 🎨 ui/
│   ├── screens/                          # Telas do aplicativo
│   │   ├── HomeScreen.kt                 # Calendário menstrual
│   │   ├── ChatScreen.kt                 # Chat de dúvidas
│   │   ├── EducacaoScreen.kt             # Artigos educacionais
│   │   ├── PerfilScreen.kt               # Perfil do usuário
│   │   ├── LoginScreen.kt                # Autenticação
│   │   └── RegistrarSintomaScreen.kt     # Registro de sintomas
│   │
│   └── theme/                            # Tema e cores
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── 🧠 viewmodel/                         # Lógica de negócio
│   ├── AuthViewModel.kt                  # Autenticação
│   ├── ChatViewModel.kt                  # Chat
│   ├── PerfilViewModel.kt                # Perfil
│   └── SintomasViewModel.kt              # Sintomas e calendário
│
└── 📊 model/                             # Modelos de dados
    └── SintomaModels.kt                  # Entidades do Firestore
```

### Modelos de Dados (Firestore)

O aplicativo utiliza as seguintes coleções no Firestore:

- **usuario** - Dados básicos do usuário
- **perfil_usuario** - Informações de saúde e fase de vida
- **configuracao_usuario** - Preferências e configurações
- **registro_sintoma** - Sintomas registrados diariamente
- **ciclo_menstrual** - Dados do ciclo menstrual
- **dados_gestacao** - Informações de gestação
- **consulta_prenatal** - Consultas pré-natal
- **mensagem_chat** - Histórico de conversas
- **artigo** - Conteúdos educacionais

---

##  Design

### Paleta de Cores

- **Rosa Primário** (`#E91E63`) - Elementos principais
- **Rosa Secundário** (`#F06292`) - Destaques e botões
- **Rosa Claro** (`#FCE4EC`) - Backgrounds sutis
- **Light Pink Background** (`#FFF0F5`) - Fundo geral

### Componentes Visuais

- **Bottom Navigation Bar** - Navegação principal com 4 seções
- **Floating Action Button** - Registro rápido de sintomas
- **Cards** - Apresentação de informações
- **Calendar Grid** - Visualização mensal customizada

---

##  Funcionalidades Futuras

- [ ] Notificações push para lembretes
- [ ] Exportação de dados em PDF
- [ ] Gráficos e estatísticas do ciclo
- [ ] Modo escuro
- [ ] Suporte a múltiplos idiomas
- [ ] Integração com Google Fit
- [ ] Compartilhamento de dados com médicos
- [ ] Violentômetro (identificação de violência doméstica)
- [ ] Localização de UBS próximas

---

##  Como Contribuir

Contribuições são bem-vindas! Siga os passos abaixo:

1. **Fork** o projeto
2. Crie uma **branch** para sua feature (`git checkout -b feature/MinhaFeature`)
3. **Commit** suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. **Push** para a branch (`git push origin feature/MinhaFeature`)
5. Abra um **Pull Request**

### Diretrizes

- Siga os padrões de código Kotlin
- Mantenha a arquitetura MVVM
- Adicione comentários em código complexo
- Teste suas alterações antes de enviar
- Atualize a documentação se necessário

---

##  Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

##  Autores

Estudantes do curso de SI da instituição Unifebe, no qual são: Leonardo Petri Hammer, Eduardo Tinti, Marlon Albino, Bruno Wozniak, Mario Reis

---

##  Contato e Suporte

- **Issues**: [GitHub Issues](https://github.com/seu-usuario/minhasaudefeminina/issues)
- **Discussões**: [GitHub Discussions](https://github.com/seu-usuario/minhasaudefeminina/discussions)

---

##  Aviso Importante

**Este aplicativo tem fins educacionais e informativos.** As informações fornecidas não substituem avaliação médica profissional. Sempre procure uma Unidade Básica de Saúde (UBS) ou profissional de saúde qualificado para diagnóstico e tratamento.

### Recursos de Emergência

- **Ligue 180** - Central de Atendimento à Mulher
- **Ligue 190** - Polícia Militar (emergências)
- **Ligue 192** - SAMU (emergências médicas)
- **Ligue 188** - CVV (apoio emocional)

---

## Referências

- [Ministério da Saúde - Saúde da Mulher](https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/s/saude-da-mulher)
- [Protocolos da Atenção Básica](https://bvsms.saude.gov.br/bvs/publicacoes/protocolos_atencao_basica_saude_mulheres.pdf)
- [Caderneta da Gestante](https://bvsms.saude.gov.br/bvs/publicacoes/caderneta_gestante.pdf)
- [Lei Maria da Penha](http://www.planalto.gov.br/ccivil_03/_ato2004-2006/2006/lei/l11340.htm)
- [Lei 14.443/2022 - Laqueadura](http://www.planalto.gov.br/ccivil_03/_ato2019-2022/2022/lei/L14443.htm)

---

<div align="center">


</div>
