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

## 3. Duplicated pull-to-search gesture logic — `ui/history/HistoryContent.kt`

The "pull down to open search / push up to close" logic is implemented twice: once in the
`NestedScrollConnection` (`onPreScroll` + `onPostScroll`) and again in a
`detectVerticalDragGestures` `pointerInput`. They can fight each other and double the work
per gesture. Consolidate into a single source of truth.

## Analyzed — no action needed

- `HomeViewModel.notes`: the `combine` + `filter` pipeline is correct and cheap for the
  expected note counts.
- `HistoryContent` `LazyColumn`: already uses `key = { it.id }` and `animateItem()`.
- `NoteCard`: `renderMarkdown(note.content)` is already memoized with `remember(note.content)`.
</content>
