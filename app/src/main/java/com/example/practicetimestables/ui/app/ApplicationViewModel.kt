package com.example.practicetimestables.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApplicationViewModel(
    private val repository: UserPreferencesRepository,
    initialLanguage: AppLanguage,
) : ViewModel() {
    val uiState: StateFlow<ApplicationUiState> = repository.preferences
        .map { preferences ->
            ApplicationUiState(
                language = preferences.language,
                selectedTables = preferences.selectedTables,
                preferencesLoaded = true,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApplicationUiState(language = initialLanguage),
        )

    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun selectTables(tables: Set<Int>) {
        viewModelScope.launch { repository.setSelectedTables(tables) }
    }

    class Factory(
        private val repository: UserPreferencesRepository,
        private val initialLanguage: AppLanguage,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ApplicationViewModel::class.java))
            return ApplicationViewModel(repository, initialLanguage) as T
        }
    }
}
