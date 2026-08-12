package com.example.practicetimestables.ui.refresh

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.ui.components.PracticeAppBar
import com.example.practicetimestables.ui.navigation.NavigationAction
import com.example.practicetimestables.ui.layout.currentResponsiveLayoutInfo

private val MaxRefreshContentWidth = 760.dp
private val EquationElementSpacing = 6.dp

@Composable
fun RefreshScreen(
    language: AppLanguage,
    uiState: RefreshUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PracticeAppBar(
                title = stringResource(R.string.refresh_table_title, uiState.activeTable),
                navigationAction = NavigationAction.BACK,
                language = language,
                onNavigationClick = onBackClick,
                onLanguageSelected = onLanguageSelected,
                centeredTitle = true,
            )
        },
        bottomBar = {
            RefreshActionBar(
                hasNextTable = uiState.hasNextTable,
                onClick = if (uiState.hasNextTable) onNextClick else onRepeatClick,
            )
        },
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            val columnCount = when {
                fontScale > 1.5f -> 1
                maxWidth >= 700.dp || maxWidth > maxHeight * 1.35f -> 3
                else -> 2
            }
            val needsScrollFallback = maxHeight < 210.dp * fontScale || fontScale > 1.5f

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                if (needsScrollFallback) {
                    ConstrainedRefreshContent(
                        uiState = uiState,
                        columnCount = columnCount,
                        modifier = Modifier
                            .widthIn(max = MaxRefreshContentWidth)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                } else {
                    StandardRefreshContent(
                        uiState = uiState,
                        columnCount = columnCount,
                        modifier = Modifier
                            .widthIn(max = MaxRefreshContentWidth)
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardRefreshContent(
    uiState: RefreshUiState,
    columnCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        EquationGrid(
            table = uiState.activeTable,
            columnCount = columnCount,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ConstrainedRefreshContent(
    uiState: RefreshUiState,
    columnCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        EquationGrid(
            table = uiState.activeTable,
            columnCount = columnCount,
            modifier = Modifier.heightIn(
                min = when (columnCount) {
                    1 -> 620.dp
                    3 -> 190.dp
                    else -> 280.dp
                },
            ),
        )
    }
}

@Composable
private fun responsiveEquationFontSize(columnWidth: androidx.compose.ui.unit.Dp, rowHeight: androidx.compose.ui.unit.Dp): TextUnit {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val effectiveWidth = columnWidth / fontScale
    val effectiveHeight = rowHeight / fontScale
    return when {
        effectiveWidth >= 245.dp && effectiveHeight >= 52.dp -> 34.sp
        effectiveWidth >= 205.dp && effectiveHeight >= 45.dp -> 30.sp
        effectiveWidth >= 175.dp && effectiveHeight >= 39.dp -> 26.sp
        effectiveWidth >= 145.dp && effectiveHeight >= 32.dp -> 23.sp
        else -> 20.sp
    }
}

@Composable
private fun EquationGrid(
    table: Int,
    columnCount: Int,
    modifier: Modifier = Modifier,
) {
    val responsiveLayout = currentResponsiveLayoutInfo()
    val columns = when (columnCount) {
        1 -> listOf(1..12)
        3 -> listOf(1..4, 5..8, 9..12)
        else -> listOf(1..6, 7..12)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columnGap = if (maxWidth >= 600.dp) 24.dp else 10.dp
        val columnWidth = (maxWidth - columnGap * (columnCount - 1)) / columnCount
        val rowHeight = maxHeight / columns.first().count()
        val equationFontSize = responsiveEquationFontSize(columnWidth, rowHeight)

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(columnGap),
        ) {
            columns.forEach { multipliers ->
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = if (responsiveLayout.isPortrait) {
                        Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    } else {
                        Arrangement.SpaceEvenly
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    multipliers.forEach { multiplier ->
                        MultiplicationFact(
                            multiplier = multiplier,
                            table = table,
                            fontSize = equationFontSize,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiplicationFact(multiplier: Int, table: Int, fontSize: TextUnit) {
    val fullEquation = stringResource(
        R.string.multiplication_equation,
        multiplier,
        table,
        multiplier * table,
    )
    val density = LocalDensity.current
    val digitSlot = with(density) { fontSize.toDp() } * 0.64f
    val numberStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = fontSize,
        lineHeight = fontSize * 1.1f,
        fontFeatureSettings = "tnum",
        letterSpacing = 0.sp,
    )
    Row(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .semantics(mergeDescendants = true) { contentDescription = fullEquation },
        horizontalArrangement = Arrangement.spacedBy(EquationElementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.table_number, multiplier),
            modifier = Modifier.width(digitSlot * 2f).alignByBaseline(),
            style = numberStyle,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        EquationSymbol(stringResource(R.string.multiplication_sign), fontSize)
        Text(
            text = stringResource(R.string.table_number, table),
            modifier = Modifier.alignByBaseline(),
            style = numberStyle,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        EquationSymbol(stringResource(R.string.equals_sign), fontSize)
        Text(
            text = stringResource(R.string.table_number, multiplier * table),
            modifier = Modifier.width(digitSlot * 3f).alignByBaseline(),
            color = MaterialTheme.colorScheme.primary,
            style = numberStyle,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start,
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.EquationSymbol(text: String, fontSize: TextUnit) {
    Text(
        text = text,
        modifier = Modifier.alignByBaseline(),
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = fontSize,
            lineHeight = fontSize * 1.1f,
            letterSpacing = 0.sp,
        ),
        fontWeight = FontWeight.Medium,
        maxLines = 1,
    )
}

@Composable
private fun RefreshActionBar(hasNextTable: Boolean, onClick: () -> Unit) {
    val responsiveLayout = currentResponsiveLayoutInfo()
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maximumButtonWidth = when {
                responsiveLayout.isLandscape -> (maxWidth * 0.42f).coerceIn(240.dp, 360.dp)
                responsiveLayout.isExpandedWidth -> 480.dp
                else -> 520.dp
            }
            Button(
                onClick = onClick,
                modifier = Modifier
                    .widthIn(max = maximumButtonWidth)
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
            ) {
                Text(
                    text = stringResource(if (hasNextTable) R.string.action_next else R.string.action_repeat),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.size(10.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
