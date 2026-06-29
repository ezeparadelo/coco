package com.coco.app.data

import com.coco.app.domain.Note
import com.coco.app.domain.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNoteRepository(private val dao: NoteDao) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeNotes().map { entities -> entities.map { it.toDomain() } }

    override fun observeActiveNotes(): Flow<List<Note>> =
        dao.observeActiveNotes().map { entities -> entities.map { it.toDomain() } }

    override fun observeArchivedNotes(): Flow<List<Note>> =
        dao.observeArchivedNotes().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        val now = System.currentTimeMillis()
        dao.insert(NoteEntity(content = trimmed, createdAt = now, updatedAt = now))
    }

    override suspend fun updateContent(note: Note, newContent: String) {
        val trimmed = newContent.trim()
        if (trimmed.isEmpty()) return
        dao.update(note.copy(content = trimmed, updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun delete(note: Note) {
        dao.updateDeleted(note.id, true, System.currentTimeMillis())
    }

    override suspend fun setPinned(note: Note, pinned: Boolean) {
        dao.update(note.copy(isPinned = pinned, updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun setColor(note: Note, colorIndex: Int) {
        dao.updateColor(note.id, colorIndex)
    }

    override suspend fun setArchived(note: Note, archived: Boolean) {
        dao.updateArchived(note.id, archived)
    }

    override suspend fun setRemindAt(note: Note, remindAt: Long?) {
        dao.updateRemindAt(note.id, remindAt)
    }

    override fun observeDeletedNotes(): Flow<List<Note>> =
        dao.observeDeletedNotes().map { entities -> entities.map { it.toDomain() } }

    override suspend fun restore(note: Note) {
        dao.updateDeleted(note.id, false, null)
    }

    override suspend fun deletePermanently(note: Note) {
        dao.delete(note.toEntity())
    }

    override suspend fun emptyTrash() {
        dao.emptyTrash()
    }

    override suspend fun cleanOldTrash() {
        val thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L
        val threshold = System.currentTimeMillis() - thirtyDaysInMillis
        dao.cleanOldTrash(threshold)
    }

    override suspend fun insertAll(notes: List<Note>) {
        dao.insertAll(notes.map { it.toEntity() })
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
