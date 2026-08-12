package com.templesoftware.practicetimestables.domain.quiz

class QuizEngine(private val randomizer: QuizRandomizer = KotlinQuizRandomizer()) {
    fun newSession(selectedTables: Set<Int>): QuizState {
        require(selectedTables.isNotEmpty()) { "A quiz requires at least one selected table" }
        require(selectedTables.all { it in MIN_TABLE..MAX_TABLE })
        val orderedTables = selectedTables.sorted()
        val totalSeconds = quizDurationSeconds(orderedTables.size)
        val initialPool = QuestionPool.create(orderedTables, randomizer)
        val draw = initialPool.draw(randomizer)
        return QuizState(
            selectedTables = orderedTables,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            pool = draw.pool,
            currentQuestion = questionFor(draw.fact, totalSeconds, totalSeconds, orderedTables.size),
        )
    }

    fun dispatch(state: QuizState, action: QuizAction): QuizTransition = when (action) {
        is QuizAction.QuestionBecameInteractive -> becomeInteractive(state, action.timestampMillis)
        is QuizAction.DigitPressed -> pressDigit(state, action.digit, action.timestampMillis)
        QuizAction.Clear -> clear(state)
        is QuizAction.ElapseSeconds -> elapse(state, action.seconds)
        QuizAction.Advance -> advance(state)
    }

    private fun becomeInteractive(state: QuizState, timestampMillis: Long): QuizTransition {
        if (state.phase != QuizPhase.ANSWERING || state.interactiveStartedAtMillis != null) {
            return QuizTransition(state, QuizEvent.None)
        }
        require(timestampMillis >= 0)
        return QuizTransition(
            state.copy(interactiveStartedAtMillis = timestampMillis),
            QuizEvent.QuestionIsInteractive,
        )
    }

    private fun pressDigit(state: QuizState, digit: Int, timestampMillis: Long): QuizTransition {
        require(digit in 0..9)
        if (!state.isInteractive) return QuizTransition(state, QuizEvent.None)
        val expectedDigits = state.currentQuestion.expectedAnswer.toString()
        val expectedDigit = expectedDigits[state.enteredDigits.length].digitToInt()
        if (digit != expectedDigit) {
            return QuizTransition(
                state.copy(enteredDigits = "", hadMistake = true),
                QuizEvent.IncorrectDigit,
            )
        }

        val entered = state.enteredDigits + digit
        if (entered.length < expectedDigits.length) {
            return QuizTransition(
                state.copy(enteredDigits = entered),
                QuizEvent.DigitAccepted(entered),
            )
        }

        val startedAt = checkNotNull(state.interactiveStartedAtMillis)
        val response = CompletedResponse(
            fact = state.currentQuestion.fact,
            selectedTable = state.currentQuestion.fact.table,
            format = state.currentQuestion.format,
            expectedAnswer = state.currentQuestion.expectedAnswer,
            hadMistake = state.hadMistake,
            responseTimeMillis = (timestampMillis - startedAt).coerceAtLeast(0),
        )
        val nextPhase = if (state.timeExpired) QuizPhase.READY_TO_FINISH
        else QuizPhase.AWAITING_ADVANCE
        return QuizTransition(
            state.copy(
                enteredDigits = "",
                interactiveStartedAtMillis = null,
                completedResponses = state.completedResponses + response,
                phase = nextPhase,
            ),
            QuizEvent.QuestionCompleted(response),
        )
    }

    private fun clear(state: QuizState): QuizTransition {
        if (state.phase != QuizPhase.ANSWERING) return QuizTransition(state, QuizEvent.None)
        return QuizTransition(state.copy(enteredDigits = ""), QuizEvent.InputCleared)
    }

    private fun elapse(state: QuizState, seconds: Int): QuizTransition {
        require(seconds >= 0)
        val remaining = (state.remainingSeconds - seconds).coerceAtLeast(0)
        val ready = remaining == 0 && state.phase == QuizPhase.AWAITING_ADVANCE
        return QuizTransition(
            state.copy(
                remainingSeconds = remaining,
                phase = if (ready) QuizPhase.READY_TO_FINISH else state.phase,
            ),
            if (ready) QuizEvent.ReadyToFinish else QuizEvent.None,
        )
    }

    private fun advance(state: QuizState): QuizTransition {
        if (state.phase == QuizPhase.READY_TO_FINISH) {
            return QuizTransition(state, QuizEvent.ReadyToFinish)
        }
        if (state.phase != QuizPhase.AWAITING_ADVANCE) return QuizTransition(state, QuizEvent.None)
        if (state.timeExpired) {
            return QuizTransition(state.copy(phase = QuizPhase.READY_TO_FINISH), QuizEvent.ReadyToFinish)
        }
        val draw = state.pool.draw(randomizer)
        val question = questionFor(
            draw.fact,
            state.remainingSeconds,
            state.totalSeconds,
            state.selectedTables.size,
        )
        return QuizTransition(
            state.copy(
                pool = draw.pool,
                currentQuestion = question,
                enteredDigits = "",
                hadMistake = false,
                interactiveStartedAtMillis = null,
                phase = QuizPhase.ANSWERING,
            ),
            QuizEvent.Advanced,
        )
    }

    private fun questionFor(
        fact: MultiplicationFact,
        remaining: Int,
        total: Int,
        selectedTableCount: Int,
    ): QuizQuestion {
        val format = if (remaining * 2 > total) {
            QuestionFormat.RESULT_MISSING
        } else {
            val allowedFormats = if (selectedTableCount == 1) {
                SingleTableSecondHalfFormats
            } else {
                QuestionFormat.entries
            }
            allowedFormats[randomizer.nextFormatIndex(allowedFormats.size)]
        }
        return QuizQuestion(fact, format)
    }

    private companion object {
        val SingleTableSecondHalfFormats = listOf(
            QuestionFormat.FIRST_OPERAND_MISSING,
            QuestionFormat.RESULT_MISSING,
        )
    }
}
