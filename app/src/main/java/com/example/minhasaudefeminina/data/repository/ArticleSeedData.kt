package com.example.minhasaudefeminina.data.repository

import com.example.minhasaudefeminina.data.local.ArticleEntity
import com.example.minhasaudefeminina.model.ArtigoStatus

/** Dados de demonstracao locais. Podem ser substituidos por uma API futuramente. */
internal object ArticleSeedData {
    fun create(): List<ArticleEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            article(
                id = "demo-ciclo-menstrual",
                slug = "ciclo-menstrual-entenda-seu-padrao",
                category = "Menstruação",
                title = "Ciclo menstrual: entenda o seu padrão",
                subtitle = "Um guia simples para observar duração, fluxo e mudanças importantes.",
                summary = "Conhecer o próprio padrão ajuda a perceber mudanças e levar informações melhores à consulta.",
                tags = "ciclo,menstruação,autocuidado",
                now = now,
                content = """{
                  "type":"doc",
                  "content":[
                    {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"O que observar"}]},
                    {"type":"paragraph","content":[{"type":"text","text":"O ciclo é contado do primeiro dia de uma menstruação até o dia anterior à próxima. Mais importante do que comparar seu corpo com um número fixo é conhecer o seu padrão e registrar mudanças."}]},
                    {"type":"bulletList","content":[
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"data de início e término do sangramento"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"intensidade do fluxo e presença de dor"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"sangramento fora do padrão habitual"}]}]}
                    ]},
                    {"type":"blockquote","content":[{"type":"paragraph","content":[{"type":"text","text":"O aplicativo ajuda a organizar informações, mas não realiza diagnóstico."}]}]},
                    {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Quando procurar atendimento"}]},
                    {"type":"paragraph","content":[{"type":"text","text":"Procure uma Unidade Básica de Saúde quando houver dor incapacitante, desmaio, febre, sangramento muito intenso ou uma mudança persistente no seu padrão."}]}
                  ]
                }""".trimIndent()
            ),
            article(
                id = "demo-colicas",
                slug = "colicas-alivio-e-sinais-de-alerta",
                category = "Menstruação",
                title = "Cólicas: alívio e sinais de alerta",
                subtitle = "Cuidados simples podem ajudar, mas dor intensa merece avaliação.",
                summary = "Veja medidas de conforto e situações em que é importante buscar atendimento.",
                tags = "cólica,dor,endometriose",
                now = now - 1_000,
                content = """{
                  "type":"doc",
                  "content":[
                    {"type":"paragraph","content":[{"type":"text","text":"Cólicas leves podem melhorar com calor local, descanso, hidratação e movimento suave, respeitando os limites do corpo."}]},
                    {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Sinais de alerta"}]},
                    {"type":"bulletList","content":[
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"dor que impede atividades comuns"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"febre, desmaio ou piora rápida"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"dor recorrente durante relações ou ao evacuar"}]}]}
                    ]},
                    {"type":"paragraph","content":[{"type":"text","text":"Nessas situações, registre os sintomas e procure a UBS. Não use este conteúdo para escolher ou interromper medicamentos por conta própria.","marks":[{"type":"bold"}]}]}
                  ]
                }""".trimIndent()
            ),
            article(
                id = "demo-pre-natal",
                slug = "pre-natal-por-que-comecar-cedo",
                category = "Gestação",
                title = "Pré-natal: por que começar cedo",
                subtitle = "O acompanhamento organiza exames, vacinas e cuidados para a gestante e o bebê.",
                summary = "Na suspeita ou confirmação de gravidez, procure a UBS para iniciar o acompanhamento.",
                tags = "gestação,pré-natal,UBS",
                now = now - 2_000,
                content = """{
                  "type":"doc",
                  "content":[
                    {"type":"paragraph","content":[{"type":"text","text":"Ao suspeitar de gravidez, procure a UBS para confirmação e orientação. O acompanhamento precoce permite avaliar necessidades individuais e planejar consultas e exames."}]},
                    {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Procure atendimento imediato se houver"}]},
                    {"type":"bulletList","content":[
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"sangramento"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"dor forte"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"febre, falta de ar ou desmaio"}]}]}
                    ]},
                    {"type":"paragraph","content":[{"type":"text","text":"Encontre informações oficiais no portal do Ministério da Saúde.","marks":[{"type":"link","attrs":{"href":"https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/s/saude-da-mulher"}}]}]}
                  ]
                }""".trimIndent()
            ),
            article(
                id = "demo-preventivo",
                slug = "preventivo-converse-com-a-ubs",
                category = "Prevenção",
                title = "Exames preventivos: converse com a UBS",
                subtitle = "A indicação depende de idade, histórico e orientações de saúde pública.",
                summary = "Mantenha suas informações organizadas e confirme o calendário indicado para você.",
                tags = "prevenção,Papanicolau,mamografia",
                now = now - 3_000,
                content = """{
                  "type":"doc",
                  "content":[
                    {"type":"paragraph","content":[{"type":"text","text":"O Papanicolau e a mamografia têm objetivos e públicos diferentes. A equipe da UBS pode avaliar seu histórico e explicar quando cada exame é indicado."}]},
                    {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Leve para a consulta"}]},
                    {"type":"orderedList","content":[
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"datas dos últimos exames"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"resultados anteriores, quando disponíveis"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"mudanças percebidas nas mamas ou sangramentos inesperados"}]}]}
                    ]}
                  ]
                }""".trimIndent()
            ),
            article(
                id = "demo-violencia",
                slug = "violencia-canais-de-apoio",
                category = "Proteção",
                title = "Violência contra a mulher: canais de apoio",
                subtitle = "Você não precisa enfrentar uma situação de violência sozinha.",
                summary = "Conheça canais de orientação e use serviços de emergência quando houver risco imediato.",
                tags = "violência,proteção,180",
                now = now - 4_000,
                content = """{
                  "type":"doc",
                  "content":[
                    {"type":"paragraph","content":[{"type":"text","text":"Violência pode ser física, psicológica, sexual, moral ou patrimonial. A culpa nunca é da vítima."}]},
                    {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Canais de ajuda"}]},
                    {"type":"bulletList","content":[
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Ligue 180 para orientação e denúncia"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Ligue 190 em situação de risco imediato"}]}]},
                      {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Procure uma rede de confiança, UBS ou serviço especializado"}]}]}
                    ]},
                    {"type":"blockquote","content":[{"type":"paragraph","content":[{"type":"text","text":"Em emergência, priorize sua segurança e acione o serviço adequado."}]}]}
                  ]
                }""".trimIndent()
            )
        )
    }

    private fun article(
        id: String,
        slug: String,
        category: String,
        title: String,
        subtitle: String,
        summary: String,
        tags: String,
        content: String,
        now: Long
    ) = ArticleEntity(
        id = id,
        slug = slug,
        category = category,
        title = title,
        subtitle = subtitle,
        summary = summary,
        contentJson = content,
        author = "Equipe Minha Saúde Feminina",
        tagsCsv = tags,
        coverUri = null,
        status = ArtigoStatus.PUBLICADO.name,
        createdAt = now,
        updatedAt = now,
        publishedAt = now
    )
}
