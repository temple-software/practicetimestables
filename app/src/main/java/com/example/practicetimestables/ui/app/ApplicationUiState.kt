package com.example.practicetimestables.ui.app

import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.data.preferences.PreferenceDefaults

data class ApplicationUiState(
    val language: AppLanguage,
    val selectedTables: Set<Int> = PreferenceDefaults.selectedTables,
    val preferencesLoaded: Boolean = false,
)
