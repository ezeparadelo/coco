package com.coco.app.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.coco.app.R
import com.coco.app.domain.Note
import com.coco.app.ui.home.HistoryViewMode
import com.coco.app.ui.components.NoteCard
import com.coco.app.ui.components.neumorphic
import com.coco.app.ui.capture.fadingEdges
import androidx.compose.foundation.border
import com.coco.app.ui.theme.CocoBrown
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoCream
import com.coco.app.ui.theme.CocoGreen
import com.coco.app.ui.theme.CocoInk
import com.coco.app.ui.theme.CocoOnBrown
import com.coco.app.ui.theme.NotePalette
import kotlinx.coroutines.delay

@Composable
fun HistoryContent(
    notes: List<Note>,
    alphaProvider: () -> Float = { 1f },
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: HistoryViewMode,
    onSelectViewMode: (HistoryViewMode) -> Unit,
    archivedCount: Int,
    activeCount: Int,
    deletedCount: Int,
    onRestoreNote: (Note) -> Unit = {},
    onPermanentDeleteNote: (Note) -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    onTogglePin: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onUndoDeleteNote: (Note) -> Unit,
    onArchiveNote: (Note) -> Unit,
    onEditNote: (Note) -> Unit,
    onSelectColor: (Note, Int) -> Unit,
    onScheduleReminder: (Note, Long?) -> Unit,
    selectedColorFilters: Set<Int> = emptySet(),
    onToggleColorFilter: (Int) -> Unit = {},
    filterWithLinksOnly: Boolean = false,
    onToggleLinkFilter: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    onOpenSettings: () -> Unit,
    dragModifier: Modifier,
    fastMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val anyFilterActive = searchQuery.isNotEmpty() || selectedColorFilters.isNotEmpty() || filterWithLinksOnly
    var isSearchOpen by rememberSaveable { mutableStateOf(anyFilterActive) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var accumulatedPull by remember { mutableStateOf(0f) }
    var accumulatedUpPush by remember { mutableStateOf(0f) }
    var recentlyDeletedNote by remember { mutableStateOf<Note?>(null) }
    var lastDeletedNoteForUI by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var noteToDeletePermanently by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(recentlyDeletedNote) {
        if (recentlyDeletedNote != null) {
            lastDeletedNoteForUI = recentlyDeletedNote
            delay(3500)
            recentlyDeletedNote = null
        }
    }

    val nestedScrollConnection = remember(searchQuery, notes) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isSearchOpen && (searchQuery.isEmpty() || notes.isEmpty())) {
                    if (available.y < 0) {
                        accumulatedUpPush += available.y
                        if (accumulatedUpPush < -40f) {
                            onClearFilters()
                            isSearchOpen = false
                            keyboard?.hide()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            accumulatedUpPush = 0f
                        }
                    } else if (available.y > 0) {
                        accumulatedUpPush = 0f
                    }
                } else if (!isSearchOpen && !listState.canScrollBackward) {
                    if (available.y > 0) {
                        accumulatedPull += available.y
                        if (accumulatedPull > 50f) {
                            isSearchOpen = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            accumulatedPull = 0f
                        }
                    } else if (available.y < 0) {
                        accumulatedPull = 0f
                    }
                } else {
                    accumulatedPull = 0f
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isSearchOpen && (searchQuery.isEmpty() || notes.isEmpty())) {
                    if (available.y < 0) {
                        accumulatedUpPush += available.y
                        if (accumulatedUpPush < -40f) {
                            onClearFilters()
                            isSearchOpen = false
                            keyboard?.hide()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            accumulatedUpPush = 0f
                        }
                    }
                } else if (!isSearchOpen && !listState.canScrollBackward) {
                    if (available.y > 0) {
                        accumulatedPull += available.y
                        if (accumulatedPull > 50f) {
                            isSearchOpen = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            accumulatedPull = 0f
                        }
                    }
                }
                return Offset.Zero
            }
        }
    }

    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text(stringResource(R.string.empty_trash_confirm_title), color = CocoBrownDark) },
            text = { Text(stringResource(R.string.empty_trash_confirm_desc), color = CocoInk) },
            confirmButton = {
                TextButton(onClick = {
                    onEmptyTrash()
                    showEmptyTrashConfirm = false
                }) {
                    Text(stringResource(R.string.delete_permanently), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) {
                    Text(stringResource(android.R.string.cancel), color = CocoBrownDark)
                }
            },
            containerColor = CocoCream
        )
    }

    if (noteToDeletePermanently != null) {
        val noteToDel = noteToDeletePermanently!!
        AlertDialog(
            onDismissRequest = { noteToDeletePermanently = null },
            title = { Text(stringResource(R.string.delete_note_confirm_title), color = CocoBrownDark) },
            text = { Text(stringResource(R.string.delete_note_confirm_desc), color = CocoInk) },
            confirmButton = {
                TextButton(onClick = {
                    onPermanentDeleteNote(noteToDel)
                    noteToDeletePermanently = null
                }) {
                    Text(stringResource(R.string.delete_permanently), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDeletePermanently = null }) {
                    Text(stringResource(android.R.string.cancel), color = CocoBrownDark)
                }
            },
            containerColor = CocoCream
        )
    }

    BackHandler(enabled = viewMode != HistoryViewMode.ACTIVE) {
        onSelectViewMode(HistoryViewMode.ACTIVE)
    }

    BackHandler(enabled = isSearchOpen && viewMode == HistoryViewMode.ACTIVE) {
        onSearchQueryChange("")
        isSearchOpen = false
    }

    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alphaProvider() }
            .background(CocoCream)
            .statusBarsPadding()
            .nestedScroll(nestedScrollConnection)
            .pointerInput(isSearchOpen) {
                detectTapGestures {
                    if (isSearchOpen) {
                        keyboard?.hide()
                    }
                }
            }
            .pointerInput(isSearchOpen, searchQuery, notes) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (isSearchOpen && (searchQuery.isEmpty() || notes.isEmpty()) && dragAmount < -30f) {
                        onClearFilters()
                        isSearchOpen = false
                        keyboard?.hide()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else if (!isSearchOpen && !listState.canScrollBackward && dragAmount > 40f) {
                        isSearchOpen = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(dragModifier)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    var menuExpanded by remember { mutableStateOf(false) }
                    var shouldShowPopup by remember { mutableStateOf(false) }

                    LaunchedEffect(menuExpanded, fastMode) {
                        if (menuExpanded) {
                            shouldShowPopup = true
                        } else {
                            if (!fastMode) delay(250)
                            shouldShowPopup = false
                        }
                    }

                    Box {
                        val arrowRotation by animateFloatAsState(
                            targetValue = if (menuExpanded) 90f else 0f,
                            animationSpec = if (fastMode) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "arrowRot"
                        )
                        val menuAlpha by animateFloatAsState(
                            targetValue = if (menuExpanded) 1f else 0f,
                            animationSpec = if (fastMode) snap() else spring(stiffness = Spring.StiffnessMedium),
                            label = "menuAlpha"
                        )
                        val menuScale by animateFloatAsState(
                            targetValue = if (menuExpanded) 1f else 0.85f,
                            animationSpec = if (fastMode) snap() else spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
                            label = "menuScale"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { menuExpanded = !menuExpanded }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            AnimatedContent(
                                targetState = viewMode,
                                transitionSpec = {
                                    if (fastMode) {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    } else {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                },
                                label = "ViewTitle"
                            ) { mode ->
                                val titleText = when (mode) {
                                    HistoryViewMode.ACTIVE -> stringResource(R.string.your_notes)
                                    HistoryViewMode.ARCHIVED -> "📦 " + stringResource(R.string.archived_title)
                                    HistoryViewMode.TRASH -> "🗑️ " + stringResource(R.string.trash_title)
                                }
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = CocoBrownDark,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = ">",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CocoBrown,
                                modifier = Modifier.rotate(arrowRotation)
                            )
                        }

                        if (shouldShowPopup) {
                            val density = LocalDensity.current
                            val popupOffsetY = remember(density) { with(density) { 48.dp.roundToPx() } }
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = IntOffset(0, popupOffsetY),
                                onDismissRequest = { menuExpanded = false },
                                properties = PopupProperties(focusable = true)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(270.dp)
                                        .graphicsLayer {
                                            this.alpha = menuAlpha
                                            this.scaleX = menuScale
                                            this.scaleY = menuScale
                                            this.transformOrigin = TransformOrigin(0f, 0f)
                                        }
                                        .background(CocoCream, RoundedCornerShape(20.dp))
                                        .border(1.dp, CocoBrown.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val options = listOf(
                                        Triple(HistoryViewMode.ACTIVE, stringResource(R.string.your_notes), "$activeCount"),
                                        Triple(HistoryViewMode.ARCHIVED, "📦 " + stringResource(R.string.archived_title), "$archivedCount"),
                                        Triple(HistoryViewMode.TRASH, "🗑️ " + stringResource(R.string.trash_title), "$deletedCount")
                                    )
                                    options.forEach { (mode, label, badge) ->
                                        val isSelected = viewMode == mode
                                        val itemBorder = if (isSelected) {
                                            Modifier.border(1.dp, CocoBrown.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                        } else {
                                            Modifier
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSelected) CocoBrown.copy(alpha = 0.1f) else Color.Transparent)
                                                .then(itemBorder)
                                                .clickable {
                                                    onSelectViewMode(mode)
                                                    menuExpanded = false
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (isSelected) CocoBrownDark else CocoInk
                                            )
                                            if (badge.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) CocoBrownDark else CocoBrown.copy(alpha = 0.18f))
                                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = badge,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) CocoOnBrown else CocoBrownDark
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = {
                    isSearchOpen = !isSearchOpen
                    if (!isSearchOpen) onClearFilters()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.search_hint),
                        tint = if (isSearchOpen || anyFilterActive) CocoBrownDark else CocoBrown,
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = CocoBrown,
                    )
                }
            }

            // Barra de búsqueda neumórfica con filtros de color y enlaces
            AnimatedVisibility(
                visible = isSearchOpen || anyFilterActive,
                enter = if (fastMode) EnterTransition.None else expandVertically() + fadeIn(),
                exit = if (fastMode) ExitTransition.None else shrinkVertically() + fadeOut()
            ) {
                LaunchedEffect(isSearchOpen) {
                    if (isSearchOpen) {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphic(shape = CircleShape, surface = CocoCream, elevation = 3.dp)
                            .border(1.5.dp, CocoBrown.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CocoBrown, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = {
                                    onSearchQueryChange(it)
                                    if (it.isNotEmpty()) isSearchOpen = true
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = CocoInk),
                                cursorBrush = SolidColor(CocoBrownDark),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(stringResource(R.string.search_hint), style = MaterialTheme.typography.bodyMedium, color = CocoBrown.copy(alpha = 0.6f))
                                    }
                                    innerTextField()
                                }
                            )
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = if (fastMode) EnterTransition.None else fadeIn() + scaleIn(),
                                exit = if (fastMode) ExitTransition.None else fadeOut() + scaleOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clip(CircleShape)
                                        .background(CocoBrown.copy(alpha = 0.15f))
                                        .clickable {
                                            onSearchQueryChange("")
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = CocoBrownDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NotePalette.forEachIndexed { idx, col ->
                            val isSelected = selectedColorFilters.contains(idx)
                            val borderMod = if (isSelected) {
                                Modifier.border(2.5.dp, CocoBrownDark, CircleShape)
                            } else {
                                Modifier.border(1.dp, CocoBrown.copy(alpha = 0.25f), CircleShape)
                            }
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .then(borderMod)
                                    .clickable {
                                        onToggleColorFilter(idx)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(CocoBrown.copy(alpha = 0.2f))
                        )

                        val linkBorder = if (filterWithLinksOnly) {
                            Modifier.border(2.5.dp, CocoBrownDark, CircleShape)
                        } else {
                            Modifier.border(1.dp, CocoBrown.copy(alpha = 0.25f), CircleShape)
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (filterWithLinksOnly) CocoBrown.copy(alpha = 0.15f) else CocoCream)
                                .then(linkBorder)
                                .clickable {
                                    onToggleLinkFilter()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔗", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (notes.isEmpty()) {
                EmptyState(viewMode, isFiltering = anyFilterActive)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .fadingEdges(showFade = { listState.canScrollBackward }, showTopFade = true, showBottomFade = false, fadeHeight = 20.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            modifier = if (fastMode) Modifier else Modifier.animateItem(),
                            onTogglePin = { onTogglePin(note) },
                            onDelete = {
                                if (viewMode == HistoryViewMode.TRASH) {
                                    noteToDeletePermanently = note
                                } else {
                                    recentlyDeletedNote = note
                                    onDeleteNote(note)
                                }
                            },
                            onArchive = {
                                if (viewMode == HistoryViewMode.TRASH) {
                                    onRestoreNote(note)
                                } else {
                                    onArchiveNote(note)
                                }
                            },
                            onEdit = { onEditNote(note) },
                            onSelectColor = { idx -> onSelectColor(note, idx) },
                            onScheduleReminder = { targetMillis -> onScheduleReminder(note, targetMillis) },
                            isTrash = viewMode == HistoryViewMode.TRASH,
                            isPendingDelete = noteToDeletePermanently?.id == note.id,
                            onRestore = { onRestoreNote(note) },
                            onPermanentDelete = { noteToDeletePermanently = note },
                            fastMode = fastMode,
                        )
                    }
                    if (viewMode == HistoryViewMode.TRASH && notes.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_trash_btn),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFC6493C),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { showEmptyTrashConfirm = true }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }

        // Pastilla flotante de Deshacer (Undo) con alto contraste y posición optimizada
        AnimatedVisibility(
            visible = recentlyDeletedNote != null,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp, start = 24.dp, end = 24.dp)
        ) {
            val deletedNote = lastDeletedNoteForUI
            if (deletedNote != null) {
                Box(
                    modifier = Modifier
                        .neumorphic(
                            shape = CircleShape,
                            surface = CocoBrownDark,
                            elevation = 10.dp
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.note_deleted),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CocoOnBrown
                        )
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CocoGreen)
                                .clickable {
                                    onUndoDeleteNote(deletedNote)
                                    recentlyDeletedNote = null
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(stringResource(R.string.undo), style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(viewMode: HistoryViewMode, isFiltering: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val titleText = if (isFiltering) {
                stringResource(R.string.no_results_title)
            } else {
                when (viewMode) {
                    HistoryViewMode.ACTIVE -> stringResource(R.string.no_notes_title)
                    HistoryViewMode.ARCHIVED -> stringResource(R.string.archived_title)
                    HistoryViewMode.TRASH -> stringResource(R.string.trash_empty)
                }
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = CocoBrownDark,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            val subtitleText = if (isFiltering) {
                stringResource(R.string.no_results_subtitle)
            } else {
                when (viewMode) {
                    HistoryViewMode.ACTIVE -> stringResource(R.string.no_notes_subtitle)
                    HistoryViewMode.ARCHIVED -> stringResource(R.string.no_archived_subtitle)
                    HistoryViewMode.TRASH -> stringResource(R.string.trash_subtitle)
                }
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = CocoBrown,
                textAlign = TextAlign.Center,
            )
        }
    }
}
