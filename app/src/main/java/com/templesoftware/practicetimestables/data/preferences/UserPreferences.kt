package com.templesoftware.practicetimestables.data.preferences

data class UserPreferences(
    val language: AppLanguage,
    val selectedTables: Set<Int>,
)

object PreferenceDefaults {
    val selectedTables: Set<Int> = setOf(2, 3, 4, 5)
    val validTables: IntRange = 2..12

    fun normalizeTables(tables: Set<Int>): Set<Int> =
        tables.filterTo(sortedSetOf()) { it in validTables }
            .ifEmpty { selectedTables }
}
