package com.coco.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.app.R
import com.coco.app.ui.theme.CocoBrown
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoCream
import com.coco.app.ui.theme.CocoGreen
import com.coco.app.ui.theme.CocoInk
import com.coco.app.ui.theme.CocoOnBrown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var dismissing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible && !dismissing,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        AnimatedVisibility(
            visible = visible && !dismissing,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(400),
            ),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(300),
            ),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = CocoCream,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "🥥", fontSize = 44.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CocoBrownDark,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    OnboardingItem(
                        emoji = "⚡",
                        title = stringResource(R.string.onboarding_step1_title),
                        desc = stringResource(R.string.onboarding_step1_desc),
                    )
                    Spacer(Modifier.height(16.dp))
                    OnboardingItem(
                        emoji = "👆",
                        title = stringResource(R.string.onboarding_step2_title),
                        desc = stringResource(R.string.onboarding_step2_desc),
                    )
                    Spacer(Modifier.height(16.dp))
                    OnboardingItem(
                        emoji = "📌",
                        title = stringResource(R.string.onboarding_step3_title),
                        desc = stringResource(R.string.onboarding_step3_desc),
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = {
                            dismissing = true
                            scope.launch {
                                delay(320)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CocoGreen,
                            contentColor = CocoOnBrown,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.got_it),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingItem(emoji: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(CocoBrown.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = CocoInk,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = CocoBrown,
            )
        }
    }
}
