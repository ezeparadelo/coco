# Coco — Codebase Map (Index)

This document maps the repository files, their key packages, and roles. Agents should refer to this map instead of running `list_dir` or `grep` queries repeatedly to discover file names and signatures.

## Directory Tree & Summary

```
C:\Users\ezequ\Documents\dev\coco
├── .agents/
│   ├── AGENTS.md                  # Development guidelines, system rules, and UI invariants
│   ├── CODEBASE.md                # This file (codebase index and package breakdown)
│   └── IDENTITY.md                # Brand philosophy, design tokens, and naming conventions
├── docs/
│   ├── FUTURE_OPTIMIZATIONS.md    # Potential future performance and UX optimizations
│   └── SPLASH_SCREEN_SPEC.md      # Dual-phase native -> Compose splash transition specification
├── app/
│   ├── build.gradle.kts           # App-level build config (Compose BOM, Room, compileSdk=35)
│   └── src/main/java/com/coco/app/
│       ├── CocoApplication.kt     # App subclass; initializes DB, SettingsStore, & Repositories (Manual DI)
│       ├── MainActivity.kt        # Entry Activity; handles window layout, ViewModel creation, & edge-to-edge
│       ├── data/                  # ROOM Local Persistence & Preferences Store
│       ├── domain/                # Pure Core Domain Interfaces & Entities
│       ├── ui/                    # Jetpack Compose UI Pages & Theme
│       ├── util/                  # Backup, time formatting, notifications, & markdown renderers
│       └── widget/                # Android home screen widgets & quick tile service
```

---

## Package Breakdown

### 1. Root Package (`com.coco.app`)
*   [CocoApplication.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/CocoApplication.kt) — Setup code. Exposes singleton `NoteRepository` and `SettingsRepository` for ViewModels.
*   [MainActivity.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/MainActivity.kt) — Hosts `HomeScreen`. Coordinates native splash condition (`installSplashScreen()`) with Compose readiness.

### 2. Core Domain (`com.coco.app.domain`)
*   [Note.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/domain/Note.kt) — Domain object representing a captured note (ID, content, timestamps, color index, pinned status, archive status).
*   [NoteRepository.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/domain/NoteRepository.kt) — Interface definitions for note persistence flows (`observeNotes()`, `add()`, `update()`, `delete()`, `setPinned()`, etc.).
*   [SettingsRepository.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/domain/SettingsRepository.kt) — Interface defining user preferences (e.g. `startInHistory`).

### 3. Local Persistence & Data (`com.coco.app.data`)
*   [CocoDatabase.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/data/CocoDatabase.kt) — Room database setup containing the `NoteDao` schema.
*   [NoteDao.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/data/NoteDao.kt) — SQLite CRUD operations for notes. Resolves sorting: `isPinned DESC, createdAt DESC` for active notes.
*   [NoteEntity.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/data/NoteEntity.kt) — Room table representation of a note. Declares extension functions `toDomain()` and `toEntity()`.
*   [RoomNoteRepository.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/data/RoomNoteRepository.kt) — Room database wrapper executing `NoteRepository` methods via Coroutines.
*   [SettingsStore.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/data/SettingsStore.kt) — DataStore wrapper implementing `SettingsRepository`. Reads preference values synchronously/asynchronously.

### 4. UI Layer (`com.coco.app.ui`)

#### UI Core and Theme (`ui/theme`)
*   [Color.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/theme/Color.kt) — Definition of "Coco Fresco" tokens (Cream `#FFFBF4`, Brown `#8C6A4F`, Green `#5E8C61`, etc.).
*   [Shape.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/shape/Shape.kt) — Rounded corner configuration and the cubic-bezier path shape for the animated arch (`CocoArchShape`).
*   [Theme.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/theme/Theme.kt) — Main Compose application theme wrapper.
*   [Type.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/theme/Type.kt) — Font typography using Outfitters/browser-safe styles.

#### UI Components (`ui/components`)
*   [CocoArch.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/components/CocoArch.kt) — Draws the signature brown bottom panel with a floating top wave animation (2 sine waves, 7s cycle).
*   [Neumorphic.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/components/Neumorphic.kt) — Extension modifiers: `Modifier.neumorphic` (creates a soft cream light/shadow background) and `Modifier.pressBounce` (adds scale click animation).
*   [NoteCard.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/components/NoteCard.kt) — Displays active/archived notes. Custom gestures (swipe-to-delete, swipe-to-archive), long click (pin/unpin), and single tap tools.
*   [CocoReminderDialog.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/components/CocoReminderDialog.kt) — Material3 date-time picker dialog to configure note reminders.

#### Views & Content Screens (`ui/home`, `ui/capture`, `ui/history`, `ui/settings`, `ui/onboarding`)
*   [HomeScreen.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/home/HomeScreen.kt) — Outer screen orchestrating vertical drag sheet (`offsetY`), splash animation, onboarding overlay, and main views.
*   [HomeViewModel.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/home/HomeViewModel.kt) — Hosts all data streams (observeNotes, observeArchivedNotes) and coordinates database actions.
*   [CaptureContent.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/capture/CaptureContent.kt) — Text input layout inside the brown arch. Employs `FocusRequester` for keyboard display.
*   [HistoryContent.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/history/HistoryContent.kt) — Chronic list of notes with search bar (down-drag activates, up-drag closes).
*   [SettingsDialog.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/settings/SettingsDialog.kt) — Overlay with checkboxes (startInHistory, fastMode) and JSON Backup Export/Import.
*   [OnboardingDialog.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/ui/onboarding/OnboardingDialog.kt) — In-line onboarding card overlay showing basic gestures to first-time users.

### 5. Utility & Widgets (`com.coco.app.util`, `com.coco.app.widget`)
*   [BackupHelper.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/util/BackupHelper.kt) — Handles JSON import/export of notes via standard stream readers.
*   [Markdown.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/util/Markdown.kt) — Parser that renders `*bold*` or `_italic_` text using Jetpack Compose annotated strings.
*   [NotificationHelper.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/util/NotificationHelper.kt) — Emits alarm notifications for note reminders via a BroadcastReceiver.
*   [Time.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/util/Time.kt) — Converts timestamps to relative representations (e.g. "hace 5 min").
*   [CocoCaptureWidgetReceiver.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/widget/CocoCaptureWidgetReceiver.kt) — Home screen widget that launches the capture state.
*   [CocoQuickTileService.kt](file:///C:/Users/ezequ/Documents/dev/coco/app/src/main/java/com/coco/app/widget/CocoQuickTileService.kt) — Android Quick Settings panel tile linking to note capture.
