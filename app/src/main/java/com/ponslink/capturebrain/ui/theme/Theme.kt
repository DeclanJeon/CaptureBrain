package com.ponslink.capturebrain.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReferenceBlue = Color(0xFF4F7DFF)
private val ReferenceMint = Color(0xFF31E6C0)
private val ReferenceGold = Color(0xFFFFC766)

private val LightColors: ColorScheme = lightColorScheme(
    primary = ReferenceBlue,
    secondary = Color(0xFF0D9488),
    tertiary = Color(0xFFB45309),
    background = Color(0xFFF3F7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF9),
    primaryContainer = Color(0xFFE8EEFF),
    secondaryContainer = Color(0xFFE5FAF6),
    tertiaryContainer = Color(0xFFFFF2D6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF07111F),
    onSurface = Color(0xFF07111F),
    onSurfaceVariant = Color(0xFF526177),
    error = Color(0xFFB91C1C),
    errorContainer = Color(0xFFFFE4E9)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = ReferenceBlue,
    secondary = ReferenceMint,
    tertiary = ReferenceGold,
    background = Color(0xFF06111F),
    surface = Color(0xFF0C1A2B),
    surfaceVariant = Color(0xFF132743),
    primaryContainer = Color(0xFF102A58),
    secondaryContainer = Color(0xFF073337),
    tertiaryContainer = Color(0xFF2B1B29),
    onPrimary = Color.White,
    onSecondary = Color(0xFF06111F),
    onBackground = Color(0xFFF3F7FF),
    onSurface = Color(0xFFF3F7FF),
    onSurfaceVariant = Color(0xFFAAB8CD),
    error = Color(0xFFFF8FA3),
    errorContainer = Color(0xFF3A1B2A)
)

@Composable
fun CaptureBrainTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
