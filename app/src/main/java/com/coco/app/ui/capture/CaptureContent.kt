package com.coco.app.ui.capture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coco.app.R
import com.coco.app.domain.Note
import com.coco.app.ui.components.CocoArch
import com.coco.app.ui.components.pressBounce
import com.coco.app.ui.theme.CocoBrown
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoCream
import com.coco.app.ui.theme.CocoGreen
import com.coco.app.ui.theme.CocoOnBrown
import com.coco.app.ui.theme.DynaPuff
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.coco.app.util.MarkdownVisualTransformation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CaptureContent(
    isActive: Boolean,
    archOffsetProvider: () -> Float,
    layoutOffsetProvider: () -> Float = archOffsetProvider,
    normalOffsetPx: Float,
    historyOffsetPx: Float,
    expandedOffsetPx: Float,
    heightPx: Float,
    editingNote: Note?,
    onCancelEdit: () -> Unit,
    onSave: (String) -> Unit,
    dragModifier: Modifier,
    canRequestFocus: Boolean = true,
    fastMode: Boolean = false,
    enterToSubmit: Boolean = true,
    sharedText: String? = null,
    onConsumeSharedText: () -> Unit = {},
    onRequestExpand: () -> Unit = {},
    onBounce: () -> Unit = {},
    onTextLayoutChanged: (Float, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var textLayoutHeight by remember { mutableStateOf(0f) }
    var textFieldHeight by remember { mutableStateOf(0) }
    var localLines by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()
    var showSavedFeedback by remember { mutableStateOf(false) }
    var saveFeedbackJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    var textLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(textFieldValue.selection, textFieldValue.text, textLayoutResultState) {
        val layout = textLayoutResultState ?: return@LaunchedEffect
        if (scrollState.maxValue > 0 && textFieldHeight > 0) {
            val cursorOffset = textFieldValue.selection.end.coerceIn(0, layout.layoutInput.text.length)
            val cursorRect = layout.getCursorRect(cursorOffset)
            val viewportHeight = textFieldHeight.toFloat()
            val paddingPx = 24 * density
            if (cursorRect.bottom > scrollState.value + viewportHeight) {
                val target = (cursorRect.bottom - viewportHeight + paddingPx).toInt().coerceIn(0, scrollState.maxValue)
                scrollState.animateScrollTo(target)
            } else if (cursorRect.top < scrollState.value) {
                val target = (cursorRect.top - paddingPx).toInt().coerceIn(0, scrollState.maxValue)
                scrollState.animateScrollTo(target)
            }
        }
    }

    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            val currentText = textFieldValue.text
            val newText = if (currentText.isBlank()) sharedText else "$currentText\n\n$sharedText"
            textFieldValue = TextFieldValue(newText, TextRange(newText.length))
            focusRequester.requestFocus()
            keyboard?.show()
            onConsumeSharedText()
        }
    }

    LaunchedEffect(editingNote) {
        if (editingNote != null) {
            textFieldValue = TextFieldValue(editingNote.content, TextRange(editingNote.content.length))
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    LaunchedEffect(isActive, canRequestFocus) {
        if (isActive && canRequestFocus) {
            focusRequester.requestFocus()
            keyboard?.show()
        } else if (!isActive) {
            focusManager.clearFocus(force = true)
            keyboard?.hide()
        }
    }

    val save: () -> Unit = {
        val rawText = textFieldValue.text
        if (rawText.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSave(rawText)
            onBounce()
            textFieldValue = TextFieldValue("")
            if (editingNote != null) onCancelEdit()
            saveFeedbackJob?.cancel()
            showSavedFeedback = true
            saveFeedbackJob = scope.launch {
                delay(1800)
                showSavedFeedback = false
            }
        }
    }

    val insertMarkdown: (String) -> Unit = { tag ->
        val selStart = textFieldValue.selection.min
        val selEnd = textFieldValue.selection.max
        val currentText = textFieldValue.text
        if (selStart != selEnd) {
            val selected = currentText.substring(selStart, selEnd)
            val newText = currentText.substring(0, selStart) + tag + selected + tag + currentText.substring(selEnd)
            val newCursorStart = selStart + tag.length
            val newCursorEnd = selEnd + tag.length
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newCursorStart, newCursorEnd)
            )
        } else {
            val cursor = selStart
            if (cursor + tag.length <= currentText.length && currentText.substring(cursor, cursor + tag.length) == tag) {
                // Saltear los asteriscos de cierre
                textFieldValue = TextFieldValue(
                    text = currentText,
                    selection = TextRange(cursor + tag.length)
                )
            } else {
                val newText = currentText.substring(0, cursor) + tag + tag + currentText.substring(cursor)
                textFieldValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(cursor + tag.length)
                )
            }
        }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val historyProgressOf: (Float) -> Float = { offset ->
        if (historyOffsetPx > normalOffsetPx) {
            ((offset - normalOffsetPx) / (historyOffsetPx - normalOffsetPx)).coerceIn(0f, 1f)
        } else 0f
    }
    val expandProgressOf: (Float) -> Float = { offset ->
        if (normalOffsetPx > expandedOffsetPx) {
            ((normalOffsetPx - offset) / (normalOffsetPx - expandedOffsetPx)).coerceIn(0f, 1f)
        } else 0f
    }
    val pullTabAlphaOf: (Float) -> Float = { offset ->
        ((historyProgressOf(offset) - 0.2f) * 2f).coerceIn(0f, 1f)
    }
    val writeContentAlphaOf: (Float) -> Float = { offset ->
        (1f - (historyProgressOf(offset) * 2.5f)).coerceIn(0f, 1f)
    }

    Box(modifier.fillMaxSize()) {
        if (isActive) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { heightPx.toDp() })
                    .graphicsLayer {
                        val offset = archOffsetProvider()
                        val hp = historyProgressOf(offset)
                        alpha = (1f - (hp * 1.5f)).coerceIn(0f, 1f)
                        translationY = offset - heightPx + (30 * density)
                    }
                    .background(CocoCream)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { normalOffsetPx.toDp().coerceAtLeast(0.dp) })
                    .statusBarsPadding()
                    .padding(24.dp)
                    .graphicsLayer {
                        val offset = archOffsetProvider()
                        val hp = historyProgressOf(offset)
                        val ep = expandProgressOf(offset)
                        alpha = (1f - (hp * 1.5f) - (ep * 1.2f)).coerceIn(0f, 1f)
                        translationY = -ep * 50f
                    }
                    .then(dragModifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = DynaPuff,
                        fontWeight = FontWeight.Bold,
                        fontSize = 45.sp,
                        letterSpacing = (-1.5).sp,
                    ),
                    color = CocoBrownDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CocoBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(CocoBrown.copy(alpha = 0.4f)),
                )
            }
        }

        CocoArch(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = archOffsetProvider() }
                .then(dragModifier)
                .pointerInput(isActive) {
                    if (!isActive) {
                        detectTapGestures { onRequestExpand() }
                    }
                },
            fastMode = fastMode,
            animateWave = !fastMode,
        ) {
            val minVisibleHeightPx = with(LocalDensity.current) { 140.dp.toPx() }
            Box(
                Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val targetHeight = (heightPx - layoutOffsetProvider()).toInt().coerceAtLeast(minVisibleHeightPx.toInt())
                        val placeable = measurable.measure(
                            constraints.copy(
                                minHeight = targetHeight,
                                maxHeight = targetHeight
                            )
                        )
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
            ) {
                val draftText = textFieldValue.text.trim()
                val hasDraft = draftText.isNotEmpty()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .graphicsLayer { alpha = pullTabAlphaOf(archOffsetProvider()) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(CocoOnBrown.copy(alpha = 0.45f)),
                    )
                    Spacer(Modifier.height(7.dp))
                    if (editingNote != null || hasDraft) {
                        val previewStr = if (editingNote != null) {
                            stringResource(R.string.pull_tab_editing, draftText.take(22) + if (draftText.length > 22) "…" else "")
                        } else {
                            stringResource(R.string.pull_tab_draft, draftText.take(22) + if (draftText.length > 22) "…" else "")
                        }
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CocoOnBrown.copy(alpha = 0.16f))
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CocoGreen)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = previewStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = CocoOnBrown,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.pull_tab_write),
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoOnBrown.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                val imeBottomPx = WindowInsets.ime.getBottom(LocalDensity.current)
                val topPad = if (imeBottomPx > 0) 72.dp else 104.dp
                Box(
                    Modifier
                        .fillMaxSize()
                        .imePadding()
                        .graphicsLayer { alpha = writeContentAlphaOf(archOffsetProvider()) }
                        .pointerInput(isActive) {
                            if (!isActive) {
                                detectTapGestures { onRequestExpand() }
                            }
                        }
                        .padding(start = 28.dp, end = 28.dp, top = topPad, bottom = 16.dp),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        if (editingNote != null) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(CocoOnBrown.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.editing_mode_hint),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CocoOnBrown
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = CocoOnBrown,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            textFieldValue = TextFieldValue("")
                                            onCancelEdit()
                                        }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            enabled = isActive,
                            readOnly = !isActive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 64.dp)
                                .onSizeChanged { textFieldHeight = it.height }
                                .fadingEdges(
                                    showFade = {
                                        val offset = archOffsetProvider()
                                        ((localLines > 4) && (offset >= normalOffsetPx - 10f)) ||
                                            ((offset <= expandedOffsetPx + 10f) && scrollState.maxValue > 0)
                                    },
                                    showTopFade = scrollState.value > 0,
                                    showBottomFade = scrollState.value < scrollState.maxValue,
                                    fadeHeight = 16.dp
                                )
                                .verticalScroll(scrollState)
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = CocoOnBrown),
                            cursorBrush = SolidColor(CocoGreen),
                            visualTransformation = remember { MarkdownVisualTransformation() },
                            onTextLayout = { textLayoutResult ->
                                textLayoutResultState = textLayoutResult
                                val lines = textLayoutResult.lineCount
                                localLines = lines
                                val height = textLayoutResult.size.height.toFloat()
                                textLayoutHeight = height
                                onTextLayoutChanged(height, lines)
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = if (enterToSubmit) ImeAction.Done else ImeAction.Default,
                            ),
                            keyboardActions = KeyboardActions(onDone = { save() }),
                            decorationBox = { inner ->
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.hint_write_note),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = CocoOnBrown.copy(alpha = 0.55f),
                                    )
                                }
                                inner()
                            },
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedVisibility(
                            visible = false, // Desactivado por ahora a pedido del usuario
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val shortcuts = listOf(
                                    Pair("B", "**"),
                                    Pair("I", "*")
                                )
                                shortcuts.forEach { (label, token) ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    Box(
                                        modifier = Modifier
                                            .size(width = 44.dp, height = 40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CocoOnBrown.copy(alpha = 0.1f))
                                            .pressBounce(interactionSource)
                                            .clickable(interactionSource = interactionSource, indication = null) {
                                                insertMarkdown(token)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = when (label) {
                                                "B" -> MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                                "I" -> MaterialTheme.typography.labelLarge.copy(fontStyle = FontStyle.Italic)
                                                else -> MaterialTheme.typography.labelLarge
                                            },
                                            color = CocoOnBrown.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showSavedFeedback,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { -it / 2 },
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(CocoGreen)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Check, null, tint = CocoOnBrown, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.note_saved), style = MaterialTheme.typography.labelLarge, color = CocoOnBrown)
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        SaveButton(
                            enabled = textFieldValue.text.isNotBlank() && isActive,
                            fastMode = fastMode,
                            modifier = Modifier.graphicsLayer {
                                val a = writeContentAlphaOf(archOffsetProvider()).coerceAtLeast(0.01f)
                                scaleX = a
                                scaleY = a
                            },
                            onClick = save,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveButton(enabled: Boolean, fastMode: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val targetColor = if (enabled) CocoGreen else CocoOnBrown.copy(alpha = 0.22f)
    val background = if (fastMode) {
        targetColor
    } else {
        val animated by animateColorAsState(
            targetValue = targetColor,
            label = "saveBg",
        )
        animated
    }
    val bounceModifier = if (fastMode) Modifier else Modifier.pressBounce(interaction)
    Box(
        modifier = modifier
            .size(56.dp)
            .then(bounceModifier)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "Guardar",
            tint = CocoOnBrown,
        )
    }
}

fun Modifier.fadingEdges(
    showFade: () -> Boolean,
    showTopFade: Boolean = true,
    showBottomFade: Boolean = true,
    fadeHeight: androidx.compose.ui.unit.Dp = 16.dp,
    fadeColor: Color = Color.Unspecified,
): Modifier = this
    .then(
        if (fadeColor == Color.Unspecified) {
            Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        } else {
            Modifier
        }
    )
    .drawWithContent {
        drawContent()
        if (!showFade() || (!showTopFade && !showBottomFade)) return@drawWithContent
        val height = size.height
        val fadeHeightPx = fadeHeight.toPx()
        if (fadeHeightPx <= 0f || height <= 0f) return@drawWithContent
        
        val isOffscreen = fadeColor == Color.Unspecified
        val blendMode = if (isOffscreen) BlendMode.DstIn else BlendMode.SrcOver
        
        if (showTopFade) {
            val colors = if (isOffscreen) {
                listOf(Color.Transparent, Color.Black)
            } else {
                listOf(fadeColor, fadeColor.copy(alpha = 0f))
            }
            drawRect(
                brush = Brush.verticalGradient(
                    colors = colors,
                    startY = 0f,
                    endY = fadeHeightPx
                ),
                blendMode = blendMode
            )
        }
        
        if (showBottomFade) {
            val colors = if (isOffscreen) {
                listOf(Color.Black, Color.Transparent)
            } else {
                listOf(fadeColor.copy(alpha = 0f), fadeColor)
            }
            drawRect(
                brush = Brush.verticalGradient(
                    colors = colors,
                    startY = height - fadeHeightPx,
                    endY = height
                ),
                blendMode = blendMode
            )
        }
    }
