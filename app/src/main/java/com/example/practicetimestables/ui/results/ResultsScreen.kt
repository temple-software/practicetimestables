package com.example.practicetimestables.ui.results

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.domain.results.AccuracyScore
import com.example.practicetimestables.domain.results.SpeedScore
import com.example.practicetimestables.domain.results.TableScore
import com.example.practicetimestables.ui.components.PracticeAppBar
import com.example.practicetimestables.ui.layout.currentResponsiveLayoutInfo
import com.example.practicetimestables.ui.navigation.NavigationAction
import com.example.practicetimestables.ui.theme.educationalColors

@Composable
fun ResultsScreen(
    language: AppLanguage,
    state: ResultsUiState,
    playFanfare: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onReturnHome: () -> Unit,
) {
    BackHandler(onBack = onReturnHome)
    if (playFanfare) ResultsFanfareEffect()
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(280, easing = FastOutSlowInEasing)) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PracticeAppBar(
                title = stringResource(R.string.screen_results),
                navigationAction = NavigationAction.HOME,
                language = language,
                onNavigationClick = onReturnHome,
                onLanguageSelected = onLanguageSelected,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val layout = currentResponsiveLayoutInfo()
            val entranceModifier = Modifier
                .graphicsLayer {
                    alpha = entrance.value
                    scaleX = 0.98f + entrance.value * 0.02f
                    scaleY = scaleX
                }
            if (layout.isLandscape) {
                Row(
                    modifier = entranceModifier
                        .fillMaxSize()
                        .widthIn(max = 1_100.dp)
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.showTableBreakdown) {
                        Column(
                            Modifier.weight(0.82f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        ) {
                            AccuracyResult(state.results.overallAccuracy, Modifier.fillMaxWidth(), compact = true)
                            SpeedResult(state.results.overallSpeed, Modifier.fillMaxWidth(), compact = true)
                        }
                        TableBreakdown(
                            scores = state.results.tableScores,
                            modifier = Modifier.weight(1.18f).fillMaxHeight(),
                            compact = true,
                        )
                    } else {
                        AccuracyResult(
                            state.results.overallAccuracy,
                            Modifier.weight(1f).widthIn(max = 430.dp),
                            compact = true,
                        )
                        SpeedResult(
                            state.results.overallSpeed,
                            Modifier.weight(1f).widthIn(max = 430.dp),
                            compact = true,
                        )
                    }
                }
            } else {
                val contentModifier = entranceModifier
                    .fillMaxSize()
                    .widthIn(max = if (layout.isExpandedWidth) 980.dp else 680.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = if (layout.isExpandedWidth) 32.dp else 18.dp, vertical = 24.dp)
                Column(contentModifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (layout.isExpandedWidth) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            AccuracyResult(state.results.overallAccuracy, Modifier.weight(1f))
                            SpeedResult(state.results.overallSpeed, Modifier.weight(1f))
                        }
                    } else {
                        AccuracyResult(state.results.overallAccuracy, Modifier.fillMaxWidth())
                        SpeedResult(state.results.overallSpeed, Modifier.fillMaxWidth())
                    }
                    if (state.showTableBreakdown) {
                        TableBreakdown(
                            scores = state.results.tableScores,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsFanfareEffect() {
    val controller = remember { SynthesizedResultsFanfareController() }
    LaunchedEffect(controller) { controller.play() }
    DisposableEffect(controller) { onDispose(controller::release) }
}

@Composable
private fun AccuracyResult(score: AccuracyScore, modifier: Modifier, compact: Boolean = false) {
    ResultPanel(
        label = stringResource(R.string.results_accuracy),
        stars = score.stars,
        supportingText = stringResource(R.string.results_accuracy_percentage, score.percentage),
        modifier = modifier,
        compact = compact,
        gradientTop = MaterialTheme.educationalColors.primaryGradientTop,
        gradientBottom = MaterialTheme.educationalColors.primaryGradientBottom,
    )
}

@Composable
private fun SpeedResult(score: SpeedScore?, modifier: Modifier, compact: Boolean = false) {
    ResultPanel(
        label = stringResource(R.string.results_speed),
        stars = score?.stars ?: 0,
        supportingText = score?.let {
            stringResource(R.string.results_average_seconds, it.averageResponseTimeMillis / 1_000.0)
        } ?: stringResource(R.string.results_speed_unavailable),
        modifier = modifier,
        compact = compact,
        gradientTop = MaterialTheme.educationalColors.secondaryGradientTop,
        gradientBottom = MaterialTheme.educationalColors.secondaryGradientBottom,
    )
}

@Composable
private fun ResultPanel(
    label: String,
    stars: Int,
    supportingText: String,
    modifier: Modifier,
    compact: Boolean,
    gradientTop: Color,
    gradientBottom: Color,
) {
    val educationalColors = MaterialTheme.educationalColors
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = modifier
            .shadow(5.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(gradientTop, gradientBottom)))
            .border(BorderStroke(1.dp, educationalColors.glossyHighlight), shape),
    ) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = if (compact) 12.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 10.dp),
        ) {
            Text(
                label,
                color = Color.White,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            StarRating(stars = stars, starSize = if (compact) 30.dp else 42.dp, richSurface = true)
            Text(
                supportingText,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun StarRating(
    stars: Int,
    starSize: Dp,
    modifier: Modifier = Modifier,
    richSurface: Boolean = false,
) {
    require(stars in 0..5)
    val educationalColors = MaterialTheme.educationalColors
    val description = stringResource(R.string.results_star_rating_description, stars, 5)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            val awarded = index < stars
            Icon(
                imageVector = if (awarded) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (awarded) educationalColors.goldStar
                else if (richSurface) educationalColors.unearnedStarOnRichSurface
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                modifier = Modifier.size(starSize),
            )
        }
    }
}

@Composable
private fun TableBreakdown(
    scores: List<TableScore>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val educationalColors = MaterialTheme.educationalColors
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = educationalColors.softPanel,
        border = BorderStroke(1.dp, educationalColors.panelBorder),
        tonalElevation = 1.dp,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (compact) 20.dp else 24.dp,
                    vertical = if (compact) 16.dp else 20.dp,
                ),
        ) {
            Text(
                stringResource(R.string.results_breakdown),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = if (compact) 10.dp else 16.dp),
            )
            BreakdownRow(
                table = { Text(stringResource(R.string.results_table_heading), fontWeight = FontWeight.Bold) },
                accuracy = { HeadingText(R.string.results_accuracy) },
                speed = { HeadingText(R.string.results_speed) },
                compact = compact,
            )
            HorizontalDivider(
                Modifier.padding(vertical = if (compact) 3.dp else 8.dp),
                color = educationalColors.panelBorder,
            )
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                itemsIndexed(scores, key = { _, score -> score.table }) { index, score ->
                    BreakdownRow(
                        table = {
                            Text(
                                stringResource(R.string.results_table_value, score.table),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        accuracy = { StarRating(score.accuracy.stars, 22.dp) },
                        speed = { StarRating(score.speed?.stars ?: 0, 22.dp) },
                        compact = compact,
                    )
                    if (index != scores.lastIndex) {
                        HorizontalDivider(
                            Modifier.padding(vertical = if (compact) 2.dp else 8.dp),
                            color = educationalColors.panelBorder,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadingText(resource: Int) {
    Text(
        stringResource(resource),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun BreakdownRow(
    table: @Composable RowScope.() -> Unit,
    accuracy: @Composable RowScope.() -> Unit,
    speed: @Composable RowScope.() -> Unit,
    compact: Boolean,
) {
    val educationalColors = MaterialTheme.educationalColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(0.65f), content = table)
        Row(Modifier.weight(1.35f), horizontalArrangement = Arrangement.Center, content = accuracy)
        VerticalDivider(
            modifier = Modifier.size(width = 1.dp, height = if (compact) 20.dp else 28.dp),
            color = educationalColors.panelBorder,
        )
        Row(Modifier.weight(1.35f), horizontalArrangement = Arrangement.Center, content = speed)
    }
}
