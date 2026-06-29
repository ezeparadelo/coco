package com.coco.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coco.app.ui.theme.NeoLight
import com.coco.app.ui.theme.NeoShadow

/**
 * Superficie gomosa: sombra de color suave (raised) + leve realce de luz en el borde.
 * Las sombras de color requieren API 28+; en 26/27 se degradan a la sombra estándar.
 */
fun Modifier.neumorphic(
    shape: Shape,
    surface: Color,
    elevation: Dp = 10.dp,
    shadowColor: Color = NeoShadow,
    lightColor: Color = NeoLight,
): Modifier = this
    .shadow(elevation = elevation, shape = shape, ambientColor = shadowColor, spotColor = shadowColor)
    .background(color = surface, shape = shape)
    .border(width = 1.dp, color = lightColor.copy(alpha = 0.5f), shape = shape)

/**
 * Feedback táctil gomoso: la superficie se "hunde" con un resorte al presionar.
 * Conectar el mismo [interactionSource] al clickable.
 */
@Composable
fun Modifier.pressBounce(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
