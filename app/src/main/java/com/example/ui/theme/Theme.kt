package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DoomlyDarkColorScheme = darkColorScheme(
    primary = DoomlyRed,
    onPrimary = DoomlyWhite,
    primaryContainer = DoomlyRedContainer,
    onPrimaryContainer = DoomlyWhite,
    secondary = DoomlySurfaceElevated,
    onSecondary = DoomlyWhite,
    secondaryContainer = DoomlyCharcoal,
    onSecondaryContainer = DoomlyMutedGray,
    tertiary = DoomlyRedBright,
    onTertiary = DoomlyWhite,
    background = DoomlyBlack,
    onBackground = DoomlyWhite,
    surface = DoomlyCharcoal,
    onSurface = DoomlyWhite,
    surfaceVariant = DoomlySurfaceElevated,
    onSurfaceVariant = DoomlyMutedGray,
    outline = DoomlyBorder,
    outlineVariant = DoomlyDivider,
    error = DoomlyRedBright,
    onError = DoomlyWhite,
    errorContainer = DoomlyRedContainer,
    onErrorContainer = DoomlyWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // DOOMLY uses a dedicated, controlled near-black and crimson-accent color scheme
    MaterialTheme(
        colorScheme = DoomlyDarkColorScheme,
        typography = Typography,
        content = content
    )
}
