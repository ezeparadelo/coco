package com.coco.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.coco.app.ui.theme.CocoArchBottom
import com.coco.app.ui.theme.CocoArchTop
import com.coco.app.ui.theme.CocoBrownDark
import kotlin.math.PI
import kotlin.math.sin

/**
 * El gran arco marrón oscuro: gradiente vertical para profundidad, una sombra suave que lo
 * despega de la crema y un borde superior con **oleaje** sutil (idle) — el domo ondula apenas
 * con una fase animada en loop. El contenido se dibuja por encima del relleno.
 */
@Composable
fun CocoArch(
    modifier: Modifier = Modifier,
    fastMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val phase = if (fastMode) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "arch")
        val animatedPhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 7000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "wavePhase",
        )
        animatedPhase
    }

    val archBrush = remember { Brush.verticalGradient(listOf(CocoArchTop, CocoArchBottom)) }
    val rimPath = remember { Path() }
    val fillPath = remember { Path() }

    Box(
        modifier
            .fillMaxWidth()
            .drawBehind {
                val w = size.width
                val h = size.height
                val rise = w * 0.13f      // altura del domo (un poco más suave)
                val amp = w * 0.0065f     // amplitud del oleaje (más sutil)
                val twoPi = (2 * PI).toFloat()
                val pi = PI.toFloat()
                val waves = 2f            // crestas a lo ancho

                fun topY(x: Float): Float {
                    val t = x / w
                    val dome = rise * (1f - sin(pi * t))            // apex arriba en el centro
                    val ripple = amp * sin(waves * twoPi * t + phase)
                    return amp + dome + ripple
                }

                val steps = 48
                // Borde superior (oleaje) como polilínea reutilizable.
                rimPath.reset()
                rimPath.moveTo(0f, topY(0f))
                for (i in 1..steps) {
                    val x = w * i / steps
                    rimPath.lineTo(x, topY(x))
                }

                // Sombra suave sobre la crema: la mitad superior del trazo queda visible y
                // despega el arco del fondo.
                drawPath(
                    path = rimPath,
                    color = CocoBrownDark.copy(alpha = 0.14f),
                    style = Stroke(width = 14.dp.toPx()),
                )

                // Relleno del arco (gradiente vertical).
                fillPath.reset()
                fillPath.addPath(rimPath)
                fillPath.lineTo(w, h + 2000f)
                fillPath.lineTo(0f, h + 2000f)
                fillPath.close()
                drawPath(
                    path = fillPath,
                    brush = archBrush,
                )

                // Brillo "húmedo" recorriendo el borde (define el filo del arco).
                drawPath(
                    path = rimPath,
                    color = Color.White.copy(alpha = 0.14f),
                    style = Stroke(width = 2.dp.toPx()),
                )
            },
        content = content,
    )
}
