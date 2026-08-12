package com.example.practicetimestables.ui.refresh

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshUiStateTest {
    @Test
    fun viewModelRestoresProgressFromSavedStateHandle() {
        val savedState = SavedStateHandle()
        val original = RefreshViewModel(savedState, setOf(3, 7, 10))
        original.advance()

        val restored = RefreshViewModel(savedState, setOf(3, 7, 10))

        assertEquals(7, restored.uiState.value.activeTable)
        assertEquals(2, restored.uiState.value.position)
    }

    @Test
    fun oneSelectedTableStartsFinalAndUsesRepeat() {
        val state = RefreshUiState.create(setOf(8))

        assertEquals(8, state.activeTable)
        assertFalse(state.hasNextTable)
    }

    @Test
    fun newSessionStartsWithTheLowestSelectedTable() {
        val state = RefreshUiState.create(setOf(9, 3, 7))

        assertEquals(3, state.activeTable)
        assertEquals(listOf(3, 7, 9), state.selectedTables)
    }

    @Test
    fun selectedTablesAreTraversedInAscendingOrder() {
        val first = RefreshUiState.create(setOf(12, 2, 8))
        val second = first.advance()
        val third = second.advance()

        assertEquals(listOf(2, 8, 12), listOf(first.activeTable, second.activeTable, third.activeTable))
    }

    @Test
    fun allTablesAreTraversedFromTwoThroughTwelve() {
        var state = RefreshUiState.create((2..12).toSet())
        val visited = mutableListOf<Int>()

        while (true) {
            visited += state.activeTable
            if (!state.hasNextTable) break
            state = state.advance()
        }

        assertEquals((2..12).toList(), visited)
    }

    @Test
    fun nextAdvancesToTheCorrectNonConsecutiveTable() {
        val next = RefreshUiState.create(setOf(2, 6, 11)).advance()

        assertEquals(6, next.activeTable)
        assertEquals(2, next.position)
    }

    @Test
    fun finalSelectedTableUsesRepeatAction() {
        val finalState = RefreshUiState.create(setOf(4, 9)).advance()

        assertEquals(9, finalState.activeTable)
        assertFalse(finalState.hasNextTable)
        assertSame(finalState, finalState.advance())
    }

    @Test
    fun nonFinalSelectedTableUsesNextAction() {
        assertTrue(RefreshUiState.create(setOf(4, 9)).hasNextTable)
    }

    @Test
    fun restoredSessionKeepsItsActiveSelectedTable() {
        val restored = RefreshUiState.create(
            selectedTables = setOf(3, 7, 10),
            restoredActiveTable = 7,
        )

        assertEquals(7, restored.activeTable)
        assertEquals(2, restored.position)
    }

    @Test
    fun newSessionAfterACompletedVisitStartsFromLowestAgain() {
        val selection = setOf(3, 7, 10)
        val previousSession = RefreshUiState.create(selection).advance().advance()
        val newSession = RefreshUiState.create(selection)

        assertEquals(10, previousSession.activeTable)
        assertEquals(3, newSession.activeTable)
    }

    @Test
    fun invalidRestoredTableFallsBackToLowestSelection() {
        val restored = RefreshUiState.create(setOf(5, 9), restoredActiveTable = 7)

        assertEquals(5, restored.activeTable)
    }
}
