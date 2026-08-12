package com.example.practicetimestables.ui.repeat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.ui.components.PracticeAppBar
import com.example.practicetimestables.ui.layout.currentResponsiveLayoutInfo
import com.example.practicetimestables.ui.navigation.NavigationAction
import com.example.practicetimestables.ui.theme.AppMotion
import kotlin.math.abs

@Composable
fun RepeatScreen(
    language: AppLanguage,
    uiState: RepeatUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBackClick: () -> Unit,
    onNextTableClick: () -> Unit,
    onQuizClick: () -> Unit,
) {
    val layout = currentResponsiveLayoutInfo()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PracticeAppBar(
                title = stringResource(R.string.refresh_table_title, uiState.activeTable),
                navigationAction = NavigationAction.BACK,
                language = language,
                onNavigationClick = onBackClick,
                onLanguageSelected = onLanguageSelected,
            )
        },
        bottomBar = {
            if (!layout.isLandscape) {
                RepeatPortraitActionBar(
                    visible = uiState.completed,
                    hasNextTable = uiState.hasNextTable,
                    onClick = if (uiState.hasNextTable) onNextTableClick else onQuizClick,
                )
            }
        },
    ) { contentPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            val centreFontSize = when {
                fontScale > 1.5f -> 30.sp
                layout.isLandscape && maxHeight < 260.dp -> 26.sp
                layout.isLandscape && maxHeight < 340.dp -> 30.sp
                layout.isLandscape -> 32.sp
                layout.isExpandedWidth -> 48.sp
                else -> 42.sp
            }

            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                val density = LocalDensity.current
                val lineStep = with(density) { (centreFontSize * 1.1f).toDp() } +
                    RepeatCarouselLineGap
                CarouselTrack(
                    currentIndex = uiState.currentItemIndex,
                    table = uiState.activeTable,
                    answerVisible = uiState.answerVisible,
                    isScrolling = uiState.isScrolling,
                    centreFontSize = centreFontSize,
                    lineStep = lineStep,
                )
            }

            if (layout.isLandscape) {
                BoxWithConstraints(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 12.dp),
                ) {
                    val maxButtonWidth = (this.maxWidth * 0.36f).coerceIn(220.dp, 340.dp)
                    RepeatActionButton(
                        visible = uiState.completed,
                        hasNextTable = uiState.hasNextTable,
                        maxWidth = maxButtonWidth,
                        onClick = if (uiState.hasNextTable) onNextTableClick else onQuizClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselTrack(
    currentIndex: Int,
    table: Int,
    answerVisible: Boolean,
    isScrolling: Boolean,
    centreFontSize: TextUnit,
    lineStep: Dp,
) {
    val density = LocalDensity.current
    val slotPixels = with(density) { lineStep.toPx() }
    val targetPosition = currentIndex + if (isScrolling) 1f else 0f
    val animatedPosition = animateFloatAsState(
        targetValue = targetPosition,
        animationSpec = tween(
            durationMillis = AppMotion.RepeatCarouselDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "repeat continuous carousel position",
    ).value
    val firstRenderedIndex = (currentIndex - 2).coerceAtLeast(RepeatUiState.INSTRUCTION_INDEX)
    val lastRenderedIndex = (currentIndex + 3).coerceAtMost(RepeatUiState.LAST_ITEM_INDEX)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(lineStep * CarouselViewportSlots)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        for (itemIndex in firstRenderedIndex..lastRenderedIndex) {
            key(table, itemIndex) {
                val item = repeatCarouselItem(itemIndex)
                val distance = itemIndex - animatedPosition
                val absoluteDistance = abs(distance)
                val scale = carouselScale(absoluteDistance)
                val alpha = carouselAlpha(absoluteDistance)
                val isLogicalCurrent = itemIndex == currentIndex
                CarouselItem(
                    item = item,
                    table = table,
                    centreFontSize = centreFontSize,
                    answerVisible = when {
                        itemIndex < currentIndex -> true
                        isLogicalCurrent -> answerVisible
                        else -> false
                    },
                    isLogicalCurrent = isLogicalCurrent,
                    modifier = Modifier.graphicsLayer {
                        translationY = distance * slotPixels
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                )
            }
        }
    }
}

@Composable
private fun CarouselItem(
    item: RepeatCarouselItem,
    table: Int,
    centreFontSize: TextUnit,
    answerVisible: Boolean,
    isLogicalCurrent: Boolean,
    modifier: Modifier,
) {
    when (item) {
        RepeatCarouselItem.Instruction -> RepeatInstruction(
            fontSize = centreFontSize * RepeatInstructionFontScale,
            modifier = modifier,
            isCentre = isLogicalCurrent,
        )
        is RepeatCarouselItem.Fact -> RepeatEquation(
            multiplier = item.multiplier,
            table = table,
            showAnswer = answerVisible,
            fontSize = centreFontSize,
            modifier = modifier,
            isCentre = isLogicalCurrent,
        )
    }
}

private fun carouselScale(distance: Float): Float =
    (1f - distance.coerceIn(0f, 1f) * AdjacentScaleReduction)

private fun carouselAlpha(distance: Float): Float = when {
    distance <= 1f -> 1f - (distance * 0.5f)
    distance >= CarouselFadeOutDistance -> 0f
    else -> 0.5f * (CarouselFadeOutDistance - distance) / (CarouselFadeOutDistance - 1f)
}

@Composable
private fun RepeatInstruction(
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    isCentre: Boolean,
) {
    val text = stringResource(R.string.repeat_out_loud)
    Text(
        text = text,
        modifier = modifier.then(if (isCentre) {
            Modifier.semantics { contentDescription = text }
        } else {
            Modifier.clearAndSetSemantics {}
        }),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = fontSize,
            lineHeight = fontSize * 1.1f,
        ),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
private fun RepeatEquation(
    multiplier: Int,
    table: Int,
    showAnswer: Boolean,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    isCentre: Boolean = false,
) {
    val answer = multiplier * table
    val spokenEquation = if (showAnswer) {
        stringResource(R.string.multiplication_equation, multiplier, table, answer)
    } else {
        stringResource(R.string.repeat_equation_without_answer, multiplier, table)
    }
    val accessibilityModifier = if (isCentre) {
        Modifier.semantics(mergeDescendants = true) { contentDescription = spokenEquation }
    } else {
        Modifier.clearAndSetSemantics {}
    }
    val density = LocalDensity.current
    val digitWidth = with(density) { fontSize.toDp() } * 0.64f
    val answerAlpha = animateFloatAsState(
        targetValue = if (showAnswer) 1f else 0f,
        animationSpec = tween(
            durationMillis = AppMotion.RepeatAnswerFadeDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "repeat answer opacity $table × $multiplier",
    ).value
    val prompt = stringResource(R.string.repeat_equation_without_answer, multiplier, table)
    Row(
        modifier = modifier.then(accessibilityModifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            modifier = Modifier.width(digitWidth * 8f).alignByBaseline(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = fontSize,
                lineHeight = fontSize * 1.1f,
                fontFeatureSettings = "tnum",
            ),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.table_number, answer),
            modifier = Modifier
                .width(digitWidth * 3f)
                .alignByBaseline()
                .graphicsLayer { alpha = answerAlpha },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = fontSize,
                lineHeight = fontSize * 1.1f,
                fontFeatureSettings = "tnum",
            ),
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start,
            maxLines = 1,
        )
    }
}

@Composable
private fun RepeatPortraitActionBar(visible: Boolean, hasNextTable: Boolean, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .heightIn(min = 76.dp),
            contentAlignment = Alignment.Center,
        ) {
            RepeatActionButton(visible, hasNextTable, maxWidth.coerceAtMost(520.dp), onClick)
        }
    }
}

@Composable
private fun RepeatActionButton(
    visible: Boolean,
    hasNextTable: Boolean,
    maxWidth: Dp,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(AppMotion.DefaultDurationMillis)),
        exit = fadeOut(tween(AppMotion.DefaultDurationMillis)),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        ) {
            Text(
                text = stringResource(if (hasNextTable) R.string.action_next else R.string.screen_quiz),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.size(10.dp))
            Icon(
                imageVector = if (hasNextTable) Icons.AutoMirrored.Filled.ArrowForward
                else Icons.Default.PlayArrow,
                contentDescription = null,
            )
        }
    }
}

private val RepeatCarouselLineGap = 16.dp
private const val CarouselViewportSlots = 3.4f
private const val RepeatInstructionFontScale = 0.82f
private const val AdjacentScaleReduction = 0.2f
private const val CarouselFadeOutDistance = 1.55f
