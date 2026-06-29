package com.coco.app.domain

import androidx.compose.runtime.Immutable

/**
 * Modelo de dominio de una nota.
 *
 * Incluye [isPinned], [remindAt], [colorIndex] (0..3 para etiquetas pastel) e [isArchived].
 * [content] es texto plano → el render Markdown es presentación.
 */
@Immutable
data class Note(
    val id: Long,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val remindAt: Long? = null,
    val colorIndex: Int = 0,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
) {
    val hasLinks: Boolean
        get() = WEB_URL_REGEX.containsMatchIn(content)

    companion object {
        private val WEB_URL_REGEX = Regex("(?i)(https?://|www\\.)\\S+")
    }
}
