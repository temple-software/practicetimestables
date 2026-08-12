package com.templesoftware.practicetimestables.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.templesoftware.practicetimestables.data.preferences.AppLanguage
import com.templesoftware.practicetimestables.data.preferences.UserPreferencesRepository
import com.templesoftware.practicetimestables.ui.home.toggleTableSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class ApplicationViewModel(
    private val repository: UserPreferencesRepository,
    initialLanguage: AppLanguage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplicationUiState(language = initialLanguage))
    val uiState: StateFlow<ApplicationUiState> = _uiState.asStateFlow()

    init {
        repository.preferences
        .map { preferences ->
            ApplicationUiState(
                language = preferences.language,
                selectedTables = preferences.selectedTables,
                preferencesLoaded = true,
            )
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun selectLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = language)
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun toggleTable(table: Int) {
        val updatedTables = toggleTableSelection(_uiState.value.selectedTables, table)
        if (updatedTables == _uiState.value.selectedTables) return

        _uiState.value = _uiState.value.copy(selectedTables = updatedTables)
        viewModelScope.launch { repository.setSelectedTables(updatedTables) }
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
