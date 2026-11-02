package com.mmatthews.process.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

private val Dark = darkColorScheme(
    primary = Color(0xFF8FC9BA),
    onPrimary = Color(0xFF00201A),
    primaryContainer = Color(0xFF1E4A41),
    onPrimaryContainer = Color(0xFFCDEAE1),
    secondary = Color(0xFFB2CBC4),
    onSecondary = Color(0xFF1D3530),
    secondaryContainer = Color(0xFF2C3F3B),
    onSecondaryContainer = Color(0xFFCDE7E0),
    background = Color(0xFF0E1211),
    onBackground = Color(0xFFE0E4E2),
    surface = Color(0xFF0E1211),
    onSurface = Color(0xFFE0E4E2),
    surfaceVariant = Color(0xFF28302E),
    onSurfaceVariant = Color(0xFFB9C5C1),
    surfaceContainer = Color(0xFF1A201F),
    surfaceContainerHigh = Color(0xFF232A28),
    outline = Color(0xFF6F7B77),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF5E1A14),
    onErrorContainer = Color(0xFFFFDAD5),
)

private val base = Typography()
private val AppTypography = base.copy(
    headlineSmall = base.headlineSmall.copy(fontSize = 26.sp, lineHeight = 34.sp),
    bodyLarge = base.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
    labelLarge = base.labelLarge.copy(fontSize = 16.sp),
)

@Composable
fun ProcessTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Dark, typography = AppTypography, content = content)
}
