package com.example.practicetimestables.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.ui.components.PracticeAppBar
import com.example.practicetimestables.ui.navigation.NavigationAction
import com.example.practicetimestables.ui.theme.AppMotion

private val MaxHomeWidth = 680.dp
private val MaxSelectorWidth = 400.dp
private val TableGap = 10.dp

@Composable
fun HomeScreen(
    language: AppLanguage,
    selectedTables: Set<Int>,
    onLanguageSelected: (AppLanguage) -> Unit,
    onTableToggled: (Int) -> Unit,
    onRefreshClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQuizClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PracticeAppBar(
                title = stringResource(R.string.screen_home),
                navigationAction = NavigationAction.NONE,
                language = language,
                onNavigationClick = {},
                onLanguageSelected = onLanguageSelected,
            )
        },
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            val horizontalPadding = when {
                maxWidth >= 840.dp -> 40.dp
                maxWidth >= 600.dp -> 28.dp
                else -> 16.dp
            }
            val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            val hasDistributionSpace = maxHeight >= 620.dp * fontScale

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                HomeBackground()
                if (hasDistributionSpace) {
                    SpaciousHomeContent(
                        selectedTables = selectedTables,
                        onTableToggled = onTableToggled,
                        onRefreshClick = onRefreshClick,
                        onRepeatClick = onRepeatClick,
                        onQuizClick = onQuizClick,
                        modifier = Modifier
                            .widthIn(max = MaxHomeWidth)
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding, vertical = 20.dp),
                    )
                } else {
                    ConstrainedHomeContent(
                        selectedTables = selectedTables,
                        onTableToggled = onTableToggled,
                        onRefreshClick = onRefreshClick,
                        onRepeatClick = onRepeatClick,
                        onQuizClick = onQuizClick,
                        modifier = Modifier
                            .widthIn(max = MaxHomeWidth)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBackground() {
    val primaryTone = MaterialTheme.colorScheme.primaryContainer
    val tertiaryTone = MaterialTheme.colorScheme.tertiaryContainer
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics {},
    ) {
        drawCircle(
            color = primaryTone.copy(alpha = 0.34f),
            radius = size.minDimension * 0.38f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.04f, size.height * 0.12f),
        )
        drawCircle(
            color = tertiaryTone.copy(alpha = 0.22f),
            radius = size.minDimension * 0.3f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.96f, size.height * 0.82f),
        )
    }
}

@Composable
private fun SpaciousHomeContent(
    selectedTables: Set<Int>,
    onTableToggled: (Int) -> Unit,
    onRefreshClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HomeHeading()
        Spacer(Modifier.height(20.dp))
        TableSelector(selectedTables, onTableToggled)
        ActionPanel(
            onRefreshClick = onRefreshClick,
            onRepeatClick = onRepeatClick,
            onQuizClick = onQuizClick,
            evenlyDistributed = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ConstrainedHomeContent(
    selectedTables: Set<Int>,
    onTableToggled: (Int) -> Unit,
    onRefreshClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HomeHeading()
        Spacer(Modifier.height(16.dp))
        TableSelector(selectedTables, onTableToggled)
        Spacer(Modifier.height(16.dp))
        ActionPanel(onRefreshClick, onRepeatClick, onQuizClick, evenlyDistributed = false)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HomeHeading() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.choose_tables),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.choose_tables_supporting_text),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TableSelector(
    selectedTables: Set<Int>,
    onTableToggled: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().widthIn(max = MaxSelectorWidth),
        contentAlignment = Alignment.Center,
    ) {
        val availableForControls = (maxWidth.coerceAtMost(MaxSelectorWidth) - TableGap * 4)
        val controlSize = (availableForControls / 5).coerceIn(48.dp, 72.dp)
        val gridWidth = controlSize * 5 + TableGap * 4
        Column(
            modifier = Modifier.width(gridWidth),
            verticalArrangement = Arrangement.spacedBy(TableGap),
        ) {
            TimesTableGrid.forEach { rowCells ->
                Row(
                    modifier = Modifier.width(gridWidth),
                    horizontalArrangement = Arrangement.spacedBy(TableGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowCells.forEach { table ->
                        if (table == null) {
                            Box(modifier = Modifier.size(controlSize))
                        } else {
                            TableControl(
                                table = table,
                                selected = table in selectedTables,
                                size = controlSize,
                                onClick = { onTableToggled(table) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableControl(
    table: Int,
    selected: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val animation = tween<Dp>(AppMotion.DefaultDurationMillis)
    val elevation by animateDpAsState(
        targetValue = if (selected) 5.dp else 1.dp,
        animationSpec = animation,
        label = "table elevation",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(AppMotion.DefaultDurationMillis),
        label = "table colour",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(AppMotion.DefaultDurationMillis),
        label = "table content colour",
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .semantics {
                this.selected = selected
                role = Role.Checkbox
            },
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = elevation,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.table_number, table),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPanel(
    onRefreshClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQuizClick: () -> Unit,
    evenlyDistributed: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 440.dp).fillMaxWidth(),
        verticalArrangement = if (evenlyDistributed) Arrangement.SpaceEvenly
        else Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SecondaryActionButton(R.string.screen_refresh, Icons.Default.Refresh, onRefreshClick)
        SecondaryActionButton(R.string.screen_repeat, Icons.AutoMirrored.Filled.ArrowForward, onRepeatClick)
        Button(
            onClick = onQuizClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.size(10.dp))
            Text(stringResource(R.string.screen_quiz), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SecondaryActionButton(
    labelResource: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 1.dp),
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.size(10.dp))
        Text(
            text = stringResource(labelResource),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}
