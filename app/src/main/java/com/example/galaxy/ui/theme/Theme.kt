package com.example.galaxy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = JJBlue60,
    onPrimary = Color.White,
    primaryContainer = JJBlue90,
    onPrimaryContainer = JJBlue10,
    secondary = JJGold50,
    onSecondary = JJGold10,
    secondaryContainer = JJGold90,
    onSecondaryContainer = JJGold10,
    tertiary = JJBlue40,
    error = Error40,
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
)

private val DarkColorScheme = darkColorScheme(
    primary = JJBlue80,
    onPrimary = JJBlue10,
    primaryContainer = JJBlue20,
    onPrimaryContainer = JJBlue90,
    secondary = JJGold80,
    onSecondary = JJGold10,
    secondaryContainer = Color(0xFF524900),
    onSecondaryContainer = JJGold90,
    tertiary = JJBlue60,
    error = Error80,
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    surface = SurfaceDark,
    onSurface = Color(0xFFE4E2E6),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
)

@Composable
fun GalaxyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
