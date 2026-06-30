# Future optimizations

Performance opportunities found while fixing the arch (`CocoArch`) drag responsiveness.
They are intentionally **out of scope** for that change — each is small enough to merit
its own focused PR, or carries enough behavioral risk to be verified in isolation. This
document is the backlog so the analysis is not lost.

## 1. Arch wave repaint is permanent — `ui/components/CocoArch.kt`

The idle "wave" uses an `infiniteRepeatable` animation that mutates `phase` every frame.
Because `phase` is read inside `drawBehind`, the **full-width arch is re-drawn on every
frame, forever** — even when the user is idle and even while dragging (where it stacks on
top of the drag work).

Proposed work:
- Freeze the wave phase while the arch is being dragged/animated (pass an
  `isInteracting` / `animateWave` flag from the caller) and only run it at rest.
- Lower the polyline resolution from `steps = 80` to ~48 — the ripple amplitude
  (`w * 0.0065f`) makes the difference imperceptible.
- Pre-compute the X positions (`w * i / steps`), which are currently recomputed twice per
  point per frame inside `topY`.

## 2. Markdown regex recompiled per keystroke — `util/Markdown.kt`

`MarkdownVisualTransformation.filter()` compiles **four new `Regex` objects on every
call**, and it is called on each recomposition of the capture `BasicTextField` (i.e. on
every keystroke). Hoist the patterns to file-level `val`s — exactly like the existing
`inlinePattern` — so they compile once.

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
