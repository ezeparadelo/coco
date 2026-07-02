# Coco — Development Guide

## What it is
Native Android app for **ultra-fast note capturing**. Philosophy: zero friction between
"I have a thought" and "it's saved". Opening the app = keyboard already open, type and you're done.

## Stack
- **Kotlin 2.0.21** + **Jetpack Compose** (Material3, Compose BOM 2024.10.01)
- **Room 2.6.1** (KSP) for local persistence
- **Coroutines + Flow**, **ViewModel** (MVVM + unidirectional data flow)
- **Manual DI** via `CocoApplication` (NO Hilt for now)
- **AGP 8.6.1 · Gradle 8.7 · JDK 17 (toolchain)** · `compileSdk`/`targetSdk` 35 · `minSdk` 26

## Commands
```bash
./gradlew :app:assembleDebug          # build debug APK
./gradlew :app:installDebug           # install on device/emulator
./gradlew :app:testDebugUnitTest      # unit tests (JVM)
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (requires device)
./gradlew :app:lintDebug              # lint
```
In PowerShell use `./gradlew.bat ...`. Ensure `JAVA_HOME` points to a JDK 17+ (here: Temurin 21).
The SDK path is read from `local.properties` (`sdk.dir`, do not commit).

## Architecture (single module `:app`, package `com.coco.app`)
```
data/        NoteEntity (+toDomain/toEntity), NoteDao, CocoDatabase, RoomNoteRepository, SettingsStore
domain/      Note, NoteRepository (interface), SettingsRepository (interface)
ui/theme/    Color, Theme, Type, Shape (CocoArchShape)
ui/components/ Neumorphic (Modifier.neumorphic / pressBounce), CocoArch (animated arch), NoteCard
ui/home/     HomeScreen (vertical drag with Animatable + progress + WritePill), HomeViewModel
ui/capture/  CaptureContent (BasicTextField inside arch + auto keyboard + circular SaveButton)
ui/history/  HistoryContent (chronological list + animated search bar + BackHandler + empty state)
ui/settings/ SettingsDialog (custom Dialog with switch + Export/Import backup buttons)
util/        Time (relativeTime), Markdown (renderMarkdown), NotificationHelper (ReminderReceiver), BackupHelper
CocoApplication.kt  Manual DI (lazy DB + repository + settingsStore)
MainActivity.kt     Immediate splash, edge-to-edge, manual ViewModel factory, Compose host
```

## UX Invariants (DO NOT break)
1. **Animated Splash Screen (1200ms)**: When opening the app, a brown circle (`#5E4130`, 170dp) descends from the center to the bottom and fades out, while the arch rises smoothly and progressively from below (30-100%). Seamless handoff with `AtomicBoolean` in `MainActivity.kt`. See `docs/SPLASH_SCREEN_SPEC.md` for technical details.
   - **startInHistory=true**: The arch animates from below up to `historyOffset` (peeking, ready to view notes).
   - **startInHistory=false**: The arch animates from below up to `normalOffset` (writing position).
   - **fastMode=true**: Skips all animations, displays the UI immediately.
2. **On Launch**: Focus on `BasicTextField` + keyboard visible, zero delay. The splash dismisses
   instantly (`installSplashScreen().setKeepOnScreenCondition { false }`).
   The keyboard is handled by `CaptureContent` (`FocusRequester` + `SoftwareKeyboardController`,
   reacting to `isActive`; if not active, `clearFocus(force=true)` + `hide()`).
   Manifest: `windowSoftInputMode="adjustResize"`.
3. The **brown arch** lives in the bottom half, above the keyboard (`imePadding()`). The header
   ("Coco" + subtitle + pull tab) occupies the top half and is draggable.
4. **Vertical Drag and Continuity**: Dragging the header or history header slides the capture layer (`translationY = offsetY`) and controls `progress` (0→1). The `dragModifier` must remain constant without being dynamically removed mid-gesture to ensure `onDragStopped` always executes the final snap. The snap threshold uses a velocity >600px/s for maximum tactile responsiveness. Revealing history triggers haptic feedback.
5. **On Save** (Done keyboard action or circular green `SaveButton`): `onSave(text)` →
   repository persists to Room, local input clears (`rememberSaveable`), haptic feedback triggers.
6. **"Start in history" setting** (`SettingsStore`, read synchronously via `StateFlow` to prevent
   flash): if active, `offsetY` starts at `heightPx`; returning to capture is done by tapping or
   **dragging upward** on the `WritePill` button ("Write"). This button uses its own `pointerInput`
   with separate `detectTapGestures` + `detectVerticalDragGestures` (tap outside + drag
   inside; DO NOT combine `clickable`+`draggable`, as they conflict).
7. **History Gestures & Cards**:
   - Swipe left deletes the note (red background with trash icon).
   - Swipe right archives the note (brown background with 📦 icon).
   - Single tap expands toolbar to: edit text, change card color (`colorIndex` 0 to 3), or schedule reminder.
   - Long-press on a card pins/unpins it (`combinedClickable` + `onLongClick` with haptics).
