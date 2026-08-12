package com.example.practicetimestables.ui.quiz

import androidx.lifecycle.SavedStateHandle
import com.example.practicetimestables.domain.quiz.CompletedResponse
import com.example.practicetimestables.domain.quiz.MAX_MULTIPLIER
import com.example.practicetimestables.domain.quiz.MIN_MULTIPLIER
import com.example.practicetimestables.domain.quiz.MultiplicationFact
import com.example.practicetimestables.domain.quiz.QuestionFormat
import com.example.practicetimestables.domain.quiz.QuestionPool
import com.example.practicetimestables.domain.quiz.QuizPhase
import com.example.practicetimestables.domain.quiz.QuizQuestion
import com.example.practicetimestables.domain.quiz.QuizState
import com.example.practicetimestables.domain.quiz.quizDurationSeconds

internal object QuizSavedState {
    private const val VERSION = "quiz_snapshot_version"
    private const val TABLES = "quiz_tables"
    private const val REMAINING = "quiz_remaining"
    private const val POOL = "quiz_pool"
    private const val LAST_FACT = "quiz_last_fact"
    private const val QUESTION = "quiz_question"
    private const val FORMAT = "quiz_format"
    private const val INPUT = "quiz_input"
    private const val MISTAKE = "quiz_mistake"
    private const val STARTED = "quiz_started"
    private const val RESPONSES = "quiz_responses"
    private const val PHASE = "quiz_phase"
    const val DEADLINE = "quiz_deadline"

    fun restore(handle: SavedStateHandle): QuizState? {
        if (handle.get<Int>(VERSION) != 1) return null
        val tables = handle.get<ArrayList<Int>>(TABLES)?.toList().orEmpty()
        if (tables.isEmpty()) return null
        val eligible = tables.flatMap { table ->
            (MIN_MULTIPLIER..MAX_MULTIPLIER).map { MultiplicationFact(it, table) }
        }
        val pool = QuestionPool(
            eligibleFacts = eligible,
            remainingFacts = handle.get<ArrayList<Int>>(POOL).orEmpty().map(::decodeFact),
            lastDrawnFact = handle.get<Int>(LAST_FACT)?.takeIf { it != 0 }?.let(::decodeFact),
        )
        return QuizState(
            selectedTables = tables,
            totalSeconds = quizDurationSeconds(tables.size),
            remainingSeconds = handle[REMAINING] ?: quizDurationSeconds(tables.size),
            pool = pool,
            currentQuestion = QuizQuestion(
                fact = decodeFact(handle.get<Int>(QUESTION) ?: return null),
                format = QuestionFormat.entries[handle.get<Int>(FORMAT) ?: 0],
            ),
            enteredDigits = handle[INPUT] ?: "",
            hadMistake = handle[MISTAKE] ?: false,
            interactiveStartedAtMillis = handle.get<Long>(STARTED)?.takeIf { it >= 0 },
            completedResponses = handle.get<ArrayList<String>>(RESPONSES).orEmpty().map(::decodeResponse),
            phase = QuizPhase.entries[handle.get<Int>(PHASE) ?: 0],
        )
    }

    fun save(handle: SavedStateHandle, state: QuizState) {
        handle[VERSION] = 1
        handle[TABLES] = ArrayList(state.selectedTables)
        handle[REMAINING] = state.remainingSeconds
        handle[POOL] = ArrayList(state.pool.remainingFacts.map(::encodeFact))
        handle[LAST_FACT] = state.pool.lastDrawnFact?.let(::encodeFact) ?: 0
        handle[QUESTION] = encodeFact(state.currentQuestion.fact)
        handle[FORMAT] = state.currentQuestion.format.ordinal
        handle[INPUT] = state.enteredDigits
        handle[MISTAKE] = state.hadMistake
        handle[STARTED] = state.interactiveStartedAtMillis ?: -1L
        handle[RESPONSES] = ArrayList(state.completedResponses.map(::encodeResponse))
        handle[PHASE] = state.phase.ordinal
    }

    private fun encodeFact(fact: MultiplicationFact) = fact.table * 100 + fact.firstOperand
    private fun decodeFact(value: Int) = MultiplicationFact(value % 100, value / 100)
    private fun encodeResponse(response: CompletedResponse) = listOf(
        encodeFact(response.fact), response.format.ordinal, response.expectedAnswer,
        if (response.hadMistake) 1 else 0, response.responseTimeMillis,
    ).joinToString(":")

    private fun decodeResponse(value: String): CompletedResponse {
        val parts = value.split(':')
        val fact = decodeFact(parts[0].toInt())
        return CompletedResponse(
            fact, fact.table, QuestionFormat.entries[parts[1].toInt()], parts[2].toInt(),
            parts[3] == "1", parts[4].toLong(),
        )
    }
}
