package com.templesoftware.practicetimestables.ui.home

import com.templesoftware.practicetimestables.data.preferences.PreferenceDefaults

val TimesTableGrid: List<List<Int?>> = listOf(
    listOf(null, 2, 3, 4, 5),
    listOf(6, 7, 8, 9, 10),
    listOf(11, 12, null, null, null),
)

fun toggleTableSelection(selectedTables: Set<Int>, table: Int): Set<Int> {
    if (table !in PreferenceDefaults.validTables) return selectedTables
    if (table in selectedTables && selectedTables.size == 1) return selectedTables

    return if (table in selectedTables) {
        selectedTables - table
    } else {
        selectedTables + table
    }
}
