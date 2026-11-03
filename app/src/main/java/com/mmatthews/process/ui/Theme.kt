package com.mmatthews.process.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/** The only three colours in the app. */
data class Palette(val background: Color, val text: Color, val accent: Color)

private val Light = Palette(
    background = Color(0xFFF7F7F4),
    text = Color(0xFF1B1D1C),
    accent = Color(0xFF2F6F62),
)
private val Dark = Palette(
    background = Color(0xFF111413),
    text = Color(0xFFE4E6E4),
    accent = Color(0xFF8CC7B8),
)

/** The only two text sizes in the app. */
object TextSizes {
    val Large = TextStyle(fontSize = 28.sp, lineHeight = 36.sp)
    val Normal = TextStyle(fontSize = 18.sp, lineHeight = 26.sp)
}

val LocalPalette = staticCompositionLocalOf { Light }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val p = if (dark) Dark else Light
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.accent, onPrimary = p.background,
            background = p.background, onBackground = p.text,
            surface = p.background, onSurface = p.text,
        )
    } else {
        lightColorScheme(
            primary = p.accent, onPrimary = p.background,
            background = p.background, onBackground = p.text,
            surface = p.background, onSurface = p.text,
        )
    }
    val normal = TextSizes.Normal
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(bodyLarge = normal, bodyMedium = normal, labelLarge = normal),
    ) {
        CompositionLocalProvider(
            LocalPalette provides p,
            // No press ripples: the breathing circle is the only animation.
            LocalRippleConfiguration provides null,
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = p.accent,
                backgroundColor = p.accent.copy(alpha = 0.3f),
            ),
            content = content,
        )
    }
}
