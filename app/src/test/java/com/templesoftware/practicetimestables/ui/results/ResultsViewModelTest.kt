package com.templesoftware.practicetimestables.ui.results

import androidx.lifecycle.SavedStateHandle
import com.templesoftware.practicetimestables.domain.quiz.CompletedResponse
import com.templesoftware.practicetimestables.domain.quiz.MultiplicationFact
import com.templesoftware.practicetimestables.domain.quiz.QuestionFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultsViewModelTest {
    @Test
    fun completedQuizResponsesProduceScoredResultsFromThatExactSession() {
        val viewModel = ResultsViewModel(SavedStateHandle())
        viewModel.acceptCompletedQuiz(
            listOf(response(5, false, 6_000), response(2, true, 900)),
        )

        val state = requireNotNull(viewModel.uiState.value)
        assertEquals(50.0, state.results.overallAccuracy.percentage, 0.0)
        assertEquals(2, state.results.overallAccuracy.stars)
        assertEquals(4, state.results.overallSpeed?.stars)
        assertEquals(listOf(2, 5), state.results.tableScores.map { it.table })
    }

    @Test
    fun oneTableSuppressesBreakdownAndMultipleTablesExposeIt() {
        val viewModel = ResultsViewModel(SavedStateHandle())
        viewModel.acceptCompletedQuiz(listOf(response(8, true, 1_000)))
        assertFalse(requireNotNull(viewModel.uiState.value).showTableBreakdown)

        viewModel.acceptCompletedQuiz(listOf(response(8, true, 1_000), response(3, true, 1_000)))
        assertTrue(requireNotNull(viewModel.uiState.value).showTableBreakdown)
    }

    @Test
    fun fanfareCanBeClaimedOnlyOnceForACompletedSession() {
        val viewModel = ResultsViewModel(SavedStateHandle())
        assertFalse(viewModel.claimFanfare())
        viewModel.acceptCompletedQuiz(listOf(response(2, true, 1_000)))

        assertTrue(viewModel.claimFanfare())
        assertFalse(viewModel.claimFanfare())
    }

    @Test
    fun clearingResultsAllowsANewQuizSession() {
        val viewModel = ResultsViewModel(SavedStateHandle())
        viewModel.acceptCompletedQuiz(listOf(response(2, true, 1_000)))

        viewModel.clearCompletedQuiz()

        assertNull(viewModel.uiState.value)
        assertFalse(viewModel.claimFanfare())
        viewModel.acceptCompletedQuiz(listOf(response(5, false, 3_000)))
        assertEquals(listOf(5), requireNotNull(viewModel.uiState.value).results.tableScores.map { it.table })
        assertTrue(viewModel.claimFanfare())
    }

    @Test
    fun completedResultsAndFanfareClaimSurviveSavedStateRecreation() {
        val handle = SavedStateHandle()
        val original = ResultsViewModel(handle)
        original.acceptCompletedQuiz(
            listOf(response(12, false, 5_500), response(2, true, 850), response(5, true, 1_900)),
        )
        val expected = requireNotNull(original.uiState.value)
        assertTrue(original.claimFanfare())

        val restored = ResultsViewModel(handle)

        val actual = requireNotNull(restored.uiState.value)
        assertEquals(expected.results, actual.results)
        assertEquals(listOf(2, 5, 12), actual.results.tableScores.map { it.table })
        assertTrue(actual.showTableBreakdown)
        assertFalse(restored.claimFanfare())
    }

    @Test
    fun malformedRestoredResponsesFailGracefullyInsteadOfCrashing() {
        val handle = SavedStateHandle(
            mapOf("results_completed_responses" to arrayListOf("not:a:valid:response")),
        )

        val restored = ResultsViewModel(handle)

        assertNull(restored.uiState.value)
        assertFalse(restored.claimFanfare())
    }

    private fun response(table: Int, accurate: Boolean, millis: Long) = CompletedResponse(
        fact = MultiplicationFact(1, table),
        selectedTable = table,
        format = QuestionFormat.RESULT_MISSING,
        expectedAnswer = table,
        hadMistake = !accurate,
        responseTimeMillis = millis,
    )
}
