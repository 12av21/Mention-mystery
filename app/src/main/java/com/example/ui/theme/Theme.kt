package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MansionDarkColorScheme = darkColorScheme(
    primary = AntiqueGold,
    onPrimary = MidnightVoid,
    primaryContainer = VelvetIndigo,
    onPrimaryContainer = PaleGold,
    secondary = BurnishedGold,
    onSecondary = MidnightVoid,
    secondaryContainer = CharcoalSlate,
    onSecondaryContainer = ParchmentIvory,
    tertiary = MoonlightCyan,
    onTertiary = MidnightVoid,
    background = MidnightVoid,
    onBackground = ParchmentIvory,
    surface = CharcoalSlate,
    onSurface = ParchmentIvory,
    surfaceVariant = VelvetIndigo,
    onSurfaceVariant = MutedPewter,
    outline = BorderSteel,
    error = CrimsonWax,
    onError = ParchmentIvory,
)

private val MansionShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
)

@Composable
fun MansionMysteryTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MansionDarkColorScheme,
        typography = MansionTypography,
        shapes = MansionShapes,
        content = content,
    )
}
