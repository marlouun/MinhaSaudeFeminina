package com.example.minhasaudefeminina.domain.content

import org.json.JSONArray
import org.json.JSONObject

sealed interface ArticleBlock {
    data class Paragraph(val content: List<RichSpan>) : ArticleBlock
    data class Heading(val level: Int, val content: List<RichSpan>) : ArticleBlock
    data class BulletList(val items: List<List<RichSpan>>) : ArticleBlock
    data class OrderedList(val items: List<List<RichSpan>>) : ArticleBlock
    data class Quote(val content: List<RichSpan>) : ArticleBlock
    data class Code(val text: String) : ArticleBlock
    data class Image(val source: String, val alternativeText: String) : ArticleBlock
    data object Divider : ArticleBlock
}

data class RichSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strike: Boolean = false,
    val link: String? = null
)

object ArticleDocumentParser {
    fun parse(json: String): List<ArticleBlock> = runCatching {
        val root = JSONObject(json)
        if (root.optString("type") != "doc") return@runCatching emptyList()
        parseBlocks(root.optJSONArray("content") ?: JSONArray())
    }.getOrElse {
        listOf(ArticleBlock.Paragraph(listOf(RichSpan("Não foi possível exibir o conteúdo deste artigo."))))
    }

    private fun parseBlocks(nodes: JSONArray): List<ArticleBlock> = buildList {
        for (index in 0 until nodes.length()) {
            val node = nodes.optJSONObject(index) ?: continue
            when (node.optString("type")) {
                "paragraph" -> add(ArticleBlock.Paragraph(parseInline(node.optJSONArray("content"))))
                "heading" -> add(
                    ArticleBlock.Heading(
                        level = node.optJSONObject("attrs")?.optInt("level", 2)?.coerceIn(1, 3) ?: 2,
                        content = parseInline(node.optJSONArray("content"))
                    )
                )
                "bulletList" -> add(ArticleBlock.BulletList(parseList(node.optJSONArray("content"))))
                "orderedList" -> add(ArticleBlock.OrderedList(parseList(node.optJSONArray("content"))))
                "blockquote" -> add(ArticleBlock.Quote(parseNestedInline(node.optJSONArray("content"))))
                "codeBlock" -> add(ArticleBlock.Code(extractText(node.optJSONArray("content"))))
                "horizontalRule" -> add(ArticleBlock.Divider)
                "image" -> {
                    val attrs = node.optJSONObject("attrs")
                    val source = attrs?.optString("src").orEmpty()
                    if (isSafeUrl(source) || source.startsWith("content://")) {
                        add(ArticleBlock.Image(source, attrs?.optString("alt").orEmpty()))
                    }
                }
            }
        }
    }

    private fun parseList(nodes: JSONArray?): List<List<RichSpan>> {
        if (nodes == null) return emptyList()
        return buildList {
            for (index in 0 until nodes.length()) {
                val item = nodes.optJSONObject(index) ?: continue
                add(parseNestedInline(item.optJSONArray("content")))
            }
        }
    }

    private fun parseNestedInline(nodes: JSONArray?): List<RichSpan> {
        if (nodes == null) return emptyList()
        val result = mutableListOf<RichSpan>()
        for (index in 0 until nodes.length()) {
            val node = nodes.optJSONObject(index) ?: continue
            when (node.optString("type")) {
                "paragraph", "heading" -> {
                    if (result.isNotEmpty()) result += RichSpan("\n")
                    result += parseInline(node.optJSONArray("content"))
                }
                "bulletList", "orderedList", "blockquote", "listItem" -> {
                    if (result.isNotEmpty()) result += RichSpan("\n")
                    result += parseNestedInline(node.optJSONArray("content"))
                }
                "text" -> result += parseTextNode(node)
            }
        }
        return result
    }

    private fun parseInline(nodes: JSONArray?): List<RichSpan> {
        if (nodes == null) return emptyList()
        return buildList {
            for (index in 0 until nodes.length()) {
                val node = nodes.optJSONObject(index) ?: continue
                when (node.optString("type")) {
                    "text" -> add(parseTextNode(node))
                    "hardBreak" -> add(RichSpan("\n"))
                }
            }
        }
    }

    private fun parseTextNode(node: JSONObject): RichSpan {
        var bold = false
        var italic = false
        var underline = false
        var strike = false
        var link: String? = null
        val marks = node.optJSONArray("marks")
        if (marks != null) {
            for (index in 0 until marks.length()) {
                val mark = marks.optJSONObject(index) ?: continue
                when (mark.optString("type")) {
                    "bold" -> bold = true
                    "italic" -> italic = true
                    "underline" -> underline = true
                    "strike" -> strike = true
                    "link" -> {
                        val candidate = mark.optJSONObject("attrs")?.optString("href").orEmpty()
                        if (isSafeUrl(candidate)) link = candidate
                    }
                }
            }
        }
        return RichSpan(node.optString("text"), bold, italic, underline, strike, link)
    }

    private fun extractText(nodes: JSONArray?): String = parseInline(nodes).joinToString("") { it.text }

    fun isSafeUrl(value: String): Boolean {
        val trimmed = value.trim()
        return (trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true)) &&
            trimmed.none { it.code < 32 }
    }
}
