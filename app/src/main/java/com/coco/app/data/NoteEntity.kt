package com.coco.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.coco.app.domain.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val remindAt: Long? = null,
    val colorIndex: Int = 0,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)

fun NoteEntity.toDomain(): Note =
    Note(id, content, createdAt, updatedAt, isPinned, remindAt, colorIndex, isArchived, isDeleted, deletedAt)

fun Note.toEntity(): NoteEntity =
    NoteEntity(id, content, createdAt, updatedAt, isPinned, remindAt, colorIndex, isArchived, isDeleted, deletedAt)
