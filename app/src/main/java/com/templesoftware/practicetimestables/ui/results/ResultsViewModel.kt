package com.templesoftware.practicetimestables.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import com.templesoftware.practicetimestables.domain.quiz.CompletedResponse
import com.templesoftware.practicetimestables.domain.quiz.MultiplicationFact
import com.templesoftware.practicetimestables.domain.quiz.QuestionFormat
import com.templesoftware.practicetimestables.domain.results.QuizResults
import com.templesoftware.practicetimestables.domain.results.QuizScoringEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ResultsUiState(
    val results: QuizResults,
) {
    val showTableBreakdown: Boolean get() = results.tableScores.size > 1
}

class ResultsViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val restoredResponses = runCatching {
        savedStateHandle
            .get<ArrayList<String>>(RESPONSES)
            .orEmpty()
            .map(::decodeResponse)
    }.getOrDefault(emptyList())
    private val _uiState = MutableStateFlow(
        restoredResponses.takeIf(List<CompletedResponse>::isNotEmpty)?.let {
            ResultsUiState(QuizScoringEngine.score(it))
        },
    )
    val uiState: StateFlow<ResultsUiState?> = _uiState.asStateFlow()
    private var fanfareClaimed: Boolean
        get() = savedStateHandle[FANFARE_CLAIMED] ?: false
        set(value) { savedStateHandle[FANFARE_CLAIMED] = value }

    fun acceptCompletedQuiz(responses: List<CompletedResponse>) {
        require(responses.isNotEmpty()) { "Results require at least one completed response" }
        _uiState.value = ResultsUiState(QuizScoringEngine.score(responses.toList()))
        savedStateHandle[RESPONSES] = ArrayList(responses.map(::encodeResponse))
        fanfareClaimed = false
    }

    fun claimFanfare(): Boolean {
        if (_uiState.value == null || fanfareClaimed) return false
        fanfareClaimed = true
        return true
    }

    fun clearCompletedQuiz() {
        _uiState.value = null
        savedStateHandle.remove<ArrayList<String>>(RESPONSES)
        fanfareClaimed = false
    }

    private fun encodeResponse(response: CompletedResponse): String = listOf(
        response.selectedTable,
        if (response.accurate) 1 else 0,
        response.responseTimeMillis,
    ).joinToString(ENCODING_SEPARATOR)

    private fun decodeResponse(encoded: String): CompletedResponse {
        val parts = encoded.split(ENCODING_SEPARATOR)
        require(parts.size == 3) { "Invalid saved Results response" }
        val table = parts[0].toInt()
        return CompletedResponse(
            fact = MultiplicationFact(firstOperand = 1, table = table),
            selectedTable = table,
            format = QuestionFormat.RESULT_MISSING,
            expectedAnswer = table,
            hadMistake = parts[1] != "1",
            responseTimeMillis = parts[2].toLong(),
        )
    }

    private companion object {
        const val RESPONSES = "results_completed_responses"
        const val FANFARE_CLAIMED = "results_fanfare_claimed"
        const val ENCODING_SEPARATOR = ":"
    }
}
