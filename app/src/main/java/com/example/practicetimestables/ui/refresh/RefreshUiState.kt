package com.example.practicetimestables.ui.refresh

import com.example.practicetimestables.data.preferences.PreferenceDefaults

class RefreshUiState private constructor(
    val selectedTables: List<Int>,
    val activeIndex: Int,
) {
    val activeTable: Int get() = selectedTables[activeIndex]
    val hasNextTable: Boolean get() = activeIndex < selectedTables.lastIndex
    val position: Int get() = activeIndex + 1

    fun advance(): RefreshUiState =
        if (hasNextTable) RefreshUiState(selectedTables, activeIndex + 1) else this

    companion object {
        fun create(
            selectedTables: Set<Int>,
            restoredActiveTable: Int? = null,
        ): RefreshUiState {
            val orderedTables = PreferenceDefaults.normalizeTables(selectedTables).sorted()
            val restoredIndex = restoredActiveTable
                ?.let(orderedTables::indexOf)
                ?.takeIf { it >= 0 }
                ?: 0
            return RefreshUiState(orderedTables, restoredIndex)
        }
    }
}
