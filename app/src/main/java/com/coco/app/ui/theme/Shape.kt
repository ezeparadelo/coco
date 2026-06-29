package com.coco.app.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CocoShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

/**
 * Panel con borde superior en forma de domo/arco orgánico (sin líneas rectas duras).
 * El centro del borde superior sube hasta y=0; los extremos quedan a [rise] del tope.
 */
val CocoArchShape = GenericShape { size, _ ->
    val rise = size.width * 0.13f
    moveTo(0f, rise)
    cubicTo(
        size.width * 0.30f, 0f,
        size.width * 0.70f, 0f,
        size.width, rise,
    )
    lineTo(size.width, size.height + 2000f)
    lineTo(0f, size.height + 2000f)
    close()
}
