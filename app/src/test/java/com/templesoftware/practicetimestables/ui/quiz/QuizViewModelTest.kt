package com.templesoftware.practicetimestables.ui.quiz

import androidx.lifecycle.SavedStateHandle
import com.templesoftware.practicetimestables.domain.quiz.QuizEngine
import com.templesoftware.practicetimestables.domain.quiz.QuizPhase
import com.templesoftware.practicetimestables.domain.quiz.QuizTestRandomizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
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
    fun correctAnswerRendersCompletedStateBeforeExplicitAdvance() {
        val viewModel = createViewModel(setOf(2))
        viewModel.questionBecameInteractive()
        val original = viewModel.uiState.value.currentQuestion
        enterCorrectAnswer(viewModel)

        assertEquals(QuizPhase.AWAITING_ADVANCE, viewModel.uiState.value.phase)
        assertEquals(original, viewModel.uiState.value.currentQuestion)
        assertEquals(original.expectedAnswer.toString(), completedAnswerText(viewModel))
        assertFalse(viewModel.uiState.value.isInteractive)
        assertTrue(viewModel.feedback.value is QuizFeedback.Correct)

        viewModel.advanceAfterCorrectTransition()
        assertEquals(QuizPhase.ANSWERING, viewModel.uiState.value.phase)
        assertFalse(viewModel.uiState.value.isInteractive)
        assertTrue(original != viewModel.uiState.value.currentQuestion)
    }

    @Test
    fun repeatedIncorrectDigitsEmitDistinctFeedbackAfterEachBriefInputHold() {
        val viewModel = createViewModel(setOf(2), reverse = true)
        viewModel.questionBecameInteractive()
        val expectedFirst = viewModel.uiState.value.currentQuestion.expectedAnswer.toString().first().digitToInt()
        val wrong = (expectedFirst + 1) % 10

        viewModel.pressDigit(wrong)
        val firstFeedback = viewModel.feedback.value as QuizFeedback.Incorrect
        assertEquals(wrong.toString(), viewModel.inputPresentation.value.attemptedDigits)
        assertFalse(viewModel.inputPresentation.value.acceptsInput)
        viewModel.finishIncorrectDigitConfirmation()
        viewModel.pressDigit(wrong)
        val secondFeedback = viewModel.feedback.value as QuizFeedback.Incorrect

        assertNotEquals(firstFeedback.id, secondFeedback.id)
        assertEquals("", viewModel.uiState.value.enteredDigits)
        assertTrue(viewModel.uiState.value.isInteractive)
    }

    @Test
    fun incorrectDigitIsPresentedBrieflyThenRetryIsEnabledWithoutResettingTiming() {
        val viewModel = createViewModel(setOf(2), reverse = true)
        viewModel.questionBecameInteractive()
        val startedAt = checkNotNull(viewModel.uiState.value.interactiveStartedAtMillis)
        val expected = viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        viewModel.pressDigit(expected.first().digitToInt())
        val wrong = ((expected.getOrNull(1) ?: expected.first()).digitToInt() + 1) % 10

        viewModel.pressDigit(wrong)

        assertEquals(expected.first() + wrong.toString(), viewModel.inputPresentation.value.attemptedDigits)
        assertTrue(viewModel.uiState.value.hadMistake)
        assertEquals("", viewModel.uiState.value.enteredDigits)
        assertFalse(viewModel.inputPresentation.value.acceptsInput)
        assertTrue(viewModel.feedback.value is QuizFeedback.Incorrect)
        assertEquals(startedAt, viewModel.uiState.value.interactiveStartedAtMillis)
        assertEquals(100, com.templesoftware.practicetimestables.ui.theme.AppMotion.QuizIncorrectDigitConfirmationMillis)

        viewModel.pressDigit(expected.first().digitToInt())
        assertEquals(expected.first() + wrong.toString(), viewModel.inputPresentation.value.attemptedDigits)

        viewModel.finishIncorrectDigitConfirmation()

        assertEquals(null, viewModel.inputPresentation.value.attemptedDigits)
        assertTrue(viewModel.inputPresentation.value.acceptsInput)
        assertEquals(startedAt, viewModel.uiState.value.interactiveStartedAtMillis)
    }

    @Test
    fun incorrectThirdDigitUsesTheExistingThirdAnswerSlot() {
        val viewModel = createViewModel(setOf(12), reverse = true)
        viewModel.questionBecameInteractive()
        val expected = viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        assertEquals(3, expected.length)
        viewModel.pressDigit(expected[0].digitToInt())
        viewModel.pressDigit(expected[1].digitToInt())
        val wrong = (expected[2].digitToInt() + 1) % 10

        viewModel.pressDigit(wrong)

        assertEquals(expected.take(2) + wrong, viewModel.inputPresentation.value.attemptedDigits)
        assertEquals("", viewModel.uiState.value.enteredDigits)
        assertTrue(viewModel.uiState.value.hadMistake)
    }

    @Test
    fun clearDoesNotEmitFeedbackOrChangeDomainProgression() {
        val viewModel = createViewModel(setOf(2), reverse = true)
        viewModel.questionBecameInteractive()
        val expected = viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        viewModel.pressDigit(expected.first().digitToInt())
        val feedbackBeforeClear = viewModel.feedback.value

        viewModel.clear()

        assertSame(feedbackBeforeClear, viewModel.feedback.value)
        assertEquals("", viewModel.uiState.value.enteredDigits)
        assertFalse(viewModel.uiState.value.hadMistake)
        assertTrue(viewModel.uiState.value.isInteractive)
    }

    @Test
    fun correctInputAfterErrorImmediatelySupersedesIncorrectFeedback() {
        val viewModel = createViewModel(setOf(2), reverse = true)
        viewModel.questionBecameInteractive()
        val expected = viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        viewModel.pressDigit((expected.first().digitToInt() + 1) % 10)
        val incorrectId = (viewModel.feedback.value as QuizFeedback.Incorrect).id
        viewModel.finishIncorrectDigitConfirmation()

        enterCorrectAnswer(viewModel)

        val correct = viewModel.feedback.value as QuizFeedback.Correct
        assertTrue(correct.id > incorrectId)
        assertEquals(QuizPhase.AWAITING_ADVANCE, viewModel.uiState.value.phase)
        viewModel.advanceAfterCorrectTransition()
        assertEquals(QuizPhase.ANSWERING, viewModel.uiState.value.phase)
        assertFalse(viewModel.uiState.value.isInteractive)
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
    fun malformedSavedSnapshotStartsAFreshValidSessionInsteadOfCrashing() {
        val handle = SavedStateHandle(
            mapOf(
                "quiz_snapshot_version" to 1,
                "quiz_tables" to arrayListOf(2),
                "quiz_question" to Int.MAX_VALUE,
            ),
        )

        val restored = createViewModel(setOf(5, 2), handle = handle)

        assertEquals(listOf(2, 5), restored.uiState.value.selectedTables)
        assertEquals(QuizPhase.ANSWERING, restored.uiState.value.phase)
        assertEquals(144, restored.uiState.value.totalSeconds)
    }

    @Test
    fun timerFormattingNeverProducesNegativeDisplay() {
        assertEquals("2:00", formatQuizTime(120))
        assertEquals("1:07", formatQuizTime(67))
        assertEquals("0:09", formatQuizTime(9))
        assertEquals("0:00", formatQuizTime(0))
    }

    @Test
    fun confirmationDelayIsShortAndDoesNotAlterRecordedResponseTime() {
        val viewModel = createViewModel(setOf(2), reverse = true)
        viewModel.questionBecameInteractive()
        val startedAt = checkNotNull(viewModel.uiState.value.interactiveStartedAtMillis)
        clock.now = startedAt + 345

        val answer = viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        answer.forEach { viewModel.pressDigit(it.digitToInt()) }

        assertEquals(100, com.templesoftware.practicetimestables.ui.theme.AppMotion.QuizAnswerConfirmationMillis)
        assertEquals(345, viewModel.uiState.value.completedResponses.last().responseTimeMillis)
        assertEquals(QuizPhase.AWAITING_ADVANCE, viewModel.uiState.value.phase)
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

    private fun completedAnswerText(viewModel: QuizViewModel): String =
        if (viewModel.uiState.value.phase == QuizPhase.ANSWERING) {
            viewModel.uiState.value.enteredDigits
        } else {
            viewModel.uiState.value.currentQuestion.expectedAnswer.toString()
        }

    private class MutableQuizClock(var now: Long) : QuizClock {
        override fun nowMillis(): Long = now
    }
}
