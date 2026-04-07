package com.example.galaxy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Navy40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Navy80,
    onPrimaryContainer = Navy10,
    secondary = Teal40,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Teal80,
    error = Red40,
    errorContainer = Red80,
    surface = Surface,
)

private val DarkColorScheme = darkColorScheme(
    primary = Navy80,
    onPrimary = Navy10,
    primaryContainer = Navy40,
    onPrimaryContainer = Navy80,
    secondary = Teal80,
    onSecondary = Navy10,
    secondaryContainer = Teal40,
    error = Red80,
    errorContainer = Red40,
    surface = SurfaceDark,
)

@Composable
fun GalaxyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
