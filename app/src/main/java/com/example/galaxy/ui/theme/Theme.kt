package com.example.galaxy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Coral40,
    onPrimary = Color.White,
    primaryContainer = Coral90,
    onPrimaryContainer = Coral10,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = Coral60,
    error = Error40,
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Coral80,
    onPrimary = Coral10,
    primaryContainer = Coral20,
    onPrimaryContainer = Coral90,
    secondary = Teal80,
    onSecondary = Teal10,
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Teal90,
    tertiary = Coral60,
    error = Error80,
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD8C2BF),
    outline = Color(0xFFA08C8A),
    outlineVariant = Color(0xFF534341),
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
