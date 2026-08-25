# Formato estruturado dos artigos

## Objetivo

O painel administrativo e o aplicativo Android usam um contrato comum para representar artigos. O texto principal é salvo como JSON estruturado no padrão de documento do TipTap/ProseMirror, em vez de HTML livre.

A versão atual do contrato é:

```text
tiptap-json-v1
```

Essa escolha permite validar os elementos aceitos, bloquear URLs perigosas, renderizar o mesmo conteúdo em tecnologias diferentes e evoluir o formato por versão.

## Estrutura do artigo

```ts
interface Article {
  id: string
  slug: string
  category: string
  title: string
  subtitle: string
  summary: string
  content: ArticleDocument
  author: string
  tags: string[]
  coverImage: string | null
  coverAlt: string
  status: 'draft' | 'published'
  formatVersion: 'tiptap-json-v1'
  createdAt: number
  updatedAt: number
  publishedAt: number | null
}
```

As datas usam milissegundos desde o Unix Epoch.

## Exemplo completo

```json
{
  "id": "artigo-exemplo",
  "slug": "ciclo-menstrual-e-autocuidado",
  "category": "Menstruação",
  "title": "Ciclo menstrual e autocuidado",
  "subtitle": "Informações simples para observar o próprio padrão.",
  "summary": "Aprenda quais informações podem ser registradas e levadas à consulta.",
  "content": {
    "type": "doc",
    "content": [
      {
        "type": "heading",
        "attrs": { "level": 2 },
        "content": [
          { "type": "text", "text": "O que observar" }
        ]
      },
      {
        "type": "paragraph",
        "content": [
          { "type": "text", "text": "Registre mudanças que sejam importantes para você. " },
          {
            "type": "text",
            "text": "Procure atendimento quando houver sinais de alerta.",
            "marks": [
              { "type": "bold" }
            ]
          }
        ]
      },
      {
        "type": "bulletList",
        "content": [
          {
            "type": "listItem",
            "content": [
              {
                "type": "paragraph",
                "content": [
                  { "type": "text", "text": "data de início e término" }
                ]
              }
            ]
          }
        ]
      },
      {
        "type": "paragraph",
        "content": [
          {
            "type": "text",
            "text": "Consulte uma fonte oficial",
            "marks": [
              {
                "type": "link",
                "attrs": {
                  "href": "https://www.gov.br/saude/"
                }
              }
            ]
          }
        ]
      }
    ]
  },
  "author": "Equipe Minha Saúde Feminina",
  "tags": ["ciclo", "autocuidado"],
  "coverImage": null,
  "coverAlt": "",
  "status": "published",
  "formatVersion": "tiptap-json-v1",
  "createdAt": 1787533200000,
  "updatedAt": 1787533200000,
  "publishedAt": 1787533200000
}
```

## Nós aceitos pelo painel

O sanitizador do painel reconhece:

- `doc`;
- `paragraph`;
- `text`;
- `heading`, limitado a H1, H2 e H3;
- `bulletList`;
- `orderedList`;
- `listItem`;
- `blockquote`;
- `codeBlock`;
- `horizontalRule`;
- `hardBreak`;
- `image`.

Nós desconhecidos são descartados antes da persistência.

## Marcações aceitas

- `bold`;
- `italic`;
- `underline`;
- `strike`;
- `code`;
- `link`.

Links só são mantidos quando usam `http://` ou `https://`. O painel adiciona atributos seguros para abertura externa.

## Imagens

### Dentro do conteúdo

O editor aceita imagens por URL HTTP/HTTPS. O sanitizador também reconhece imagens `data:image/...;base64` para compatibilidade local, embora URLs ou um serviço de mídia sejam preferíveis em uma futura API.

### Capa

Na edição local, a capa pode ser convertida em Data URL e salva no IndexedDB. O limite do painel é 2 MB por arquivo.

Quando houver backend, recomenda-se armazenar o arquivo em serviço próprio e manter no artigo apenas a URL e o texto alternativo.

## Compatibilidade Android

O renderizador Android interpreta o núcleo compartilhado:

- parágrafos;
- H1, H2 e H3;
- listas com marcadores e numeradas;
- citações;
- blocos de código;
- separadores;
- imagens com origem permitida;
- negrito, itálico, sublinhado, tachado e links HTTP/HTTPS.

Algumas opções do editor web têm degradação aceitável nesta versão:

- alinhamento é preservado no JSON, mas o Android pode exibir o texto com alinhamento padrão;
- código inline continua legível, ainda que possa não receber estilo monoespaçado no Android;
- recursos futuros não reconhecidos são ignorados em vez de executados.

## Regras de publicação

O painel exige, no mínimo:

- título;
- categoria;
- autor;
- resumo com pelo menos 20 caracteres;
- conteúdo textual não vazio.

Também aplica limites de tamanho para campos e número máximo de tags.

## Contrato recomendado para uma API futura

Uma API pode expor recursos como:

```text
GET    /articles?status=published
GET    /articles/{id}
POST   /admin/articles
PUT    /admin/articles/{id}
DELETE /admin/articles/{id}
POST   /admin/articles/{id}/publish
```

Recomendações:

1. validar `formatVersion` no servidor;
2. executar sanitização novamente no servidor;
3. validar permissões administrativas em cada escrita;
4. usar HTTPS;
5. manter histórico de revisão e autoria;
6. devolver somente artigos publicados ao aplicativo comum;
7. rejeitar campos desconhecidos ou tratá-los de forma versionada;
8. armazenar datas em UTC e converter apenas na apresentação;
9. não confiar em status, autor ou `publishedAt` enviados pelo cliente sem autorização.

## Evolução do formato

Mudanças incompatíveis devem criar uma nova versão, por exemplo `tiptap-json-v2`. O consumidor deve:

- identificar a versão antes de renderizar;
- manter migrações explícitas;
- não reinterpretar silenciosamente documentos incompatíveis;
- conservar um subconjunto básico legível durante transições.
