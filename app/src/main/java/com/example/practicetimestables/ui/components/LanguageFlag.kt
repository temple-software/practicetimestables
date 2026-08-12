package com.example.practicetimestables.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.practicetimestables.data.preferences.AppLanguage

@Composable
fun LanguageFlag(language: AppLanguage, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .width(28.dp)
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(2.dp)),
    ) {
        when (language) {
            AppLanguage.ENGLISH_UK -> drawUnitedKingdomFlag()
            AppLanguage.FRENCH -> drawFrenchFlag()
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFrenchFlag() {
    val third = size.width / 3f
    drawRect(Color(0xFF1B3F8B), size = Size(third, size.height))
    drawRect(Color.White, topLeft = Offset(third, 0f), size = Size(third, size.height))
    drawRect(Color(0xFFE33B44), topLeft = Offset(third * 2f, 0f), size = Size(third, size.height))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUnitedKingdomFlag() {
    val blue = Color(0xFF173A7A)
    val red = Color(0xFFC92D3B)
    drawRect(blue)

    val diagonal = size.height * 0.24f
    drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), diagonal, StrokeCap.Square)
    drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), diagonal, StrokeCap.Square)
    drawLine(red, Offset(0f, 0f), Offset(size.width, size.height), diagonal * 0.42f, StrokeCap.Square)
    drawLine(red, Offset(size.width, 0f), Offset(0f, size.height), diagonal * 0.42f, StrokeCap.Square)

    drawRect(Color.White, topLeft = Offset(0f, size.height * 0.34f), size = Size(size.width, size.height * 0.32f))
    drawRect(Color.White, topLeft = Offset(size.width * 0.39f, 0f), size = Size(size.width * 0.22f, size.height))
    drawRect(red, topLeft = Offset(0f, size.height * 0.41f), size = Size(size.width, size.height * 0.18f))
    drawRect(red, topLeft = Offset(size.width * 0.44f, 0f), size = Size(size.width * 0.12f, size.height))
}
