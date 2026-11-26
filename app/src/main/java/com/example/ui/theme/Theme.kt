package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IndustrialTerminalColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextMain,
    primaryContainer = CrtBorder,
    onPrimaryContainer = AccentCyanLight,
    secondary = AccentGreen,
    onSecondary = BgDesktop,
    tertiary = AccentAmber,
    error = AccentRed,
    background = BgDesktop,
    onBackground = TextMain,
    surface = BgPanel,
    onSurface = TextMain,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextMuted,
    outline = BorderSteel
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndustrialTerminalColorScheme,
        typography = Typography,
        content = content
    )
}
