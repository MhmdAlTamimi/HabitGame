package com.habitgame.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple80 = Color(0xFFBB86FC)
private val Purple40 = Color(0xFF6200EE)
private val PurpleDark = Color(0xFF3700B3)
private val Teal = Color(0xFF03DAC6)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A148C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEF8),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00352F),
    tertiary = Color(0xFFFF6F00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF3E2700),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F0FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
)

@Composable
fun HabitGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
