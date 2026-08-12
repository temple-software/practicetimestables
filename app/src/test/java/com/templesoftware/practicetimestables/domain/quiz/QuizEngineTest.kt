package com.templesoftware.practicetimestables.domain.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizEngineTest {
    @Test
    fun newSessionSortsTablesAndStartsWithResultMissingQuestion() {
        val state = QuizEngine(QuizTestRandomizer()).newSession(setOf(9, 2, 5))

        assertEquals(listOf(2, 5, 9), state.selectedTables)
        assertEquals(168, state.totalSeconds)
        assertEquals(QuestionFormat.RESULT_MISSING, state.currentQuestion.format)
        assertEquals(state.currentQuestion.fact.product, state.currentQuestion.expectedAnswer)
    }

    @Test
    fun firstHalfAlwaysUsesResultMissingAndBoundaryUsesSecondHalfRule() {
        val randomizer = QuizTestRandomizer(mutableListOf(0))
        val engine = QuizEngine(randomizer)
        val initial = engine.newSession(setOf(2))
        val firstHalf = advanceAfterCorrect(engine, initial.copy(remainingSeconds = 61), 0, 100)
        val boundary = advanceAfterCorrect(engine, firstHalf.copy(remainingSeconds = 60), 200, 300)

        assertEquals(QuestionFormat.RESULT_MISSING, firstHalf.currentQuestion.format)
        assertEquals(QuestionFormat.FIRST_OPERAND_MISSING, boundary.currentQuestion.format)
    }

    @Test
    fun oneTableSecondHalfUsesBothAllowedFormatsAndNeverSecondOperandMissing() {
        val randomizer = QuizTestRandomizer(mutableListOf(0, 1))
        val engine = QuizEngine(randomizer)
        var state = engine.newSession(setOf(8)).copy(remainingSeconds = 60)
        val formats = mutableListOf<QuestionFormat>()

        repeat(2) { index ->
            state = advanceAfterCorrect(engine, state, index * 100L, index * 100L + 50)
            formats += state.currentQuestion.format
        }

        assertEquals(
            setOf(QuestionFormat.FIRST_OPERAND_MISSING, QuestionFormat.RESULT_MISSING),
            formats.toSet(),
        )
        assertFalse(formats.contains(QuestionFormat.SECOND_OPERAND_MISSING))
    }

    @Test
    fun multipleTablesSecondHalfStillUsesAllFormatsAndExpectedAnswersAreExplicit() {
        val fact = MultiplicationFact(7, 8)
        assertEquals(56, QuizQuestion(fact, QuestionFormat.RESULT_MISSING).expectedAnswer)
        assertEquals(7, QuizQuestion(fact, QuestionFormat.FIRST_OPERAND_MISSING).expectedAnswer)
        assertEquals(8, QuizQuestion(fact, QuestionFormat.SECOND_OPERAND_MISSING).expectedAnswer)

        val randomizer = QuizTestRandomizer(mutableListOf(0, 1, 2))
        val engine = QuizEngine(randomizer)
        var state = engine.newSession(setOf(8, 9)).copy(remainingSeconds = 72)
        val formats = mutableListOf<QuestionFormat>()
        repeat(3) { index ->
            state = advanceAfterCorrect(engine, state, index * 100L, index * 100L + 50)
            formats += state.currentQuestion.format
        }
        assertEquals(QuestionFormat.entries.toSet(), formats.toSet())
    }

    @Test
    fun correctMultiDigitInputIsValidatedSequentiallyAndAwaitsAdvance() {
        val engine = QuizEngine(QuizTestRandomizer(reverseShuffle = true))
        var state = engine.newSession(setOf(12)) // 12 × 12 = 144
        state = engine.dispatch(state, QuizAction.QuestionBecameInteractive(1_000)).state

        val first = engine.dispatch(state, QuizAction.DigitPressed(1, 1_100))
        val second = engine.dispatch(first.state, QuizAction.DigitPressed(4, 1_200))
        val final = engine.dispatch(second.state, QuizAction.DigitPressed(4, 1_350))

        assertEquals("1", first.state.enteredDigits)
        assertEquals("14", second.state.enteredDigits)
        assertEquals(QuizPhase.AWAITING_ADVANCE, final.state.phase)
        assertEquals(350, final.state.completedResponses.single().responseTimeMillis)
        assertTrue(final.event is QuizEvent.QuestionCompleted)
    }

    @Test
    fun incorrectDigitClearsInputAndMistakeRemainsTrueAcrossMultipleErrors() {
        val engine = QuizEngine(QuizTestRandomizer(reverseShuffle = true))
        var state = engine.newSession(setOf(2)) // 12 × 2 = 24
        state = engine.dispatch(state, QuizAction.QuestionBecameInteractive(100)).state
        state = engine.dispatch(state, QuizAction.DigitPressed(2, 110)).state
        val wrongLater = engine.dispatch(state, QuizAction.DigitPressed(9, 120))
        val wrongAgain = engine.dispatch(wrongLater.state, QuizAction.DigitPressed(9, 130))

        assertEquals("", wrongLater.state.enteredDigits)
        assertTrue(wrongLater.state.hadMistake)
        assertTrue(wrongAgain.state.hadMistake)
        assertEquals(QuizEvent.IncorrectDigit, wrongAgain.event)

        var corrected = engine.dispatch(wrongAgain.state, QuizAction.DigitPressed(2, 140)).state
        corrected = engine.dispatch(corrected, QuizAction.DigitPressed(4, 150)).state
        val response = corrected.completedResponses.single()
        assertTrue(response.hadMistake)
        assertFalse(response.accurate)
    }

    @Test
    fun correctSingleDigitAnswerCompletesAndExtraInputIsIgnored() {
        val engine = QuizEngine(QuizTestRandomizer())
        var state = engine.newSession(setOf(2)) // 1 × 2 = 2
        state = engine.dispatch(state, QuizAction.QuestionBecameInteractive(10)).state
        val completed = engine.dispatch(state, QuizAction.DigitPressed(2, 20))
        val extra = engine.dispatch(completed.state, QuizAction.DigitPressed(2, 30))

        assertEquals(QuizPhase.AWAITING_ADVANCE, completed.state.phase)
        assertSame(completed.state, extra.state)
        assertEquals(1, extra.state.completedResponses.size)
    }

    @Test
    fun clearRemovesInputWithoutChangingMistakeCompletionOrStartTime() {
        val engine = QuizEngine(QuizTestRandomizer(reverseShuffle = true))
        var state = engine.newSession(setOf(2))
        state = engine.dispatch(state, QuizAction.QuestionBecameInteractive(500)).state
        state = engine.dispatch(state, QuizAction.DigitPressed(2, 550)).state
        val cleared = engine.dispatch(state, QuizAction.Clear)

        assertEquals("", cleared.state.enteredDigits)
        assertFalse(cleared.state.hadMistake)
        assertEquals(500L, cleared.state.interactiveStartedAtMillis)
        assertEquals(QuizPhase.ANSWERING, cleared.state.phase)
        assertEquals(QuizEvent.InputCleared, cleared.event)
    }

    @Test
    fun timingStartsOnlyWhenInteractiveAndErrorsOrClearDoNotRestartIt() {
        val engine = QuizEngine(QuizTestRandomizer(reverseShuffle = true))
        val initial = engine.newSession(setOf(2))
        assertSame(initial, engine.dispatch(initial, QuizAction.DigitPressed(2, 50)).state)
        var state = engine.dispatch(initial, QuizAction.QuestionBecameInteractive(1_000)).state
        state = engine.dispatch(state, QuizAction.DigitPressed(9, 1_100)).state
        state = engine.dispatch(state, QuizAction.Clear).state
        state = engine.dispatch(state, QuizAction.DigitPressed(2, 1_200)).state
        state = engine.dispatch(state, QuizAction.DigitPressed(4, 1_500)).state

        assertEquals(500, state.completedResponses.single().responseTimeMillis)
    }

    @Test
    fun countdownClampsAtZeroAndActiveQuestionMustStillBeCompleted() {
        val engine = QuizEngine(QuizTestRandomizer())
        var state = engine.newSession(setOf(2))
        state = engine.dispatch(state, QuizAction.QuestionBecameInteractive(0)).state
        state = engine.dispatch(state, QuizAction.ElapseSeconds(500)).state

        assertEquals(0, state.remainingSeconds)
        assertEquals(QuizPhase.ANSWERING, state.phase)
        val completed = engine.dispatch(state, QuizAction.DigitPressed(2, 100))
        assertEquals(QuizPhase.READY_TO_FINISH, completed.state.phase)
        assertTrue(completed.event is QuizEvent.QuestionCompleted)
    }

    @Test
    fun expiryWhileAwaitingAdvanceFinishesWithoutCreatingQuestion() {
        val engine = QuizEngine(QuizTestRandomizer())
        val awaiting = completeCurrent(engine, engine.newSession(setOf(2)), 0, 20)
        val originalQuestion = awaiting.currentQuestion
        val expired = engine.dispatch(awaiting.copy(remainingSeconds = 1), QuizAction.ElapseSeconds(1))
        val advance = engine.dispatch(expired.state, QuizAction.Advance)

        assertEquals(QuizPhase.READY_TO_FINISH, expired.state.phase)
        assertEquals(originalQuestion, advance.state.currentQuestion)
        assertEquals(1, advance.state.completedResponses.size)
    }

    @Test
    fun explicitAdvanceCreatesNextQuestionOnlyWhenTimeRemains() {
        val engine = QuizEngine(QuizTestRandomizer())
        val initial = engine.newSession(setOf(2))
        val awaiting = completeCurrent(engine, initial, 0, 20)
        assertEquals(QuizPhase.AWAITING_ADVANCE, awaiting.phase)

        val advanced = engine.dispatch(awaiting, QuizAction.Advance)
        assertEquals(QuizPhase.ANSWERING, advanced.state.phase)
        assertEquals(QuizEvent.Advanced, advanced.event)
        assertFalse(advanced.state.isInteractive)
        assertFalse(advanced.state.hadMistake)
        assertEquals("", advanced.state.enteredDigits)
        assertEquals(1, advanced.state.completedResponses.size)
        assertEquals(2, advanced.state.currentQuestion.fact.firstOperand)
    }

    private fun advanceAfterCorrect(
        engine: QuizEngine,
        state: QuizState,
        start: Long,
        end: Long,
    ): QuizState = engine.dispatch(completeCurrent(engine, state, start, end), QuizAction.Advance).state

    private fun completeCurrent(
        engine: QuizEngine,
        original: QuizState,
        start: Long,
        end: Long,
    ): QuizState {
        var state = engine.dispatch(original, QuizAction.QuestionBecameInteractive(start)).state
        original.currentQuestion.expectedAnswer.toString().forEach { digit ->
            state = engine.dispatch(state, QuizAction.DigitPressed(digit.digitToInt(), end)).state
        }
        return state
    }
}
