package com.example.practicetimestables.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color(0xFF00315F),
    primaryContainer = BlueContainerDark,
    secondary = OrangeSecondaryDark,
    secondaryContainer = OrangeContainerDark,
    tertiary = GreenTertiaryDark,
    tertiaryContainer = GreenContainerDark,
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
)

@Composable
fun PracticeTimesTablesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
