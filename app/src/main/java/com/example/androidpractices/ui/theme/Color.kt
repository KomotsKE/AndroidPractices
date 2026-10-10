package com.example.androidpractices.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Тёплая «янтарная» палитра — в тон теме пивоварен.

internal val LightColors = lightColorScheme(
    primary = Color(0xFF8A4B08),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCBE),
    onPrimaryContainer = Color(0xFF2D1600),
    secondary = Color(0xFF735A43),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE3CC),
    onSecondaryContainer = Color(0xFF2A1706),
    tertiary = Color(0xFF55643A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD8EAB4),
    onTertiaryContainer = Color(0xFF141F02),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFFFF8F4),
    onBackground = Color(0xFF221A14),
    surface = Color(0xFFFFF8F4),
    onSurface = Color(0xFF221A14),
    surfaceVariant = Color(0xFFF3DFD1),
    onSurfaceVariant = Color(0xFF52443A),
    outline = Color(0xFF857469),
    outlineVariant = Color(0xFFD7C3B5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1E8),
    surfaceContainer = Color(0xFFFBEBE0),
    surfaceContainerHigh = Color(0xFFF5E5DA),
    surfaceContainerHighest = Color(0xFFEFE0D5),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB86F),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3B00),
    onPrimaryContainer = Color(0xFFFFDCBE),
    secondary = Color(0xFFE2C0A4),
    onSecondary = Color(0xFF412C18),
    secondaryContainer = Color(0xFF5A4330),
    onSecondaryContainer = Color(0xFFFFDCC0),
    tertiary = Color(0xFFBCCE9A),
    onTertiary = Color(0xFF283510),
    tertiaryContainer = Color(0xFF3E4C24),
    onTertiaryContainer = Color(0xFFD8EAB4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF19120C),
    onBackground = Color(0xFFEFE0D5),
    surface = Color(0xFF19120C),
    onSurface = Color(0xFFEFE0D5),
    surfaceVariant = Color(0xFF52443A),
    onSurfaceVariant = Color(0xFFD7C3B5),
    outline = Color(0xFF9F8D81),
    outlineVariant = Color(0xFF52443A),
    surfaceContainerLowest = Color(0xFF130D08),
    surfaceContainerLow = Color(0xFF221A14),
    surfaceContainer = Color(0xFF261E18),
    surfaceContainerHigh = Color(0xFF312822),
    surfaceContainerHighest = Color(0xFF3C332C),
)
