package com.example.practicetimestables.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color(0xFF00315F),
    primaryContainer = BlueContainerDark,
    secondary = OrangeSecondaryDark,
    secondaryContainer = OrangeContainerDark,
    tertiary = GreenTertiaryDark,
    tertiaryContainer = GreenContainerDark,
    background = Color(0xFF19191E),
    surface = Color(0xFF19191E),
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    secondary = OrangeSecondary,
    secondaryContainer = OrangeContainer,
    tertiary = GreenTertiary,
    tertiaryContainer = GreenContainer,
    background = AppBackground,
    surface = AppSurface,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = MutedInk,
    outlineVariant = SoftOutline,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun PracticeTimesTablesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
