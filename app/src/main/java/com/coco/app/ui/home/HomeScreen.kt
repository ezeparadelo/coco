package com.coco.app.ui.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coco.app.R
import com.coco.app.ui.capture.CaptureContent
import com.coco.app.ui.history.HistoryContent
import com.coco.app.ui.onboarding.OnboardingDialog
import com.coco.app.ui.settings.SettingsDialog
import com.coco.app.ui.theme.CocoArchBottom
import com.coco.app.ui.theme.CocoArchTop
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoCream
import com.coco.app.ui.theme.CocoInk
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: HomeViewModel, onComposeReady: () -> Unit = {}) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val startInHistory by viewModel.startInHistory.collectAsStateWithLifecycle()
    val fastMode by viewModel.fastMode.collectAsStateWithLifecycle()
    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsStateWithLifecycle()
    val enterToSubmit by viewModel.enterToSubmit.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val archivedCount by viewModel.archivedCount.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()
    val deletedCount by viewModel.deletedCount.collectAsStateWithLifecycle()
    val deletedNotes by viewModel.deletedNotes.collectAsStateWithLifecycle()
    val editingNote by viewModel.editingNote.collectAsStateWithLifecycle()
    val selectedColorFilters by viewModel.selectedColorFilters.collectAsStateWithLifecycle()
    val filterWithLinksOnly by viewModel.filterWithLinksOnly.collectAsStateWithLifecycle()
    val sharedText by viewModel.sharedText.collectAsStateWithLifecycle()
    val quickCaptureTrigger by viewModel.quickCaptureTrigger.collectAsStateWithLifecycle()
    val easterEggTaps by viewModel.easterEggTaps.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    val context = LocalContext.current

    var heightPx by remember { mutableFloatStateOf(1f) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    val createDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null && pendingExportJson != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingExportJson!!.toByteArray())
                }
                Toast.makeText(context, context.getString(R.string.backup_exported), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        pendingExportJson = null
    }

    val openDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    showSettings = false
                    pendingImportJson = json
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val normalOffset by remember { derivedStateOf { heightPx * 0.42f } }
    val expandedOffset by remember { derivedStateOf { heightPx * 0.12f } }
    val historyOffset by remember { derivedStateOf { heightPx - (115 * density) } }

    fun animateOffsetTo(target: Float, velocity: Float = 0f) {
        scope.launch {
            if (fastMode) {
                offsetY = target
            } else {
                Animatable(offsetY).animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    initialVelocity = velocity,
                ) { offsetY = value }
            }
        }
    }

    fun animateBounce() {
        if (fastMode) return
        scope.launch {
            val start = offsetY
            val bouncePeak = (start - (15 * density)).coerceAtLeast(expandedOffset)
            Animatable(start).animateTo(
                targetValue = bouncePeak,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) { offsetY = value }
            Animatable(bouncePeak).animateTo(
                targetValue = normalOffset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) { offsetY = value }
        }
    }

    var startupComplete by remember { mutableStateOf(false) }
    val morphProgress = remember { Animatable(0f) }

    LaunchedEffect(initialized) {
        if (initialized && !startupComplete) {
            if (fastMode) {
                morphProgress.snapTo(1f)
                startupComplete = true
                onComposeReady()
            } else {
                withFrameNanos { }
                onComposeReady()
                delay(150)
                morphProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing,
                    )
                )
                startupComplete = true
            }
        }
    }

    LaunchedEffect(sharedText, initialized) {
        if (!sharedText.isNullOrBlank() && initialized) {
            animateOffsetTo(normalOffset, -1000f)
        }
    }

    val minOffset by remember { derivedStateOf { expandedOffset } }
    val maxOffset by remember { derivedStateOf { historyOffset } }

    var textHeight by remember { mutableStateOf(0f) }
    var textLines by remember { mutableStateOf(0) }

    val dragState = rememberDraggableState { delta ->
        offsetY = (offsetY + delta).coerceIn(minOffset, maxOffset)
    }
    val isInHistoryZone by remember {
        derivedStateOf { offsetY > (normalOffset + maxOffset) / 2f }
    }
    val dragModifier = Modifier
        .draggable(
            state = dragState,
            orientation = Orientation.Vertical,
            onDragStopped = { velocity ->
                val target = when {
                    velocity > 600f -> if (offsetY < normalOffset - 30f) normalOffset else maxOffset
                    velocity < -600f -> if (offsetY > normalOffset + 30f) normalOffset else minOffset
                    offsetY < (normalOffset + minOffset) / 2f -> minOffset
                    offsetY > (normalOffset + maxOffset) / 2f -> maxOffset
                    else -> normalOffset
                }
                if (target == maxOffset) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                animateOffsetTo(target, velocity)
            },
        )


    val progress by remember {
        derivedStateOf {
            if (maxOffset > normalOffset) {
                ((offsetY - normalOffset) / (maxOffset - normalOffset)).coerceIn(0f, 1f)
            } else 0f
        }
    }
    val isCapture by remember { derivedStateOf { initialized && progress < 0.5f } }
    val showOnboarding by remember { derivedStateOf { !hasSeenOnboarding && startupComplete } }
    
    val archPhase by remember {
        derivedStateOf { ((morphProgress.value - 0.6f) / 0.4f).coerceIn(0f, 1f) }
    }
    val effectiveOffsetY by remember {
        derivedStateOf {
            if (!startupComplete && initialized) {
                val targetOffset = if (startInHistory && !quickCaptureTrigger && sharedText == null) historyOffset else normalOffset
                targetOffset + (heightPx - targetOffset) * (1f - archPhase)
            } else offsetY
        }
    }
    
    val appAlpha by remember {
        derivedStateOf { if (startupComplete) 1f else archPhase }
    }

    LaunchedEffect(textLines, textHeight) {
        if (isCapture) {
            if (textLines >= 8) {
                if (offsetY > minOffset && Math.abs(offsetY - minOffset) > 1f) {
                    animateOffsetTo(minOffset, 0f)
                }
            } else {
                val singleLineHeight = if (textLines > 0) textHeight / textLines else 0f
                val extraHeight = if (textLines > 4) textHeight - (singleLineHeight * 4) else 0f
                val target = (normalOffset - extraHeight).coerceAtLeast(minOffset)
                if (Math.abs(offsetY - target) > 1f) {
                    if (target < normalOffset - 1f || offsetY < normalOffset - 1f) {
                        animateOffsetTo(target, 0f)
                    }
                }
            }
        }
    }

    LaunchedEffect(quickCaptureTrigger, initialized) {
        if (quickCaptureTrigger && initialized) {
            offsetY = normalOffset
            viewModel.consumeQuickCapture()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(CocoCream)
            .onSizeChanged {
                val h = it.height.toFloat().coerceAtLeast(1f)
                val w = it.width.toFloat().coerceAtLeast(1f)
                heightPx = h
                widthPx = w
                if (!initialized) {
                    val nOff = h * 0.42f
                    val hOff = h - (115 * density)
                    offsetY = if (startInHistory && !quickCaptureTrigger && sharedText == null) hOff else nOff
                    initialized = true
                }
            },
    ) {
        HistoryContent(
            notes = notes,
            alphaProvider = { progress * appAlpha },
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::setSearchQuery,
            viewMode = viewMode,
            onSelectViewMode = viewModel::setViewMode,
            archivedCount = archivedCount,
            activeCount = activeCount,
            deletedCount = deletedCount,
            onRestoreNote = viewModel::restoreNote,
            onPermanentDeleteNote = viewModel::deletePermanently,
            onEmptyTrash = viewModel::emptyTrash,
            onTogglePin = viewModel::togglePin,
            onDeleteNote = viewModel::deleteNote,
            onUndoDeleteNote = viewModel::undoDelete,
            onArchiveNote = { note -> viewModel.archiveNote(note, viewMode != HistoryViewMode.ARCHIVED) },
            onEditNote = { note ->
                viewModel.startEditing(note)
                animateOffsetTo(normalOffset, -1000f)
            },
            onSelectColor = viewModel::updateColor,
            onScheduleReminder = { note, triggerAtMillis ->
                viewModel.scheduleReminder(context, note, triggerAtMillis)
                if (triggerAtMillis != null) {
                    Toast.makeText(context, context.getString(R.string.reminder_set), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.reminder_cancelled), Toast.LENGTH_SHORT).show()
                }
            },
            selectedColorFilters = selectedColorFilters,
            onToggleColorFilter = viewModel::toggleColorFilter,
            filterWithLinksOnly = filterWithLinksOnly,
            onToggleLinkFilter = viewModel::toggleLinkFilter,
            onClearFilters = viewModel::clearFilters,
            onOpenSettings = { showSettings = true },
            dragModifier = dragModifier,
            fastMode = fastMode,
            modifier = Modifier.fillMaxSize(),
        )

        CaptureContent(
            isActive = isCapture,
            archOffsetProvider = { effectiveOffsetY },
            layoutOffsetProvider = { offsetY },
            normalOffsetPx = normalOffset,
            historyOffsetPx = historyOffset,
            expandedOffsetPx = expandedOffset,
            heightPx = heightPx,
            editingNote = editingNote,
            onCancelEdit = viewModel::cancelEditing,
            onSave = viewModel::save,
            dragModifier = dragModifier,
            canRequestFocus = hasSeenOnboarding && (startupComplete || morphProgress.value > 0.7f),
            fastMode = fastMode,
            enterToSubmit = enterToSubmit,
            sharedText = sharedText,
            onConsumeSharedText = viewModel::consumeSharedText,
            onRequestExpand = { animateOffsetTo(normalOffset, -1200f) },
            onBounce = { animateBounce() },
            onTextLayoutChanged = { height, lines ->
                textHeight = height
                textLines = lines
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = appAlpha },
        )

        if (!startupComplete && initialized) {
            val p = morphProgress.value
            val circleSizeDp = 170f
            val centerY = heightPx / 2f - (circleSizeDp / 2 * density)
            val bottomY = heightPx - (circleSizeDp / 2 * density)
            
            val circlePhase = (p / 0.5f).coerceIn(0f, 1f)
            val circleAlpha = if (p < 0.5f) 1f else ((0.6f - p) / 0.1f).coerceIn(0f, 1f)
            
            val currentY = centerY + (bottomY - centerY) * circlePhase

            if (circleAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            translationY = currentY
                            alpha = circleAlpha
                        }
                        .size(circleSizeDp.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(CocoArchTop, CocoArchBottom)))
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {}
            )
        }

        if (showOnboarding) {
            OnboardingDialog(onDismiss = { viewModel.completeOnboarding() })
        }
    }

    if (showSettings) {
        SettingsDialog(
            startInHistory = startInHistory,
            onStartInHistoryChange = viewModel::setStartInHistory,
            fastMode = fastMode,
            onFastModeChange = viewModel::setFastMode,
            enterToSubmit = enterToSubmit,
            onEnterToSubmitChange = viewModel::setEnterToSubmit,
            onExportBackup = {
                viewModel.exportBackup { json ->
                    pendingExportJson = json
                    createDocLauncher.launch("coco_backup.json")
                }
            },
            onImportBackup = {
                openDocLauncher.launch(arrayOf("application/json", "*/*"))
            },
            deletedCount = deletedNotes.size,
            easterEggTaps = easterEggTaps,
            onIncrementEasterEggTaps = viewModel::incrementEasterEggTaps,
            onDismiss = { showSettings = false },
        )
    }

    if (pendingImportJson != null) {
        val totalNotesCount = activeCount + archivedCount + deletedCount
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = { Text(stringResource(R.string.import_confirm_title), color = CocoBrownDark) },
            text = { Text(stringResource(R.string.import_confirm_desc, totalNotesCount), color = CocoInk) },
            confirmButton = {
                TextButton(onClick = {
                    val jsonToImport = pendingImportJson!!
                    pendingImportJson = null
                    showSettings = false
                    viewModel.importBackup(jsonToImport) {
                        Toast.makeText(context, context.getString(R.string.backup_imported), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(stringResource(R.string.replace), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) {
                    Text(stringResource(android.R.string.cancel), color = CocoBrownDark)
                }
            },
            containerColor = CocoCream
        )
    }
}
