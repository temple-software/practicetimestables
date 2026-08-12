package com.example.practicetimestables.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.domain.quiz.QuestionFormat
import com.example.practicetimestables.domain.quiz.QuizPhase
import com.example.practicetimestables.domain.quiz.QuizQuestion
import com.example.practicetimestables.domain.quiz.QuizState
import com.example.practicetimestables.ui.components.PracticeAppBar
import com.example.practicetimestables.ui.layout.currentResponsiveLayoutInfo
import com.example.practicetimestables.ui.navigation.NavigationAction
import com.example.practicetimestables.ui.theme.AppMotion
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    language: AppLanguage,
    uiState: QuizState,
    feedback: QuizFeedback,
    inputPresentation: QuizInputPresentation,
    onLanguageSelected: (AppLanguage) -> Unit,
    onQuestionReady: () -> Unit,
    onDigitPressed: (Int) -> Unit,
    onClearPressed: () -> Unit,
    onAbandonConfirmed: () -> Unit,
    onReadyForResults: () -> Unit,
) {
    var showAbandonDialog by remember { mutableStateOf(false) }
    val audioSession = remember {
        QuizAudioSession(SynthesizedQuizAudioController(), feedback.id)
    }
    LaunchedEffect(feedback.id) { audioSession.onFeedback(feedback) }
    DisposableEffect(audioSession) {
        onDispose { audioSession.close() }
    }
    val shakeOffset = remember { Animatable(0f) }
    val tryAgainAlpha = remember { Animatable(0f) }
    val starAlpha = remember { Animatable(0f) }
    val starRotation = remember { Animatable(0f) }
    LaunchedEffect(feedback.id) {
        shakeOffset.stop()
        tryAgainAlpha.stop()
        starAlpha.stop()
        starRotation.stop()
        shakeOffset.snapTo(0f)
        tryAgainAlpha.snapTo(0f)
        starAlpha.snapTo(0f)
        starRotation.snapTo(0f)
        when (feedback) {
            QuizFeedback.None -> Unit
            is QuizFeedback.Incorrect -> coroutineScope {
                launch {
                    shakeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = AppMotion.QuizShakeDurationMillis
                            -AppMotion.QuizShakeMagnitudeDp.toFloat() at 55
                            AppMotion.QuizShakeMagnitudeDp * 0.8f at 120
                            -AppMotion.QuizShakeMagnitudeDp * 0.6f at 190
                            AppMotion.QuizShakeMagnitudeDp * 0.4f at 260
                            -AppMotion.QuizShakeMagnitudeDp * 0.2f at 330
                            0f at AppMotion.QuizShakeDurationMillis
                        },
                    )
                }
                launch {
                    tryAgainAlpha.animateTo(1f, tween(120))
                    delay((AppMotion.QuizTryAgainDurationMillis - 370).toLong())
                    tryAgainAlpha.animateTo(0f, tween(250))
                }
            }
            is QuizFeedback.Correct -> coroutineScope {
                launch {
                    starRotation.animateTo(
                        360f,
                        tween(AppMotion.QuizCorrectFeedbackDurationMillis, easing = FastOutSlowInEasing),
                    )
                }
                launch {
                    starAlpha.animateTo(1f, tween(140))
                    delay((AppMotion.QuizCorrectFeedbackDurationMillis - 390).toLong())
                    starAlpha.animateTo(0f, tween(250))
                }
            }
        }
    }
    BackHandler { showAbandonDialog = true }
    LaunchedEffect(uiState.currentQuestion, uiState.phase) {
        if (uiState.phase == QuizPhase.ANSWERING && !uiState.isInteractive) {
            withFrameNanos { }
            onQuestionReady()
        }
    }
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == QuizPhase.READY_TO_FINISH) onReadyForResults()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PracticeAppBar(
                title = stringResource(R.string.screen_quiz),
                navigationAction = NavigationAction.BACK,
                language = language,
                onNavigationClick = { showAbandonDialog = true },
                onLanguageSelected = onLanguageSelected,
            )
        },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            QuizBackground()
            val layout = currentResponsiveLayoutInfo()
            val content = Modifier
                .fillMaxSize()
                .padding(horizontal = if (layout.isExpandedWidth) 32.dp else 16.dp, vertical = 12.dp)
            if (layout.isLandscape) {
                Row(
                    modifier = content.widthIn(max = 980.dp).align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuizPromptArea(
                        uiState,
                        inputPresentation.attemptedDigits,
                        tryAgainAlpha.value,
                        Modifier
                            .weight(1.05f)
                            .fillMaxHeight()
                            .graphicsLayer { translationX = shakeOffset.value.dp.toPx() },
                    )
                    QuizKeypad(
                        enabled = uiState.isInteractive && inputPresentation.acceptsInput,
                        onDigitPressed = onDigitPressed,
                        onClearPressed = onClearPressed,
                        modifier = Modifier.weight(0.95f).fillMaxHeight().widthIn(max = 430.dp),
                    )
                }
            } else {
                Column(
                    modifier = content.widthIn(max = 560.dp).align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    QuizPromptArea(
                        uiState,
                        inputPresentation.attemptedDigits,
                        tryAgainAlpha.value,
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .graphicsLayer { translationX = shakeOffset.value.dp.toPx() },
                    )
                    QuizKeypad(
                        enabled = uiState.isInteractive && inputPresentation.acceptsInput,
                        onDigitPressed = onDigitPressed,
                        onClearPressed = onClearPressed,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 390.dp),
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = 12.dp)
                    .size(if (layout.isExpandedWidth) 76.dp else 60.dp)
                    .graphicsLayer {
                        alpha = starAlpha.value
                        rotationZ = starRotation.value
                        scaleX = 0.85f + starAlpha.value * 0.15f
                        scaleY = scaleX
                    }
                    .clearAndSetSemantics {},
            )
        }
    }

    if (showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            title = { Text(stringResource(R.string.quiz_abandon_title)) },
            text = { Text(stringResource(R.string.quiz_abandon_message)) },
            confirmButton = {
                TextButton(onClick = onAbandonConfirmed) { Text(stringResource(R.string.quiz_abandon_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonDialog = false }) {
                    Text(stringResource(R.string.quiz_abandon_stay))
                }
            },
        )
    }
}

