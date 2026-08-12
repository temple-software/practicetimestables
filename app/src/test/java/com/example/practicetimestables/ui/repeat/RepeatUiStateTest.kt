package com.example.practicetimestables.ui.repeat

import com.example.practicetimestables.ui.theme.AppMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RepeatUiStateTest {
    @Test
    fun educationalTimingDefaultsArePreserved() {
        assertEquals(2_000, AppMotion.RepeatPromptDelayMillis)
        assertEquals(500, AppMotion.RepeatAnswerFadeDurationMillis)
        assertEquals(1_500, AppMotion.RepeatAnswerHoldDurationMillis)
        assertEquals(600, AppMotion.RepeatCarouselDurationMillis)
    }

    @Test
    fun newSessionStartsAtInstructionForLowestSelectedTable() {
        val state = RepeatUiState.newSession(setOf(12, 5, 2))

        assertEquals(listOf(2, 5, 12), state.selectedTables)
        assertEquals(0, state.activeTableIndex)
        assertEquals(2, state.activeTable)
        assertEquals(0, state.currentItemIndex)
        assertEquals(RepeatCarouselItem.Instruction, state.currentItem)
        assertNull(state.multiplier)
        assertFalse(state.answerVisible)
        assertFalse(state.completed)
    }

    @Test
    fun instructionScrollAdvancesExactlyOnceToFirstFact() {
        val initial = RepeatUiState.newSession(setOf(2))
        val scrolling = initial.beginScroll()
        val first = scrolling.finishScroll()

        assertTrue(scrolling.isScrolling)
        assertEquals(0, scrolling.currentItemIndex)
        assertEquals(1, first.currentItemIndex)
        assertEquals(1, first.multiplier)
        assertFalse(first.answerVisible)
        assertSame(first, first.finishScroll())
    }

    @Test
    fun logicalIndexChangesOnlyWhenVisualScrollIsFinished() {
        val first = firstFact(setOf(4)).revealAnswer()
        val scrolling = first.beginScroll()

        assertEquals(1, first.currentItemIndex)
        assertEquals(1, scrolling.currentItemIndex)
        assertTrue(scrolling.isScrolling)
        val settled = scrolling.finishScroll()
        assertEquals(2, settled.currentItemIndex)
        assertFalse(settled.isScrolling)
        assertFalse(settled.answerVisible)
        assertSame(settled, settled.finishScroll())
    }

    @Test
    fun multiplicationItemRequiresAnswerBeforeItCanScroll() {
        val first = firstFact(setOf(3))

        assertSame(first, first.beginScroll())
        val answered = first.revealAnswer()
        assertTrue(answered.answerVisible)
        assertTrue(answered.beginScroll().isScrolling)
    }

    @Test
    fun instructionCanNeverRevealAnAnswer() {
        val instruction = RepeatUiState.newSession(setOf(7))

        assertSame(instruction, instruction.revealAnswer())
        assertFalse(instruction.answerVisible)
        assertNull(instruction.answer)
    }

    @Test
    fun multipliersAdvanceExactlyOneThroughTwelveAndResetAnswer() {
        var state = firstFact(setOf(8))
        val visited = mutableListOf<Int>()
        while (state.currentItemIndex < 12) {
            visited += checkNotNull(state.multiplier)
            state = state.revealAnswer().beginScroll().finishScroll()
            assertFalse(state.answerVisible)
        }
        visited += checkNotNull(state.multiplier)

        assertEquals((1..12).toList(), visited)
        assertEquals(0, state.activeTableIndex)
    }

    @Test
    fun twelfthFactCompletesAndCannotAdvanceBeyondTwelve() {
        var state = firstFact(setOf(12))
        repeat(11) { state = state.revealAnswer().beginScroll().finishScroll() }

        assertEquals(12, state.multiplier)
        assertFalse(state.answerVisible)
        assertEquals(144, state.answer)
        assertSame(state, state.beginScroll())
        val revealed = state.revealAnswer()
        assertTrue(revealed.answerVisible)
        val completed = revealed.completeTable()
        assertTrue(completed.completed)
        assertEquals(12, completed.currentItemIndex)
        assertSame(completed, completed.beginScroll())
        assertSame(completed, completed.completeTable())
    }

    @Test
    fun nextTableAdvancesOnceAndResetsToInstruction() {
        val completed = completeCurrentTable(RepeatUiState.newSession(setOf(12, 2, 5)))
        val next = completed.advanceTable()

        assertEquals(1, next.activeTableIndex)
        assertEquals(5, next.activeTable)
        assertEquals(0, next.currentItemIndex)
        assertEquals(RepeatCarouselItem.Instruction, next.currentItem)
        assertFalse(next.answerVisible)
        assertFalse(next.completed)
    }

    @Test
    fun finalSelectedTableProducesLetsGoAndDoesNotReset() {
        val firstCompleted = completeCurrentTable(RepeatUiState.newSession(setOf(2, 5)))
        val finalCompleted = completeCurrentTable(firstCompleted.advanceTable())

        assertFalse(finalCompleted.hasNextTable)
        assertSame(finalCompleted, finalCompleted.advanceTable())
        assertEquals(5, finalCompleted.activeTable)
        assertEquals(12, finalCompleted.currentItemIndex)
    }

    @Test
    fun fullNonConsecutiveSessionVisitsEveryStateThroughFinalCompletion() {
        var state = RepeatUiState.newSession(setOf(12, 2, 5))
        val visited = mutableListOf<Pair<Int, Int>>()
        while (true) {
            visited += state.activeTable to state.currentItemIndex
            state = when {
                state.currentItemIndex == 0 -> state.beginScroll().finishScroll()
                state.currentItemIndex < 12 -> state.revealAnswer().beginScroll().finishScroll()
                else -> state.revealAnswer().completeTable()
            }
            if (state.completed) {
                if (!state.hasNextTable) break
                state = state.advanceTable()
            }
        }

        val expected = listOf(2, 5, 12).flatMap { table -> (0..12).map { table to it } }
        assertEquals(expected, visited)
        assertEquals(12, state.activeTable)
        assertEquals(12, state.multiplier)
        assertEquals(144, state.answer)
        assertTrue(state.completed)
    }

    private fun firstFact(selectedTables: Set<Int>): RepeatUiState =
        RepeatUiState.newSession(selectedTables).beginScroll().finishScroll()

    private fun completeCurrentTable(start: RepeatUiState): RepeatUiState {
        var state = start
        if (state.currentItemIndex == 0) state = state.beginScroll().finishScroll()
        while (state.currentItemIndex < 12) {
            state = state.revealAnswer().beginScroll().finishScroll()
        }
        return state.revealAnswer().completeTable()
    }
}
