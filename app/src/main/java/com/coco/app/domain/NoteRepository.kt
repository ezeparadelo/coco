package com.coco.app.domain

import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a notas. Es una interfaz para poder inyectar fakes en tests.
 */
interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    fun observeActiveNotes(): Flow<List<Note>>
    fun observeArchivedNotes(): Flow<List<Note>>
    suspend fun add(content: String)
    suspend fun updateContent(note: Note, newContent: String)
    suspend fun delete(note: Note)
    suspend fun setPinned(note: Note, pinned: Boolean)
    suspend fun setColor(note: Note, colorIndex: Int)
    suspend fun setArchived(note: Note, archived: Boolean)
    suspend fun setRemindAt(note: Note, remindAt: Long?)
    fun observeDeletedNotes(): Flow<List<Note>>
    suspend fun restore(note: Note)
    suspend fun deletePermanently(note: Note)
    suspend fun emptyTrash()
    suspend fun cleanOldTrash()
    suspend fun insertAll(notes: List<Note>)
    suspend fun deleteAll()
}