@Composable
private fun QuizPromptArea(
    state: QuizState,
    attemptedDigits: String?,
    tryAgainAlpha: Float,
    modifier: Modifier,
) {
    val layout = currentResponsiveLayoutInfo()
    val timer = formatQuizTime(state.remainingSeconds)
    val timerDescription = stringResource(R.string.quiz_timer_description, timer)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = timer,
            modifier = Modifier.align(Alignment.End).semantics { contentDescription = timerDescription },
            color = if (state.timeExpired) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        Box(Modifier.weight(QuizQuestionRegionWeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = questionText(state.currentQuestion),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = if (layout.isLandscape) 38.sp else if (layout.isExpandedWidth) 58.sp else 46.sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.quiz_try_again),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { alpha = tryAgainAlpha },
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier.weight(QuizAnswerRegionWeight).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            AnswerSlots(state, attemptedDigits)
        }
    }
}

@Composable
private fun AnswerSlots(state: QuizState, attemptedDigits: String?) {
    val expected = state.currentQuestion.expectedAnswer.toString()
    val visible = attemptedDigits ?: if (state.phase == QuizPhase.ANSWERING) state.enteredDigits else expected
    val description = stringResource(R.string.quiz_answer_description, visible)
    Row(
        modifier = Modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        expected.indices.forEach { index ->
            Surface(
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 56.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = visible.getOrNull(index)?.toString() ?: stringResource(R.string.quiz_answer_placeholder),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private const val QuizQuestionRegionWeight = 1.1f
private const val QuizAnswerRegionWeight = 0.9f

@Composable
private fun QuizKeypad(
    enabled: Boolean,
    onDigitPressed: (Int) -> Unit,
    onClearPressed: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit ->
                    KeypadButton(digit.toString(), enabled, { onDigitPressed(digit) }, Modifier.weight(1f))
                }
            }
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KeypadButton("0", enabled, { onDigitPressed(0) }, Modifier.weight(1f))
            KeypadButton(
                stringResource(R.string.quiz_clear), enabled, onClearPressed, Modifier.weight(2f), isClear = true,
            )
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    isClear: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxSize().heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.large,
        colors = if (isClear) ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) else ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun questionText(question: QuizQuestion): String = when (question.format) {
    QuestionFormat.RESULT_MISSING -> stringResource(
        R.string.quiz_question_result_missing, question.fact.firstOperand, question.fact.table,
    )
    QuestionFormat.FIRST_OPERAND_MISSING -> stringResource(
        R.string.quiz_question_first_missing, question.fact.table, question.fact.product,
    )
    QuestionFormat.SECOND_OPERAND_MISSING -> stringResource(
        R.string.quiz_question_second_missing, question.fact.firstOperand, question.fact.product,
    )
}

internal fun formatQuizTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun QuizBackground() {
    val color = MaterialTheme.colorScheme.tertiaryContainer
    Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
        drawCircle(color.copy(alpha = 0.2f), size.minDimension * 0.38f)
    }
}
