package com.templesoftware.practicetimestables.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = LearningBlueDark,
    onPrimary = OnLearningBlueDark,
    primaryContainer = LearningBlueContainerDark,
    onPrimaryContainer = OnLearningBlueContainerDark,
    secondary = ActionTealDark,
    onSecondary = OnActionTealDark,
    secondaryContainer = ActionTealContainerDark,
    onSecondaryContainer = OnActionTealContainerDark,
    tertiary = CelebrationGoldDark,
    onTertiary = OnCelebrationGoldDark,
    tertiaryContainer = CelebrationGoldContainerDark,
    onTertiaryContainer = OnCelebrationGoldContainerDark,
    error = WarmErrorDark,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = LearningBlueContainerDark,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedInk,
    outline = DarkMutedInk,
    outlineVariant = DarkOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = LearningBlue,
    onPrimary = OnLearningBlue,
    primaryContainer = LearningBlueContainer,
    onPrimaryContainer = OnLearningBlueContainer,
    secondary = ActionTeal,
    onSecondary = OnActionTeal,
    secondaryContainer = ActionTealContainer,
    onSecondaryContainer = OnActionTealContainer,
    tertiary = CelebrationGold,
    onTertiary = OnCelebrationGold,
    tertiaryContainer = CelebrationGoldContainer,
    onTertiaryContainer = OnCelebrationGoldContainer,
    error = WarmError,
    background = WarmBackground,
    onBackground = DeepNavyInk,
    surface = WarmSurface,
    surfaceContainerLowest = WarmSurface,
    surfaceContainerLow = PaleCream,
    surfaceContainer = PaleBlue,
    surfaceContainerHigh = PaleAqua,
    surfaceContainerHighest = PaleLavender,
    onSurface = DeepNavyInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedSlateInk,
    outline = MutedSlateInk,
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
    CompositionLocalProvider(
        LocalEducationalColors provides if (darkTheme) DarkEducationalColors else LightEducationalColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}

val MaterialTheme.educationalColors: EducationalColors
    @Composable get() = LocalEducationalColors.current
