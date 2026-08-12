package com.templesoftware.practicetimestables.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TableSelectionTest {
    @Test
    fun gridMatchesTheRequiredThreeByFiveLayout() {
        assertEquals(
            listOf(
                listOf(null, 2, 3, 4, 5),
                listOf(6, 7, 8, 9, 10),
                listOf(11, 12, null, null, null),
            ),
            TimesTableGrid,
        )
    }

    @Test
    fun everyGridRowHasExactlyFiveColumns() {
        assertEquals(listOf(5, 5, 5), TimesTableGrid.map { it.size })
    }

    @Test
    fun gridContainsEverySelectableTableExactlyOnce() {
        assertEquals((2..12).toList(), TimesTableGrid.flatten().filterNotNull().sorted())
    }

    @Test
    fun tappingAnUnselectedTableSelectsIt() {
        assertEquals(setOf(2, 3, 7), toggleTableSelection(setOf(2, 3), 7))
    }

    @Test
    fun tappingASelectedTableDeselectsItWhenAnotherRemains() {
        assertEquals(setOf(3), toggleTableSelection(setOf(2, 3), 2))
    }

    @Test
    fun tappingTheFinalSelectedTableDoesNotDeselectIt() {
        val selection = setOf(8)

        assertSame(selection, toggleTableSelection(selection, 8))
    }

    @Test
    fun unsupportedTableDoesNotChangeSelection() {
        val selection = setOf(2, 3)

        assertSame(selection, toggleTableSelection(selection, 13))
    }
}
