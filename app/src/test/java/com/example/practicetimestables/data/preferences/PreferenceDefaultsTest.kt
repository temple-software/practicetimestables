package com.example.practicetimestables.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class PreferenceDefaultsTest {
    @Test
    fun defaultSelectionContainsTwoThroughFive() {
        assertEquals(setOf(2, 3, 4, 5), PreferenceDefaults.selectedTables)
    }

    @Test
    fun normalizationSortsAndRemovesUnsupportedTables() {
        assertEquals(setOf(2, 7, 12), PreferenceDefaults.normalizeTables(setOf(12, 1, 7, 13, 2)))
    }

    @Test
    fun emptySelectionFallsBackToDefaults() {
        assertEquals(PreferenceDefaults.selectedTables, PreferenceDefaults.normalizeTables(emptySet()))
    }

    @Test
    fun systemLanguageUsesFrenchOnlyForFrenchLocale() {
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromSystem(java.util.Locale.CANADA_FRENCH))
        assertEquals(AppLanguage.ENGLISH_UK, AppLanguage.fromSystem(java.util.Locale.GERMANY))
    }
}
