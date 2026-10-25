package com.zhonghua.shici.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Cinnabar,
    onPrimary = White,
    primaryContainer = CinnabarMist,
    onPrimaryContainer = CinnabarDeep,
    secondary = Dai,
    onSecondary = White,
    secondaryContainer = DaiMist,
    onSecondaryContainer = Dai,
    tertiary = Gold,
    onTertiary = White,
    tertiaryContainer = GoldMist,
    onTertiaryContainer = Gold,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = InkSoft,
    outline = Line,
    error = CinnabarDeep
)

@Composable
fun ShiciTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = ShiciTypography,
        content = content
    )
}
