package com.example.practicetimestables.ui.repeat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.practicetimestables.ui.theme.AppMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RepeatViewModel(
    private val savedStateHandle: SavedStateHandle,
    selectedTables: Set<Int>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        if (savedStateHandle.contains(ITEM_INDEX_KEY)) {
            RepeatUiState.restore(
                selectedTables = selectedTables,
                activeTableIndex = savedStateHandle[ACTIVE_TABLE_INDEX_KEY] ?: 0,
                currentItemIndex = savedStateHandle[ITEM_INDEX_KEY] ?: 0,
                answerVisible = savedStateHandle[ANSWER_VISIBLE_KEY] ?: false,
                completed = savedStateHandle[COMPLETED_KEY] ?: false,
                isScrolling = savedStateHandle[SCROLLING_KEY] ?: false,
            )
        } else {
            RepeatUiState.newSession(selectedTables)
        },
    )
    val uiState: StateFlow<RepeatUiState> = _uiState.asStateFlow()
    private var sequenceJob: Job? = null

    init {
        persist(_uiState.value)
        startSequence()
    }

    fun advanceTable() {
        val next = _uiState.value.advanceTable()
        if (next == _uiState.value) return
        sequenceJob?.cancel()
        updateState(next)
        startSequence()
    }

    private fun startSequence() {
        if (_uiState.value.completed) return
        sequenceJob?.cancel()
        sequenceJob = viewModelScope.launch {
            if (_uiState.value.isScrolling) finishPendingScroll()
            while (!_uiState.value.completed) {
                val state = _uiState.value
                if (state.currentItemIndex == RepeatUiState.INSTRUCTION_INDEX) {
                    delay(AppMotion.RepeatPromptDelayMillis.toLong())
                    scrollOnce()
                } else {
                    if (!state.answerVisible) {
                        delay(AppMotion.RepeatPromptDelayMillis.toLong())
                        updateState(_uiState.value.revealAnswer())
                        delay(AppMotion.RepeatAnswerFadeDurationMillis.toLong())
                    }
                    delay(AppMotion.RepeatAnswerHoldDurationMillis.toLong())
                    if (_uiState.value.currentItemIndex == RepeatUiState.LAST_ITEM_INDEX) {
                        updateState(_uiState.value.completeTable())
                    } else {
                        scrollOnce()
                    }
                }
            }
        }
    }

    private suspend fun scrollOnce() {
        val scrolling = _uiState.value.beginScroll()
        check(scrolling.isScrolling) { "Repeat attempted an invalid carousel advance" }
        updateState(scrolling)
        finishPendingScroll()
    }

    private suspend fun finishPendingScroll() {
        delay(AppMotion.RepeatCarouselDurationMillis.toLong())
        updateState(_uiState.value.finishScroll())
    }

    private fun updateState(state: RepeatUiState) {
        _uiState.value = state
        persist(state)
    }

    private fun persist(state: RepeatUiState) {
        savedStateHandle[ACTIVE_TABLE_INDEX_KEY] = state.activeTableIndex
        savedStateHandle[ITEM_INDEX_KEY] = state.currentItemIndex
        savedStateHandle[ANSWER_VISIBLE_KEY] = state.answerVisible
        savedStateHandle[COMPLETED_KEY] = state.completed
        savedStateHandle[SCROLLING_KEY] = state.isScrolling
    }

    companion object {
        private const val ACTIVE_TABLE_INDEX_KEY = "repeat_active_table_index_v2"
        private const val ITEM_INDEX_KEY = "repeat_item_index_v2"
        private const val ANSWER_VISIBLE_KEY = "repeat_answer_visible_v2"
        private const val COMPLETED_KEY = "repeat_completed_v2"
        private const val SCROLLING_KEY = "repeat_scrolling_v2"

        fun factory(selectedTables: Set<Int>) = viewModelFactory {
            initializer {
                RepeatViewModel(createSavedStateHandle(), selectedTables)
            }
        }
    }
}