8. **Edit Mode and Dragging**: If a note is in edit mode (`editingNote != null`) in the capture layer and the user drags downward to return to history, the input field clears automatically and the edit cancels to prevent visual bugs.
9. **Navigation and BackHandler**: In the history view, `BackHandler` intercepts the device Back button to return from archived notes to active notes, or to close the expandable search bar, preventing accidental app exit.
10. **Runtime Permissions & Notifications**: The notifications and reminders system is temporarily hidden from the UI for visual simplicity, preserving the underlying logic.
11. **Search via Swipe Down & Swipe Up**: In `HistoryContent`, when at the very top, dragging vertically downward (> 60px) opens the animated search bar. With search open, dragging upward (< -40px) closes search and hides the keyboard.
12. **Color Transitions and 4-Tone Palette**: When changing color on `NoteCard`, the surface transitions smoothly with `animateColorAsState`. The palette consists of 4 pastel tones (no cream): Warm Sand (default index 0), Coco Green, Lavender, and Nordic Blue.
13. **Undo on Delete**: When deleting a note via swipe, a floating neumorphic pill appears at the bottom for 3.5 seconds with the option to restore the note intact.
14. **"No animations" setting (`fastMode`)**: If enabled in settings, startup animations, UI bounce physics, card color animations, and the continuous arch wave (`CocoArch`) are skipped for maximum speed and GPU savings.

## "Fresh Coco" Design System
Tokens in `ui/theme/Color.kt`:
| Token | Hex | Role |
|---|---|---|
| `CocoCream` | `#FFFBF4` | background / surface |
| `CocoBrown` | `#8C6A4F` | arch (primary) |
| `CocoBrownDark` | `#3A2A1D` | details / strong text |
| `CocoGreen` | `#5E8C61` | accent / active state / save button |
| `CocoInk` | `#2E2018` | text on cream |
| `CocoOnBrown` | `#FFFBF4` | text on brown |
| `NeoLight` | `#FFFFFF` | light highlight (neumorphism) |
| `NeoShadow` | `#ECE0CC` | shadow (neumorphism) |

The **arch** uses a dark gradient `CocoArchTop` (`#5E4130`) → `CocoArchBottom` (`#3E2819`) with soft shadow
(`CocoBrownDark` 14% alpha) and a top border featuring a subtle **idle wave** (drawn in `CocoArch`
with `drawBehind` + phase animated by `rememberInfiniteTransition`, 7s cycle, 2 sine waves).

Rules:
- **No hard straight lines**: everything rounded, gummy, organic (`CocoArchShape` with cubic bezier,
  `CocoShapes` from 12dp to 40dp).
- Surfaces feature **soft neumorphism** (`Modifier.neumorphic`: shadow + background + border).
  Tactile feedback with 0.94x scale + spring (`Modifier.pressBounce` with `MutableInteractionSource`)
  and **haptic feedback** when saving / pinning / revealing history.
- Animations use **spring physics**, no linear/abrupt curves.
- Clean and minimalist buttons: text without visible technical extensions (e.g., say "Export notes" instead of "Export notes (.json)").

## Data Model (future-proof)
`NoteEntity`: `id`, `content` (plain text), `createdAt`, `updatedAt`, `isPinned=false`, `remindAt: Long?=null`, `colorIndex: Int=0`, `isArchived: Boolean=false`.
Extension functions `toDomain()` / `toEntity()` to map between Entity ↔ Note (domain).
- **Markdown** (implemented): `renderMarkdown` only renders bold/italic; `content` is always plain text.
- **Pin & Archive** (implemented): active notes ordered by `isPinned DESC, createdAt DESC`.
- **JSON Backup**: `BackupHelper` serializes/deserializes the note list into clean JSON format compatible with Storage Access Framework (SAF).

## Repository API
```kotlin
interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    fun observeArchivedNotes(): Flow<List<Note>>
    suspend fun add(content: String)
    suspend fun updateContent(note: Note, newContent: String)
    suspend fun delete(note: Note)
    suspend fun setPinned(note: Note, pinned: Boolean)
    suspend fun setArchived(note: Note, archived: Boolean)
    suspend fun updateColor(note: Note, colorIndex: Int)
    suspend fun setRemindAt(note: Note, remindAt: Long?)
    suspend fun insertAll(notes: List<Note>)
}

interface SettingsRepository {
    val startInHistory: StateFlow<Boolean>
    fun setStartInHistory(value: Boolean)
}
```

## Conventions
- Stateless composables whenever possible (state hoisting to ViewModel).
- A single `StateFlow` as the sole source of truth for the notes list (`observeNotes()` →
  `stateIn(WhileSubscribed(5_000))`).
- The ViewModel DOES NOT extend `AndroidViewModel`: receives `NoteRepository` + `SettingsRepository`
  via constructor, with a manual `companion object factory(...)`.
- Test ViewModel (fake repo) and DAO (in-memory Room); use `@Preview` to validate components.
- Capture text lives in `rememberSaveable` inside `CaptureContent` (NOT in ViewModel),
  because it is ephemeral UI state that survives config changes but doesn't need database persistence.
