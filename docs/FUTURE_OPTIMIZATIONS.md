# Future optimizations

Performance opportunities found while fixing the arch (`CocoArch`) drag responsiveness.
They are intentionally **out of scope** for that change — each is small enough to merit
its own focused PR, or carries enough behavioral risk to be verified in isolation. This
document is the backlog so the analysis is not lost.

## 1. [COMPLETED] Arch wave repaint & resolution — `ui/components/CocoArch.kt`

*Implemented:*
- Lowered polyline resolution from `steps = 80` to `48` (~40% reduction in trig calculations per frame without visible loss of ripple quality).
- Precomputed X positions (`w * i / steps`) outside the `lineTo` call.

## 2. [COMPLETED] Markdown regex recompiled per keystroke — `util/Markdown.kt`

*Implemented:*
- Hoisted all four inline Markdown regex objects (`boldRegex`, `italicAsteriskRegex`, `italicUnderscoreRegex`, `codeRegex`) to file-level private `val` properties in `util/Markdown.kt`. They now compile only once when the class loaded.

## 3. [COMPLETED] Duplicated pull-to-search gesture logic — `ui/history/HistoryContent.kt`

*Implemented:*
- Consolidated pull-to-search gestures into shared state triggers (`triggerOpenSearch` / `triggerCloseSearch`).
- Removed `onPreScroll` accumulation so vertical drags are only evaluated once in `onPostScroll` or when touch happens on non-scrollable areas (`!listState.isScrollInProgress`), avoiding gesture conflicts and double work.
- Used `rememberUpdatedState` and `pointerInput(Unit)` so gesture connections never reallocate when typing search queries or filtering notes.

## 4. [COMPLETED] Offscreen GPU buffer elimination — `CaptureContent.kt` & `HistoryContent.kt`

*Implemented:*
- Updated `Modifier.fadingEdges` to support `fadeColor`. When passed `fadeColor = CocoCream` on `LazyColumn`, it draws a lightweight gradient rectangle in standard blending mode (`BlendMode.SrcOver`) instead of allocating an offscreen GPU texture (`CompositingStrategy.Offscreen`) of the entire viewport height on every scroll frame.

## 5. [COMPLETED] Zero-Recomposition Arch Dragging — `CaptureContent.kt`

*Implemented:*
- Replaced `visibleHeight` composable calculation with a custom `Modifier.layout` block so height updates occur purely during Phase 2 (Layout) without triggering Phase 1 (Composition).
- Replaced `showUpperLayers` and button alpha checks inside composable scope with `graphicsLayer` drawing and simple `isActive` checks.
- Memorized `visualTransformation = remember { MarkdownVisualTransformation() }`.

## 6. [COMPLETED] Precalculated Domain Properties & Continuous Wave Animation

*Implemented:*
- Converted `Note.hasLinks` from a computed getter (`get() = ...`) to a precalculated field initialized once per domain object.
- Configured `CocoArch` to keep playing the subtle wave animation continuously (`animateWave = !fastMode`) so it stays visually active and aesthetic while reading notes in history.
- Configured `enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(...))` in `MainActivity.kt` so status bar icons (battery, clock, notifications) always render dark and legible over the light `CocoCream` background.

## Analyzed — no action needed

- `HomeViewModel.notes`: the `combine` + `filter` pipeline is correct and cheap for the
  expected note counts.
- `HistoryContent` `LazyColumn`: already uses `key = { it.id }` and `animateItem()`.
- `NoteCard`: `renderMarkdown(note.content)` is already memoized with `remember(note.content)`.
