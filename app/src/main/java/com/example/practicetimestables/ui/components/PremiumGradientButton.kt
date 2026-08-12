package com.example.practicetimestables.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun PremiumGradientButton(
    onClick: () -> Unit,
    gradientTop: Color,
    gradientBottom: Color,
    highlight: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    preserveAppearanceWhenDisabled: Boolean = false,
    tactilePress: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) if (tactilePress) 0.97f else 0.975f else 1f,
        animationSpec = if (tactilePress) tween(PRESS_ANIMATION_MILLIS) else spring(),
        label = "premium button press scale",
    )
    val elevation by animateDpAsState(
        if (pressed) 1.dp else 5.dp,
        animationSpec = if (tactilePress) tween(PRESS_ANIMATION_MILLIS) else spring(),
        label = "premium button elevation",
    )
    val translationY by animateDpAsState(
        if (pressed && tactilePress) 1.dp else 0.dp,
        animationSpec = tween(PRESS_ANIMATION_MILLIS),
        label = "premium button press translation",
    )
    val pressedFraction by animateFloatAsState(
        if (pressed && tactilePress) 1f else 0f,
        animationSpec = tween(PRESS_ANIMATION_MILLIS),
        label = "premium button gradient depth",
    )
    val shape = RoundedCornerShape(18.dp)
    val normalColors = listOf(
        lerp(gradientTop, Color.Black, 0.08f * pressedFraction),
        lerp(gradientBottom, Color.Black, 0.12f * pressedFraction),
    )
    val colors = if (enabled || preserveAppearanceWhenDisabled) normalColors
    else listOf(gradientTop.copy(alpha = 0.46f), gradientBottom.copy(alpha = 0.46f))
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY.toPx()
            }
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(colors))
            .border(BorderStroke(1.dp, highlight), shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

private const val PRESS_ANIMATION_MILLIS = 90
