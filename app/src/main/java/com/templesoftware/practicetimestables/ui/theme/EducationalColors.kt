package com.templesoftware.practicetimestables.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class EducationalColors(
    val primaryGradientTop: Color,
    val primaryGradientBottom: Color,
    val secondaryGradientTop: Color,
    val secondaryGradientBottom: Color,
    val glossyHighlight: Color,
    val goldStar: Color,
    val unearnedStarOnRichSurface: Color,
    val softPanel: Color,
    val panelBorder: Color,
    val coralAccent: Color,
)

internal val LightEducationalColors = EducationalColors(
    primaryGradientTop = Color(0xFF626BE7),
    primaryGradientBottom = Color(0xFF30349A),
    secondaryGradientTop = Color(0xFF24BDB0),
    secondaryGradientBottom = Color(0xFF00796F),
    glossyHighlight = Color.White.copy(alpha = 0.38f),
    goldStar = Color(0xFFF2B83B),
    unearnedStarOnRichSurface = Color.White.copy(alpha = 0.52f),
    softPanel = Color(0xFFEDF2FF),
    panelBorder = Color(0xFFB9C5F4),
    coralAccent = Color(0xFFE8636F),
)

internal val DarkEducationalColors = EducationalColors(
    primaryGradientTop = Color(0xFF7079EE),
    primaryGradientBottom = Color(0xFF3A3FA8),
    secondaryGradientTop = Color(0xFF35C5B8),
    secondaryGradientBottom = Color(0xFF087C73),
    glossyHighlight = Color.White.copy(alpha = 0.3f),
    goldStar = Color(0xFFFFC75B),
    unearnedStarOnRichSurface = Color.White.copy(alpha = 0.48f),
    softPanel = Color(0xFF242B43),
    panelBorder = Color(0xFF56628D),
    coralAccent = Color(0xFFFF8A94),
)

internal val LocalEducationalColors = staticCompositionLocalOf { LightEducationalColors }
