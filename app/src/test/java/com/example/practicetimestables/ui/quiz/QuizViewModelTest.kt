package com.example.practicetimestables.ui.quiz

import androidx.lifecycle.SavedStateHandle
import com.example.practicetimestables.domain.quiz.QuizEngine
import com.example.practicetimestables.domain.quiz.QuizPhase
import com.example.practicetimestables.domain.quiz.QuizTestRandomizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizViewModelTest {
    @Test
    fun freshViewModelInitializesSortedQuizSession() {
        val viewModel = createViewModel(setOf(12, 2, 5))

        assertEquals(listOf(2, 5, 12), viewModel.uiState.value.selectedTables)
        assertEquals(168, viewModel.uiState.value.remainingSeconds)
        assertFalse(viewModel.uiState.value.isInteractive)
    }

    @Test
    fun questionReadyMakesFirstQuestionInteractive() {
        val viewModel = createViewModel(setOf(2))
        viewModel.questionBecameInteractive()

        assertTrue(viewModel.uiState.value.isInteractive)
        assertEquals(clock.now, viewModel.uiState.value.interactiveStartedAtMillis)
    }

    @Test
    fun wrongDigitAndClearAreWiredThroughDomainValidation() {
        val viewModel = createViewModel(setOf(2), reverse = true)
        viewModel.questionBecameInteractive()
        val expected = viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        viewModel.pressDigit(expected.first().digitToInt())
        viewModel.clear()
        assertEquals("", viewModel.uiState.value.enteredDigits)
        assertFalse(viewModel.uiState.value.hadMistake)

        val wrong = (expected.first().digitToInt() + 1) % 10
        viewModel.pressDigit(wrong)
        assertEquals("", viewModel.uiState.value.enteredDigits)
        assertTrue(viewModel.uiState.value.hadMistake)
    }

    @Test
    fun correctAnswerAwaitsExplicitAdvanceThenCreatesNewQuestion() {
        val viewModel = createViewModel(setOf(2))
        viewModel.questionBecameInteractive()
        val original = viewModel.uiState.value.currentQuestion
        enterCorrectAnswer(viewModel)

        assertEquals(QuizPhase.AWAITING_ADVANCE, viewModel.uiState.value.phase)
        assertEquals(original, viewModel.uiState.value.currentQuestion)
        viewModel.advanceAfterCorrectTransition()
        assertEquals(QuizPhase.ANSWERING, viewModel.uiState.value.phase)
        assertFalse(viewModel.uiState.value.isInteractive)
        assertTrue(original != viewModel.uiState.value.currentQuestion)
    }

    @Test
    fun expiryKeepsActiveQuestionAndCompletionBecomesReadyForResults() {
        val viewModel = createViewModel(setOf(2))
        viewModel.questionBecameInteractive()
        val question = viewModel.uiState.value.currentQuestion
        clock.now += 121_000
        viewModel.synchronizeCountdown()

        assertEquals(0, viewModel.uiState.value.remainingSeconds)
        assertEquals(question, viewModel.uiState.value.currentQuestion)
        assertEquals(QuizPhase.ANSWERING, viewModel.uiState.value.phase)
        enterCorrectAnswer(viewModel)
        assertEquals(QuizPhase.READY_TO_FINISH, viewModel.uiState.value.phase)
        assertEquals(question, viewModel.uiState.value.currentQuestion)
    }

    @Test
    fun bundleSafeSnapshotRestoresQuestionInputTimerAndMistakeState() {
        val handle = SavedStateHandle()
        val original = createViewModel(setOf(2, 5), handle = handle, reverse = true)
        original.questionBecameInteractive()
        val expected = original.uiState.value.currentQuestion.expectedAnswer.toString()
        original.pressDigit(expected.first().digitToInt())
        val nextExpected = expected.getOrNull(1)?.digitToInt() ?: expected.first().digitToInt()
        original.pressDigit((nextExpected + 1) % 10)
        clock.now += 5_000
        original.synchronizeCountdown()

        val restored = createViewModel(setOf(2, 5), handle = handle, reverse = true)
        assertEquals(original.uiState.value, restored.uiState.value)
        assertTrue(restored.uiState.value.hadMistake)
        assertEquals(139, restored.uiState.value.remainingSeconds)
    }

    @Test
    fun timerFormattingNeverProducesNegativeDisplay() {
        assertEquals("2:00", formatQuizTime(120))
        assertEquals("1:07", formatQuizTime(67))
        assertEquals("0:09", formatQuizTime(9))
        assertEquals("0:00", formatQuizTime(0))
    }

    private val clock = MutableQuizClock(10_000)

    private fun createViewModel(
        tables: Set<Int>,
        handle: SavedStateHandle = SavedStateHandle(),
        reverse: Boolean = false,
    ) = QuizViewModel(
        savedStateHandle = handle,
        selectedTables = tables,
        engine = QuizEngine(QuizTestRandomizer(reverseShuffle = reverse)),
        clock = clock,
        automaticJobsEnabled = false,
    )

    private fun enterCorrectAnswer(viewModel: QuizViewModel) {
        viewModel.uiState.value.currentQuestion.expectedAnswer.toString().forEach {
            clock.now += 10
            viewModel.pressDigit(it.digitToInt())
        }
    }

    private class MutableQuizClock(var now: Long) : QuizClock {
        override fun nowMillis(): Long = now
    }
}
