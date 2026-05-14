package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.model.Artigo
import com.example.minhasaudefeminina.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducacaoScreen() {
    var categoriaSelecionada by remember { mutableStateOf("Menstruação") }
    var artigoSelecionado by remember { mutableStateOf<Artigo?>(null) }
    
    val categorias = listOf(
        CategoriaInfo("Gestação", Icons.Default.BabyChangingStation),
        CategoriaInfo("Menstruação", Icons.Default.Favorite),
        CategoriaInfo("Contracepção", Icons.Default.Security),
        CategoriaInfo("Prevenção", Icons.Default.HealthAndSafety),
        CategoriaInfo("Climatério", Icons.Default.WbSunny)
    )

    if (artigoSelecionado != null) {
        ArtigoDetalheScreen(artigo = artigoSelecionado!!, onVoltar = { artigoSelecionado = null })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightPinkBackground)
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
                Box(modifier = Modifier.padding(15.dp), contentAlignment = Alignment.Center) {
                    Text("♀ Minha Saúde Feminina", fontSize = 20.sp, color = RosaPrimario, fontWeight = FontWeight.Bold)
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp)) {
                Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF7B42F5), Color(0xFFF542A1)))).padding(24.dp).fillMaxWidth()) {
                    Column {
                        Text("Educação em Saúde", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Conteúdos validados para sua jornada", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                }
            }

            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                items(categorias) { cat ->
                    FilterChip(
                        selected = categoriaSelecionada == cat.nome,
                        onClick = { categoriaSelecionada = cat.nome },
                        label = { Text(cat.nome) },
                        leadingIcon = { Icon(cat.icone, null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RosaSecundario, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    item { Text("Conteúdos sobre $categoriaSelecionada", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 10.dp)) }
                    // CRITICAL: Filter by helper field 'categoria'
                    val artigosFiltrados = getArtigosCompletos().filter { it.categoria == categoriaSelecionada }
                    items(artigosFiltrados) { artigo ->
                        ArtigoCard(artigo, onLerMais = { artigoSelecionado = artigo })
                    }
                }
            }
        }
    }
}

