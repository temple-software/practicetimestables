package com.example.practicetimestables.domain.quiz

const val QUIZ_BASE_SECONDS = 120
const val QUIZ_EXTRA_TABLE_SECONDS = 24
const val MIN_MULTIPLIER = 1
const val MAX_MULTIPLIER = 12
const val MIN_TABLE = 2
const val MAX_TABLE = 12

fun quizDurationSeconds(selectedTableCount: Int): Int {
    require(selectedTableCount > 0) { "A quiz requires at least one selected table" }
    return QUIZ_BASE_SECONDS + (selectedTableCount - 1) * QUIZ_EXTRA_TABLE_SECONDS
}

data class MultiplicationFact(
    val firstOperand: Int,
    val table: Int,
) {
    init {
        require(firstOperand in MIN_MULTIPLIER..MAX_MULTIPLIER)
        require(table in MIN_TABLE..MAX_TABLE)
    }

    val product: Int get() = firstOperand * table
}

enum class QuestionFormat {
    RESULT_MISSING,
    FIRST_OPERAND_MISSING,
    SECOND_OPERAND_MISSING,
}

data class QuizQuestion(
    val fact: MultiplicationFact,
    val format: QuestionFormat,
) {
    val expectedAnswer: Int get() = when (format) {
        QuestionFormat.RESULT_MISSING -> fact.product
        QuestionFormat.FIRST_OPERAND_MISSING -> fact.firstOperand
        QuestionFormat.SECOND_OPERAND_MISSING -> fact.table
    }
}

data class CompletedResponse(
    val fact: MultiplicationFact,
    val selectedTable: Int,
    val format: QuestionFormat,
    val expectedAnswer: Int,
    val hadMistake: Boolean,
    val responseTimeMillis: Long,
) {
    val accurate: Boolean get() = !hadMistake
}

enum class QuizPhase { ANSWERING, AWAITING_ADVANCE, READY_TO_FINISH }

data class QuizState(
    val selectedTables: List<Int>,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val pool: QuestionPool,
    val currentQuestion: QuizQuestion,
    val enteredDigits: String = "",
    val hadMistake: Boolean = false,
    val interactiveStartedAtMillis: Long? = null,
    val completedResponses: List<CompletedResponse> = emptyList(),
    val phase: QuizPhase = QuizPhase.ANSWERING,
) {
    init {
        require(selectedTables.isNotEmpty())
        require(selectedTables == selectedTables.distinct().sorted())
        require(selectedTables.all { it in MIN_TABLE..MAX_TABLE })
        require(totalSeconds == quizDurationSeconds(selectedTables.size))
        require(remainingSeconds in 0..totalSeconds)
        require(enteredDigits.all(Char::isDigit))
        require(currentQuestion.fact.table in selectedTables)
        require(phase == QuizPhase.ANSWERING || enteredDigits.isEmpty())
    }

    val isFirstHalf: Boolean get() = remainingSeconds * 2 > totalSeconds
    val timeExpired: Boolean get() = remainingSeconds == 0
    val isInteractive: Boolean
        get() = phase == QuizPhase.ANSWERING && interactiveStartedAtMillis != null
}

sealed interface QuizAction {
    data class QuestionBecameInteractive(val timestampMillis: Long) : QuizAction
    data class DigitPressed(val digit: Int, val timestampMillis: Long) : QuizAction
    data object Clear : QuizAction
    data class ElapseSeconds(val seconds: Int = 1) : QuizAction
    data object Advance : QuizAction
}

sealed interface QuizEvent {
    data object None : QuizEvent
    data object QuestionIsInteractive : QuizEvent
    data class DigitAccepted(val enteredDigits: String) : QuizEvent
    data object IncorrectDigit : QuizEvent
    data object InputCleared : QuizEvent
    data class QuestionCompleted(val response: CompletedResponse) : QuizEvent
    data object Advanced : QuizEvent
    data object ReadyToFinish : QuizEvent
}

data class QuizTransition(val state: QuizState, val event: QuizEvent)
