package com.coco.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coco.app.R
import com.coco.app.domain.Note
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.coco.app.ui.theme.CocoBrown
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoGreen
import com.coco.app.ui.theme.CocoOnBrown
import com.coco.app.ui.theme.NotePalette
import com.coco.app.util.relativeTime
import com.coco.app.util.renderMarkdown
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onSelectColor: (Int) -> Unit,
    onScheduleReminder: (Long?) -> Unit,
    isTrash: Boolean = false,
    isPendingDelete: Boolean = false,
    onRestore: () -> Unit = {},
    onPermanentDelete: () -> Unit = {},
    fastMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    var copyJob by remember { mutableStateOf<Job?>(null) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    val palette = NotePalette
    val targetColor = palette.getOrElse(note.colorIndex) { palette[0] }
    val animatedCardColor = if (fastMode) {
        targetColor
    } else {
        val animated by animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 280),
            label = "CardColor"
        )
        animated
    }

    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentOnArchive by rememberUpdatedState(onArchive)
    val currentOnRestore by rememberUpdatedState(onRestore)
    val currentOnPermanentDelete by rememberUpdatedState(onPermanentDelete)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        delay(120)
                        if (isTrash) currentOnRestore() else currentOnArchive()
                    }
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isTrash) {
                        currentOnPermanentDelete()
                        false
                    } else {
                        scope.launch {
                            delay(120)
                            currentOnDelete()
                        }
                        true
                    }
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> if (isTrash) CocoGreen else CocoBrown
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFC6493C)
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    val emoji = when {
                        isTrash -> "♻️"
                        note.isArchived -> "📤"
                        else -> "📦"
                    }
                    Text(emoji, style = MaterialTheme.typography.titleMedium)
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) {
        val pinBorder = if (isPendingDelete) {
            Modifier.border(2.5.dp, Color(0xFFC6493C), RoundedCornerShape(20.dp))
        } else if (note.isPinned) {
            Modifier.border(2.dp, CocoBrownDark, RoundedCornerShape(20.dp))
        } else {
            Modifier.border(1.dp, CocoBrown.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphic(
                    shape = RoundedCornerShape(20.dp),
                    surface = animatedCardColor,
                    elevation = 4.dp,
                )
                .then(pinBorder)
                .combinedClickable(
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 300L) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            clipboardManager.setText(AnnotatedString(note.content))
                            isExpanded = false
                            copyJob?.cancel()
                            showCopiedFeedback = true
                            copyJob = scope.launch {
                                delay(1500)
                                showCopiedFeedback = false
                            }
                            lastTapTime = 0L
                        } else {
                            isExpanded = !isExpanded
                            lastTapTime = now
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTogglePin()
                    },
                )
                .padding(20.dp),
        ) {
            Column {
                val styledContent = remember(note.content) { renderMarkdown(note.content) }
                Text(
                    text = styledContent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CocoBrownDark,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = showCopiedFeedback,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(CocoGreen)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.copied_pill),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (note.hasLinks) {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(CocoBrown.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔗",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (note.isPinned) {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(CocoBrownDark)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pinned),
                                style = MaterialTheme.typography.labelSmall,
                                color = CocoOnBrown,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = relativeTime(note.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CocoBrown,
                    )
                }

                if (fastMode) {
                    if (isExpanded) {
                        ExpandedToolbar(
                            isTrash = isTrash,
                            note = note,
                            palette = palette,
                            onRestore = onRestore,
                            onPermanentDelete = onPermanentDelete,
                            onEdit = onEdit,
                            onSelectColor = onSelectColor
                        )
                    }
                } else {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ExpandedToolbar(
                            isTrash = isTrash,
                            note = note,
                            palette = palette,
                            onRestore = onRestore,
                            onPermanentDelete = onPermanentDelete,
                            onEdit = onEdit,
                            onSelectColor = onSelectColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedToolbar(
    isTrash: Boolean,
    note: Note,
    palette: List<Color>,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onEdit: () -> Unit,
    onSelectColor: (Int) -> Unit
) {
    Column(Modifier.padding(top = 16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CocoBrown.copy(alpha = 0.15f))
        )
        Spacer(Modifier.height(12.dp))
        if (isTrash) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CocoGreen)
                        .clickable { onRestore() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("♻️ Restaurar", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFC6493C))
                        .clickable { onPermanentDelete() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Editar
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CocoBrown.copy(alpha = 0.1f))
                        .clickable { onEdit() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = CocoBrownDark, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.edit_note), style = MaterialTheme.typography.labelMedium, color = CocoBrownDark)
                }

                // Selector de 4 colores pastel animado
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    palette.forEachIndexed { idx, col ->
                        val selectedBorder = if (note.colorIndex == idx) {
                            Modifier.border(2.dp, CocoBrownDark, CircleShape)
                        } else Modifier
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(1.dp, CocoBrown.copy(alpha = 0.3f), CircleShape)
                                .then(selectedBorder)
                                .clickable { onSelectColor(idx) }
                        )
                    }
                }
            }
        }
    }
}
