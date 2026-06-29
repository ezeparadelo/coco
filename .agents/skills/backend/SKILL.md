---
name: backend
description: Back-end data layer guidelines for Room DB schemas, entities, manual DI, JSON backup, and state observations in Coco.
---

# Coco Backend & Room Data Layer Custom Skill

This skill guides AI agents and developers implementing storage, schema migrations, and repository models in Coco.

## 🗄️ Database & Schema Invariants
- **NoteEntity**: Located in `com.coco.app.data.NoteEntity.kt`.
- Schema fields:
  - `id`: Long (primary key, auto-increment)
  - `content`: String (flat text captured by the user)
  - `createdAt` & `updatedAt`: Long (epoch timestamps)
  - `isPinned`: Boolean (pinned sorting status)
  - `remindAt`: Long? (reminder timestamp, null if none)
  - `colorIndex`: Int (range 0..3)
  - `isArchived`: Boolean (archive visibility status)
- **Mapping**: Always keep the database mapping clean. Use extension functions `NoteEntity.toDomain(): Note` and `Note.toEntity(): NoteEntity` to transfer models between Room tables and domain layers. Never expose raw entities to ViewModels.

---

## 🏗️ State Flow Observation & Threading
- Observe database tables through continuous Kotlin Flows.
- Active notes must be query-sorted by: `isPinned DESC, createdAt DESC`.
- Use the repository pattern to abstract database actions:
  ```kotlin
  val notes: StateFlow<List<Note>> = repository.observeNotes()
      .stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5000),
          initialValue = emptyList()
      )
  ```
- Write operations (insert, delete, update) must execute on dispatcher scopes (`Dispatchers.IO`) inside repository implementations, avoiding blockages on the main GUI thread.

---

## 🔌 Manual Dependency Injection
Do not integrate Hilt, Dagger, or Koin unless requested.
- Singleton database objects and repository configurations reside in `com.coco.app.CocoApplication.kt`.
- ViewModels receive dependencies via standard class constructors. Define ViewModel Factory instances manually inside `MainActivity.kt`:
  ```kotlin
  class HomeViewModelFactory(
      private val noteRepository: NoteRepository,
      private val settingsRepository: SettingsRepository
  ) : ViewModelProvider.Factory { ... }
  ```

---

## 💾 JSON Backup (BackupHelper)
- Backups are handled via `BackupHelper.kt` utilizing Gson/Kotlinx.Serialization.
- **Rules**:
  - The export JSON must be a clean, readable structure containing arrays of note models.
  - Do not export absolute system paths or raw database auto-increment keys that could cause primary key collisions on other devices.
  - Keep backup mechanisms compliant with Android Storage Access Framework (SAF) URI stream readers.
