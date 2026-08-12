package com.templesoftware.practicetimestables.ui.app

import com.templesoftware.practicetimestables.data.preferences.AppLanguage
import com.templesoftware.practicetimestables.data.preferences.PreferenceDefaults

data class ApplicationUiState(
    val language: AppLanguage,
    val selectedTables: Set<Int> = PreferenceDefaults.selectedTables,
    val preferencesLoaded: Boolean = false,
)
