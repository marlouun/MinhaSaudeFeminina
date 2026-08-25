package com.example.minhasaudefeminina.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.minhasaudefeminina.domain.content.ArticleBlock
import com.example.minhasaudefeminina.domain.content.ArticleDocumentParser
import com.example.minhasaudefeminina.domain.content.RichSpan
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario

@Composable
fun ArticleContentRenderer(contentJson: String, modifier: Modifier = Modifier) {
    val blocks = remember(contentJson) { ArticleDocumentParser.parse(contentJson) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { block ->
            when (block) {
                is ArticleBlock.Paragraph -> RichText(block.content, MaterialTheme.typography.bodyLarge)
                is ArticleBlock.Heading -> RichText(
                    block.content,
                    when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    }.copy(color = RosaPrimario, fontWeight = FontWeight.Bold)
                )
                is ArticleBlock.BulletList -> ListBlock(block.items, ordered = false)
                is ArticleBlock.OrderedList -> ListBlock(block.items, ordered = true)
                is ArticleBlock.Quote -> Surface(
                    color = RosaClaro.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RichText(
                        block.content,
                        MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        Modifier.padding(16.dp)
                    )
                }
                is ArticleBlock.Code -> Surface(
                    color = Color(0xFFF1F1F1),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = block.text,
                        modifier = Modifier.padding(14.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                is ArticleBlock.Image -> AsyncImage(
                    model = block.source,
                    contentDescription = block.alternativeText.ifBlank { "Imagem do artigo" },
                    modifier = Modifier.fillMaxWidth()
                )
                ArticleBlock.Divider -> HorizontalDivider(color = RosaClaro)
            }
        }
    }
}

@Composable
private fun ListBlock(items: List<List<RichSpan>>, ordered: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (ordered) "${index + 1}." else "•",
                    color = RosaSecundario,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp)
                )
                RichText(item, MaterialTheme.typography.bodyLarge, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RichText(
    spans: List<RichSpan>,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val annotated = remember(spans) { buildRichAnnotatedString(spans) }
    ClickableText(
        text = annotated,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = LINK_TAG, start = offset, end = offset)
                .firstOrNull()
                ?.item
                ?.let { url -> runCatching { uriHandler.openUri(url) } }
        }
    )
}

private fun buildRichAnnotatedString(spans: List<RichSpan>): AnnotatedString = buildAnnotatedString {
    spans.forEach { span ->
        val decorations = buildList {
            if (span.underline) add(TextDecoration.Underline)
            if (span.strike) add(TextDecoration.LineThrough)
        }
        span.link?.let { pushStringAnnotation(LINK_TAG, it) }
        pushStyle(
            SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
                textDecoration = decorations.takeIf { it.isNotEmpty() }?.let(TextDecoration::combine),
                color = if (span.link != null) RosaSecundario else Color.Unspecified
            )
        )
        append(span.text)
        pop()
        if (span.link != null) pop()
    }
}

private const val LINK_TAG = "article_link"
