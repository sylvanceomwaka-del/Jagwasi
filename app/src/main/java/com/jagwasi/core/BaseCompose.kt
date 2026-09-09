package com.jagwasi.core

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val JagwasiLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBB86FC),
    onPrimaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF3700B3),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBB86FC),
    onTertiaryContainer = Color(0xFF3700B3),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF3C3C3C),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFCD8D8),
    onErrorContainer = Color(0xFF7F0000),
    outline = Color(0xFF808080),
    outlineVariant = Color(0xFFC0C0C0),
    scrim = Color(0x66000000),
    inverseSurface = Color(0xFF121212),
    inverseOnSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFBB86FC)
)

val JagwasiDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color(0xFFD0B0FF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color(0xFFE0FFFF),
    tertiary = Color(0xFFBB86FC),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF3700B3),
    onTertiaryContainer = Color(0xFFD0B0FF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3C3C3C),
    onSurfaceVariant = Color(0xFFE0E0E0),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFFD8D8),
    outline = Color(0xFF808080),
    outlineVariant = Color(0xFF3C3C3C),
    scrim = Color(0xCC000000),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF6200EE)
)

val JagwasiTypography: Typography = Typography()

@Composable
fun JagwasiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JagwasiDarkColorScheme else JagwasiLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JagwasiTypography,
        content = content
    )
}

