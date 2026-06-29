package com.coco.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.coco.app.domain.Note
import com.coco.app.domain.NoteRepository
import com.coco.app.domain.SettingsRepository
import com.coco.app.util.BackupHelper
import com.coco.app.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryViewMode {
    ACTIVE, ARCHIVED, TRASH
}

class HomeViewModel(
    private val repository: NoteRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedColorFilters = MutableStateFlow<Set<Int>>(emptySet())
    val selectedColorFilters: StateFlow<Set<Int>> = _selectedColorFilters.asStateFlow()

    private val _filterWithLinksOnly = MutableStateFlow(false)
    val filterWithLinksOnly: StateFlow<Boolean> = _filterWithLinksOnly.asStateFlow()

    private val _sharedText = MutableStateFlow<String?>(null)
    val sharedText: StateFlow<String?> = _sharedText.asStateFlow()

    private val _quickCaptureTrigger = MutableStateFlow(false)
    val quickCaptureTrigger: StateFlow<Boolean> = _quickCaptureTrigger.asStateFlow()

    private val _viewMode = MutableStateFlow(HistoryViewMode.ACTIVE)
    val viewMode: StateFlow<HistoryViewMode> = _viewMode.asStateFlow()

    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()

    private val activeNotes: StateFlow<List<Note>> = repository.observeActiveNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeCount: StateFlow<Int> = activeNotes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val archivedNotes: StateFlow<List<Note>> = repository.observeArchivedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val archivedCount: StateFlow<Int> = archivedNotes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val deletedNotes: StateFlow<List<Note>> = repository.observeDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val deletedCount: StateFlow<Int> = deletedNotes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _easterEggTaps = MutableStateFlow(0)
    val easterEggTaps: StateFlow<Int> = _easterEggTaps.asStateFlow()

    fun incrementEasterEggTaps() {
        _easterEggTaps.value += 1
    }

    init {
        viewModelScope.launch {
            repository.cleanOldTrash()
        }
    }

    private val _filterState = combine(
        _viewMode,
        _searchQuery,
        _selectedColorFilters,
        _filterWithLinksOnly
    ) { mode, query, colors, linksOnly ->
        FilterParams(mode, query, colors, linksOnly)
    }

    private data class FilterParams(
        val viewMode: HistoryViewMode,
        val searchQuery: String,
        val selectedColors: Set<Int>,
        val linksOnly: Boolean
    )

    val notes: StateFlow<List<Note>> = combine(
        activeNotes,
        archivedNotes,
        deletedNotes,
        _filterState
    ) { active, archived, deleted, params ->
        val targetList = when (params.viewMode) {
            HistoryViewMode.ACTIVE -> active
            HistoryViewMode.ARCHIVED -> archived
            HistoryViewMode.TRASH -> deleted
        }
        targetList.filter { note ->
            val matchesQuery = params.searchQuery.isBlank() || note.content.contains(params.searchQuery, ignoreCase = true)
            val matchesColor = params.selectedColors.isEmpty() || params.selectedColors.contains(note.colorIndex)
            val matchesLinks = !params.linksOnly || note.hasLinks
            matchesQuery && matchesColor && matchesLinks
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val startInHistory: StateFlow<Boolean> = settings.startInHistory
    val fastMode: StateFlow<Boolean> = settings.fastMode
    val hasSeenOnboarding: StateFlow<Boolean> = settings.hasSeenOnboarding
    val enterToSubmit: StateFlow<Boolean> = settings.enterToSubmit

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleColorFilter(colorIndex: Int) {
        val current = _selectedColorFilters.value
        _selectedColorFilters.value = if (current.contains(colorIndex)) current - colorIndex else current + colorIndex
    }

    fun toggleLinkFilter() {
        _filterWithLinksOnly.value = !_filterWithLinksOnly.value
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedColorFilters.value = emptySet()
        _filterWithLinksOnly.value = false
    }

    fun onSharedTextReceived(text: String?) {
        if (!text.isNullOrBlank()) {
            _sharedText.value = text
        }
    }

    fun consumeSharedText() {
        _sharedText.value = null
    }

    fun triggerQuickCapture() {
        _quickCaptureTrigger.value = true
    }

    fun consumeQuickCapture() {
        _quickCaptureTrigger.value = false
    }

    fun setViewMode(mode: HistoryViewMode) {
        _viewMode.value = mode
    }

    fun setShowArchived(show: Boolean) {
        _viewMode.value = if (show) HistoryViewMode.ARCHIVED else HistoryViewMode.ACTIVE
    }

    fun startEditing(note: Note) {
        _editingNote.value = note
    }

    fun cancelEditing() {
        _editingNote.value = null
    }

    fun save(text: String) {
        val currentEditing = _editingNote.value
        viewModelScope.launch {
            if (currentEditing != null) {
                repository.updateContent(currentEditing, text)
                _editingNote.value = null
            } else {
                repository.add(text)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.delete(note) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { repository.setPinned(note, !note.isPinned) }
    }

    fun updateColor(note: Note, colorIndex: Int) {
        viewModelScope.launch { repository.setColor(note, colorIndex) }
    }

    fun archiveNote(note: Note, archived: Boolean) {
        viewModelScope.launch { repository.setArchived(note, archived) }
    }

    fun undoDelete(note: Note) {
        viewModelScope.launch { repository.restore(note) }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch { repository.restore(note) }
    }

    fun deletePermanently(note: Note) {
        viewModelScope.launch { repository.deletePermanently(note) }
    }

    fun emptyTrash() {
        viewModelScope.launch { repository.emptyTrash() }
    }

    fun scheduleReminder(context: Context, note: Note, triggerAtMillis: Long?) {
        if (triggerAtMillis == null) {
            NotificationHelper.cancelReminder(context, note.id)
            viewModelScope.launch { repository.setRemindAt(note, null) }
        } else {
            NotificationHelper.scheduleReminder(context, note.id, note.content, triggerAtMillis)
            viewModelScope.launch { repository.setRemindAt(note, triggerAtMillis) }
        }
    }

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val notesList = repository.observeNotes().first()
            onExported(BackupHelper.exportToJson(notesList))
        }
    }

    fun importBackup(jsonString: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val importedNotes = BackupHelper.importFromJson(jsonString)
            if (importedNotes.isNotEmpty()) {
                repository.deleteAll()
                repository.insertAll(importedNotes)
            }
            onComplete()
        }
    }

    fun setStartInHistory(value: Boolean) {
        settings.setStartInHistory(value)
    }

    fun setFastMode(value: Boolean) {
        settings.setFastMode(value)
    }

    fun setEnterToSubmit(value: Boolean) {
        settings.setEnterToSubmit(value)
    }

    fun completeOnboarding() {
        settings.completeOnboarding()
    }

    companion object {
        fun factory(repository: NoteRepository, settings: SettingsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HomeViewModel(repository, settings) }
            }
    }
}
