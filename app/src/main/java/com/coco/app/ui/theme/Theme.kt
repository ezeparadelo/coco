package com.coco.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CocoColorScheme = lightColorScheme(
    primary = CocoBrown,
    onPrimary = CocoOnBrown,
    secondary = CocoGreen,
    onSecondary = CocoOnBrown,
    tertiary = CocoGreen,
    onTertiary = CocoOnBrown,
    background = CocoCream,
    onBackground = CocoInk,
    surface = CocoCream,
    onSurface = CocoInk,
    surfaceVariant = NeoShadow,
    onSurfaceVariant = CocoBrownDark,
)

@Composable
fun CocoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CocoColorScheme,
        typography = CocoTypography,
        shapes = CocoShapes,
        content = content,
    )
}
