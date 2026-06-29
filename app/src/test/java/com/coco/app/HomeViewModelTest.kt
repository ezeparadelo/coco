package com.coco.app

import app.cash.turbine.test
import com.coco.app.domain.Note
import com.coco.app.domain.NoteRepository
import com.coco.app.domain.SettingsRepository
import com.coco.app.ui.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeNoteRepository : NoteRepository {
        private val state = MutableStateFlow<List<Note>>(emptyList())
        override fun observeNotes(): Flow<List<Note>> = state
        override fun observeActiveNotes(): Flow<List<Note>> = state.map { list -> list.filter { !it.isArchived } }
        override fun observeArchivedNotes(): Flow<List<Note>> = state.map { list -> list.filter { it.isArchived } }
        override suspend fun add(content: String) {
            val trimmed = content.trim()
            if (trimmed.isEmpty()) return
            val now = System.currentTimeMillis()
            state.update { current -> current + Note(current.size + 1L, trimmed, now, now) }
        }
        override suspend fun updateContent(note: Note, newContent: String) {
            val trimmed = newContent.trim()
            if (trimmed.isEmpty()) return
            state.update { list -> list.map { if (it.id == note.id) it.copy(content = trimmed) else it } }
        }
        override suspend fun delete(note: Note) {
            state.update { list -> list.filterNot { it.id == note.id } }
        }
        override suspend fun setPinned(note: Note, pinned: Boolean) {
            state.update { list -> list.map { if (it.id == note.id) it.copy(isPinned = pinned) else it } }
        }
        override suspend fun setColor(note: Note, colorIndex: Int) {
            state.update { list -> list.map { if (it.id == note.id) it.copy(colorIndex = colorIndex) else it } }
        }
        override suspend fun setArchived(note: Note, archived: Boolean) {
            state.update { list -> list.map { if (it.id == note.id) it.copy(isArchived = archived) else it } }
        }
        override suspend fun setRemindAt(note: Note, remindAt: Long?) {
            state.update { list -> list.map { if (it.id == note.id) it.copy(remindAt = remindAt) else it } }
        }
        override fun observeDeletedNotes(): Flow<List<Note>> = MutableStateFlow(emptyList())
        override suspend fun restore(note: Note) {}
        override suspend fun deletePermanently(note: Note) {}
        override suspend fun emptyTrash() {}
        override suspend fun cleanOldTrash() {}
        override suspend fun insertAll(notes: List<Note>) {
            state.update { it + notes }
        }
        override suspend fun deleteAll() {
            state.value = emptyList()
        }
    }

    private class FakeSettings : SettingsRepository {
        private val state = MutableStateFlow(false)
        private val fastState = MutableStateFlow(false)
        private val onboardingState = MutableStateFlow(true)
        private val enterState = MutableStateFlow(true)
        override val startInHistory: StateFlow<Boolean> = state
        override val fastMode: StateFlow<Boolean> = fastState
        override val hasSeenOnboarding: StateFlow<Boolean> = onboardingState
        override val enterToSubmit: StateFlow<Boolean> = enterState
        override fun setStartInHistory(value: Boolean) {
            state.value = value
        }
        override fun setFastMode(value: Boolean) {
            fastState.value = value
        }
        override fun completeOnboarding() {
            onboardingState.value = true
        }
        override fun setEnterToSubmit(value: Boolean) {
            enterState.value = value
        }
    }

    @Test
    fun savingNoteEmitsItInNotesFlow() = runTest {
        val viewModel = HomeViewModel(FakeNoteRepository(), FakeSettings())
        viewModel.notes.test {
            assertEquals(emptyList<Note>(), awaitItem())
            viewModel.save("primera idea")
            assertEquals("primera idea", awaitItem().first().content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun blankNoteIsIgnored() = runTest {
        val viewModel = HomeViewModel(FakeNoteRepository(), FakeSettings())
        viewModel.notes.test {
            assertEquals(emptyList<Note>(), awaitItem())
            viewModel.save("   ")
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
