package com.pause.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFF9800),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF212121),
    background = Color(0xFFFFFFFF),
    error = Color(0xFFB00020)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFF9800),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    background = Color(0xFF121212),
    error = Color(0xFFCF6679)
)

private val AppTypography = Typography()

@Composable
fun PAUSEAppTheme(content: @Composable () -> Unit) {
    val useDark = isSystemInDarkTheme()
    val colors = if (useDark) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