@Composable
fun ArtigoCard(artigo: Artigo, onLerMais: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onLerMais() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = LightPinkBackground.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(RosaSecundario, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(artigo.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(artigo.resumo, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Ler artigo completo →", fontSize = 14.sp, color = RosaSecundario, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtigoDetalheScreen(artigo: Artigo, onVoltar: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artigo.categoria, fontSize = 16.sp, color = Color.Gray) },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)
        ) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)).background(LightPinkBackground.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(artigo.categoria) {
                                "Gestação" -> Icons.Default.BabyChangingStation
                                "Contracepção" -> Icons.Default.Security
                                "Prevenção" -> Icons.Default.HealthAndSafety
                                "Climatério" -> Icons.Default.WbSunny
                                else -> Icons.Default.Favorite
                            },
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = RosaSecundario
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(artigo.titulo, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, lineHeight = 32.sp)
                    Spacer(modifier = Modifier.height(15.dp))
                    HorizontalDivider(color = RosaClaro, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    artigo.conteudo.split("\n\n").forEach { paragraph ->
                        when {
                            paragraph.startsWith("# ") -> {
                                Text(text = paragraph.removePrefix("# "), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RosaPrimario, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            paragraph.startsWith("●") || paragraph.startsWith("-") || paragraph.startsWith("*") -> {
                                Text(text = paragraph, fontSize = 16.sp, color = Color.DarkGray, lineHeight = 24.sp, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                            }
                            paragraph.startsWith("⚠️") -> {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), modifier = Modifier.padding(vertical = 12.dp), shape = RoundedCornerShape(10.dp)) {
                                    Text(text = paragraph, modifier = Modifier.padding(16.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF827717))
                                }
                            }
                            else -> {
                                Text(text = paragraph, fontSize = 16.sp, lineHeight = 26.sp, color = Color(0xFF444444), modifier = Modifier.padding(bottom = 12.dp))
                            }
                        }
                    }

                    if (artigo.referencias.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(30.dp))
                        Text("Referências e Fontes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RosaSecundario)
                        Spacer(modifier = Modifier.height(10.dp))
                        artigo.referencias.forEach { ref ->
                            Text(ref, fontSize = 12.sp, color = Color.Blue, modifier = Modifier.padding(bottom = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

data class CategoriaInfo(val nome: String, val icone: ImageVector)

fun getArtigosCompletos(): List<Artigo> {
    return listOf(
        Artigo(
            id = "M1",
            titulo = "Ciclo Menstrual e Menstruação",
            resumo = "Entenda o fluxo, a duração e o que é considerado saudável.",
            conteudo = "# O que é a Menstruação?\n\nÉ o desprendimento do revestimento do útero quando não há gravidez. Um ciclo saudável varia entre 21 e 36 dias.\n\n# O que é normal?\n\n● Cor: Vermelho vivo no início, podendo ser marrom no final.\n● Volume: Perder entre 30ml e 80ml por ciclo é o normal.\n● Duração: De 3 a 7 dias.\n\n# Pobreza Menstrual\n\nLembre-se que o SUS agora oferece absorventes gratuitos para estudantes e pessoas em situação de vulnerabilidade. Informe-se na sua UBS.",
            categoria = "Menstruação",
            referencias = listOf("Ministério da Saúde - Programa Dignidade Menstrual")
        ),
        Artigo(
            id = "M2",
            titulo = "Cólica: Alívio e Quando se preocupar",
            resumo = "Dicas para lidar com a dor e identificar sinais de alerta.",
            conteudo = "# Cólicas Menstruais\n\nTambém chamadas de dismenorreia, são causadas pela contração do útero para expulsar o endométrio.\n\n# O que você pode fazer em casa:\n\n● Compressas de água morna na região inferior do abdômen.\n● Praticar atividade física leve.\n● Manter hidratação e alimentação saudável.\n\n# Sinais de Alerta\n\n⚠️ Procure a UBS se houver febre, se a dor for incapacitante, se houver sangramento muito intenso ou se notar manchas arroxeadas na pele.\n\n⚠️ Cólicas que não passam com analgésicos comuns devem ser investigadas para afastar Endometriose.",
            categoria = "Menstruação",
            referencias = listOf("Protocolos da Atenção Básica", "Ebook Saúde da Mulher")
        ),
        Artigo(
            id = "M3",
            titulo = "Corrimento Vaginal: Guia Completo",
            resumo = "Aprenda a diferenciar o muco normal das infecções.",
            conteudo = "# O Muco Fisiológico\n\nÉ normal ter um muco transparente ou claro, sem cheiro e que não coça. No período fértil, ele fica elástico como 'clara de ovo'.\n\n# O que NÃO é normal?\n\n● Branco e grumoso (como leite coalhado): Indica candidíase, geralmente com coceira intensa.\n● Amarelo ou esverdeado com odor forte: Pode ser vaginose bacteriana ou IST.\n● Acinzentado: Cheiro fétido que piora após a relação.\n\n⚠️ Gestantes com qualquer tipo de alteração (mesmo leve) precisam de avaliação médica imediata.\n\n# Dicas de Prevenção\n\n● Evite roupas muito apertadas e calcinhas sintéticas.\n● Durma sem calcinha para ventilação da região.\n● Use sabão neutro e evite duchas internas.",
            categoria = "Menstruação",
            referencias = listOf("https://bvsms.saude.gov.br/bvs/publicacoes/protocolos_atencao_basica_saude_mulheres.pdf")
        ),
        Artigo(
            id = "M4",
            titulo = "Sangramento fora do período",
            resumo = "Entenda o sangramento de escape e quando ele é um sinal de alerta.",
            conteudo = "# O que é o Spotting?\n\nSão pequenas gotas de sangue que aparecem fora do período esperado. Pode ocorrer no início de um novo anticoncepcional ou durante a ovulação.\n\n# Quando Investigar?\n\n● Sangramento após a relação sexual.\n● Sangramento após a menopausa (sinal de alerta crítico para câncer de endométrio).\n● Fluxo tão intenso que exige troca de absorvente a cada 1 hora.\n\n⚠️ Todo sangramento fora do seu padrão habitual deve ser avaliado por um profissional na UBS.",
            categoria = "Menstruação"
        ),
        Artigo(
            id = "G1",
            titulo = "Pré-Natal: Início Precoce",
            resumo = "Por que você deve procurar a UBS logo na suspeita de gravidez.",
            conteudo = "# Início do Acompanhamento\n\nO Ministério da Saúde recomenda que o pré-natal comece assim que a gravidez for confirmada. O ideal é antes da 12ª semana.\n\n# Por que é urgente?\n\n● Prevenção de riscos: Identificação de anemia, hipertensão e diabetes gestacional.\n● Testes rápidos: Triagem para HIV, Sífilis e Hepatites no ato da consulta.\n● Suplementação: Início de vitaminas essenciais para a formação do bebê.\n\n⚠️ Gestantes com qualquer dor, sangramento ou febre devem procurar a UBS imediatamente.",
            categoria = "Gestação",
            referencias = listOf("Caderneta da Gestante - Ministério da Saúde")
        ),
        Artigo(
            id = "G2",
            titulo = "Amamentação e Puerpério",
            resumo = "Cuidados com você e seu bebê após o nascimento.",
            conteudo = "# O Poder do Leite Materno\n\nA amamentação exclusiva é recomendada até os 6 meses de vida. Ela protege contra infecções e fortalece o vínculo.\n\n# Saúde da Puérpera\n\nO pós-parto (puerpério) é uma fase de grandes mudanças. Mantenha a rede de apoio ativa e procure a UBS para revisão e vacinação do bebê.\n\n⚠️ Tristeza persistente no pós-parto não é frescura. Busque ajuda psicológica na sua unidade de saúde.",
            categoria = "Gestação"
        ),
        Artigo(
            id = "C1",
            titulo = "Planejamento Reprodutivo e Métodos",
            resumo = "Escolha o método contraceptivo ideal disponível no SUS.",
            conteudo = "# Métodos no SUS\n\n● DIU de Cobre: Sem hormônios e longa duração.\n● Injetáveis: Mensais ou trimestrais.\n● Pílulas: Combinadas ou minipílula para quem amamenta.\n● Preservativos: Únicos que protegem contra ISTs.\n\n# Como escolher?\n\nA escolha deve ser consciente e acompanhada por profissionais de saúde. Participe dos grupos de planejamento familiar na UBS.",
            categoria = "Contracepção",
            referencias = listOf("https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/p/planejamento-familiar")
        ),
        Artigo(
            id = "C2",
            titulo = "Lei da Laqueadura (14.443/2022)",
            resumo = "Conheça as novas regras para esterilização voluntária.",
            conteudo = "# Novas Regras (2023)\n\n● Idade mínima: 21 anos (ou 18 anos se tiver pelo menos 2 filhos vivos).\n● Sem autorização do cônjuge: A mulher agora decide sozinha sobre o procedimento.\n● Acompanhamento: É necessário um intervalo de 60 dias entre a vontade manifestada e o procedimento para aconselhamento.\n\n⚠️ Informe-se na sua UBS sobre a agenda de cirurgias e protocolos municipais.",
            categoria = "Contracepção"
        ),
        Artigo(
            id = "P1",
            titulo = "Exame Papanicolau",
            resumo = "Tudo sobre o preventivo que salva vidas.",
            conteudo = "# O que é?\n\nExame que detecta o câncer de colo de útero antes mesmo dele se manifestar. \n\n# Quem deve fazer?\n\nMulheres de 25 a 64 anos que já iniciaram atividade sexual.\n\n⚠️ Procure a UBS se você está há mais de um ano sem realizar o exame ou se nunca fez.",
            categoria = "Prevenção"
        ),
        Artigo(
            id = "P2",
            titulo = "Saúde das Mamas",
            resumo = "Autoexame mensal e mamografia de rastreamento.",
            conteudo = "# Sinais de Alerta\n\n● Caroço (nódulo) fixo e indolor.\n● Alterações no mamilo ou pele da mama.\n● Secreção espontânea.\n\n⚠️ A mamografia de rotina é recomendada pelo Ministério da Saúde a partir dos 50 anos (ou 40 conforme avaliação clínica).",
            categoria = "Prevenção"
        ),
        Artigo(
            id = "P3",
            titulo = "Saúde Urinária: Dor e Ardor",
            resumo = "Identifique os sinais de Infecção Urinária (ITU).",
            conteudo = "# Sintomas Comuns\n\n● Urgência em urinar (vontade toda hora).\n● Ardor ou dor ao sair o xixi.\n● Dor no baixo ventre ou nas costas.\n\n# Quando procurar a UBS?\n\n● Se o ardor persistir por mais de um dia.\n● Presença de sangue na urina.\n● Febre, calafrios ou dor lombar forte.\n\n⚠️ Beber água regularmente é a melhor forma de prevenir infecções urinárias. Não segure o xixi por muito tempo.",
            categoria = "Prevenção",
            referencias = listOf("Ministério da Saúde - Cadernos de Atenção Básica")
        ),
        Artigo(
            id = "L1",
            titulo = "Fogachos e Menopausa",
            resumo = "Dicas práticas para as ondas de calor.",
            conteudo = "# O Climatério\n\nFase de transição hormonal (40-65 anos). A menopausa é confirmada após 1 ano sem menstruar.\n\n# Dicas de Alívio\n\n● Hidratação constante.\n● Ambientes ventilados.\n● Exercícios físicos regulares para saúde óssea.",
            categoria = "Climatério"
        ),
        Artigo(
            id = "L2",
            titulo = "TPM e Alterações de Humor",
            resumo = "Entenda por que suas emoções oscilam e como se cuidar.",
            conteudo = "# A Dança dos Hormônios\n\nAntes da menstruação, a queda de estrogênio afeta a serotonina, o que pode causar irritabilidade, tristeza ou sensibilidade.\n\n# Como Melhorar?\n\n● Alimentação equilibrada rica em fibras.\n● Reduzir café e açúcar nos dias que antecedem o ciclo.\n\n⚠️ Se você sente que a TPM está 'atrapalhando sua vida' ou causando sobrecarga emocional extrema, busque apoio psicológico na UBS. Pode ser TDPM (Transtorno Disfórico Pré-Menstrual).",
            categoria = "Climatério"
        ),
        Artigo(
            id = "S1",
            titulo = "Violência Contra a Mulher",
            resumo = "Identifique os sinais de abuso e saiba como pedir ajuda.",
            conteudo = "# Não se cale!\n\nA violência contra a mulher pode ser física, psicológica, sexual, patrimonial ou moral.\n\n# Onde buscar ajuda?\n\n● Ligue 180.\n● UBS (os profissionais estão prontos para te acolher).\n● Delegacia da Mulher.\n\n⚠️ Você não está sozinha. Busque sua rede de apoio.",
            categoria = "Prevenção",
            referencias = listOf("Lei Maria da Penha")
        )
    )
}
