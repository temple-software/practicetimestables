package com.example.practicetimestables.ui.refresh

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RefreshViewModel(
    private val savedStateHandle: SavedStateHandle,
    selectedTables: Set<Int>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RefreshUiState.create(
            selectedTables = selectedTables,
            restoredActiveTable = savedStateHandle[ACTIVE_TABLE_KEY],
        ),
    )
    val uiState: StateFlow<RefreshUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle[ACTIVE_TABLE_KEY] = _uiState.value.activeTable
    }

    fun advance() {
        val nextState = _uiState.value.advance()
        if (nextState == _uiState.value) return

        _uiState.value = nextState
        savedStateHandle[ACTIVE_TABLE_KEY] = nextState.activeTable
    }

    companion object {
        private const val ACTIVE_TABLE_KEY = "refresh_active_table"

        fun factory(selectedTables: Set<Int>) = viewModelFactory {
            initializer {
                RefreshViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    selectedTables = selectedTables,
                )
            }
        }
    }
}
