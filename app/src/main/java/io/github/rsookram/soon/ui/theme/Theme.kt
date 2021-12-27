package io.github.rsookram.soon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material 2 theme which uses monochrome colours.
 */
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val primary = Color(if (darkTheme) 0xFFFFFFFF else 0xFF121212)
    val primaryVariant = Color(if (darkTheme) 0xFFE0E0E0 else 0xFF000000)
    val secondary = Color(if (darkTheme) 0xFF757575 else 0xFFE0E0E0)
    val background = Color(if (darkTheme) 0xFF121212 else 0xFFFFFFFF)
    val error = Color(if (darkTheme) 0xFFCF6679 else 0xFFB00020)
    val onPrimary = if (darkTheme) Color.Black else Color.White
    val onSecondary = if (darkTheme) Color.Black else Color.Black
    val onBackground = if (darkTheme) Color.White else Color.Black
    val onError = if (darkTheme) Color.Black else Color.White

    MaterialTheme(
        Colors(
            primary,
            primaryVariant,
            secondary,
            secondaryVariant = secondary,
            background,
            surface = background,
            error,
            onPrimary,
            onSecondary,
            onBackground,
            onSurface = onBackground,
            onError,
            isLight = !darkTheme,
        ),
        content = content,
    )
}
