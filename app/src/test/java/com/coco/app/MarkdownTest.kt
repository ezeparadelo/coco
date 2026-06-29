package com.coco.app

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.coco.app.util.renderMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    @Test
    fun stripsInlineMarkers() {
        assertEquals("hola mundo", renderMarkdown("**hola** mundo").text)
        assertEquals("texto it y code", renderMarkdown("texto *it* y `code`").text)
        assertEquals("cursiva", renderMarkdown("_cursiva_").text)
    }

    @Test
    fun stripsBlockMarkers() {
        assertEquals("Titulo", renderMarkdown("# Titulo").text)
        assertEquals("Subtitulo", renderMarkdown("## Subtitulo").text)
        assertEquals("•  uno\n•  dos", renderMarkdown("- uno\n* dos").text)
    }

    @Test
    fun appliesStyles() {
        val bold = renderMarkdown("**x**")
        assertEquals("x", bold.text)
        assertTrue(bold.spanStyles.any { it.item.fontWeight == FontWeight.Bold })

        val italic = renderMarkdown("*x*")
        assertTrue(italic.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    }
}
