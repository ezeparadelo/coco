package com.coco.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping

// Tokens inline, en orden de prioridad: **negrita**, `código`, *cursiva*, _cursiva_.
private val inlinePattern = Regex("""\*\*(.+?)\*\*|`([^`]+?)`|\*(.+?)\*|_(.+?)_""")

/**
 * Render Markdown **básico** a [AnnotatedString]:
 * `# ` y `## ` encabezados, `- `/`* ` viñetas, `**negrita**`, `*cursiva*`/`_cursiva_`, `` `código` ``.
 * El texto se guarda siempre como plano; esto es solo presentación.
 */
fun renderMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val lines = text.split("\n")
    lines.forEachIndexed { index, raw ->
        when {
            raw.startsWith("# ") ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp)) {
                    appendInline(raw.removePrefix("# "))
                }
            raw.startsWith("## ") ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                    appendInline(raw.removePrefix("## "))
                }
            raw.startsWith("- ") || raw.startsWith("* ") -> {
                append("•  ")
                appendInline(raw.drop(2))
            }
            else -> appendInline(raw)
        }
        if (index != lines.lastIndex) append("\n")
    }
}

private fun AnnotatedString.Builder.appendInline(text: String) {
    var last = 0
    for (match in inlinePattern.findAll(text)) {
        if (match.range.first > last) append(text.substring(last, match.range.first))
        val groups = match.groups
        when {
            groups[1] != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(groups[1]!!.value) }
            groups[2] != null -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(groups[2]!!.value) }
            groups[3] != null -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(groups[3]!!.value) }
            groups[4] != null -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(groups[4]!!.value) }
        }
        last = match.range.last + 1
    }
    if (last < text.length) append(text.substring(last))
}

private val boldRegex = Regex("""\*\*(.+?)\*\*""")
private val italicAsteriskRegex = Regex("""(?<!\*)\*([^*]+?)\*(?!\*)""")
private val italicUnderscoreRegex = Regex("""_([^_]+?)_""")
private val codeRegex = Regex("""`([^`]+?)`""")

/**
 * VisualTransformation que aplica estilos Markdown al texto plano durante la edición,
 * manteniendo visibles los símbolos de marcado (asteriscos, guiones, etc.) sin alterar
 * la longitud del texto. Esto evita problemas de offset del cursor.
 */
class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val annotated = buildAnnotatedString {
            append(raw)
            
            // 1. Bold: **text**
            boldRegex.findAll(raw).forEach { match ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
            
            // 2. Italic: *text* (evitando doble asterisco)
            italicAsteriskRegex.findAll(raw).forEach { match ->
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
            }
            
            // 3. Italic: _text_
            italicUnderscoreRegex.findAll(raw).forEach { match ->
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
            }
            
            // 4. Code: `text`
            codeRegex.findAll(raw).forEach { match ->
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace), match.range.first, match.range.last + 1)
            }
            
            // 5. Encabezados y viñetas por línea
            val lines = raw.split("\n")
            var currentOffset = 0
            lines.forEach { line ->
                when {
                    line.startsWith("# ") -> {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp), currentOffset, currentOffset + line.length)
                    }
                    line.startsWith("## ") -> {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp), currentOffset, currentOffset + line.length)
                    }
                    line.startsWith("- ") || line.startsWith("* ") -> {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), currentOffset, currentOffset + 2)
                    }
                }
                currentOffset += line.length + 1
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
