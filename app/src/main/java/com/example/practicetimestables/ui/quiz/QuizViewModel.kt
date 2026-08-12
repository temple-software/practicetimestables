package com.example.practicetimestables.ui.quiz

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.practicetimestables.domain.quiz.QuizAction
import com.example.practicetimestables.domain.quiz.QuizEngine
import com.example.practicetimestables.domain.quiz.QuizPhase
import com.example.practicetimestables.domain.quiz.QuizState
import com.example.practicetimestables.ui.theme.AppMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

fun interface QuizClock { fun nowMillis(): Long }

class QuizViewModel(
    private val savedStateHandle: SavedStateHandle,
    selectedTables: Set<Int>,
    private val engine: QuizEngine = QuizEngine(),
    private val clock: QuizClock = QuizClock(SystemClock::elapsedRealtime),
    private val automaticJobsEnabled: Boolean = true,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        QuizSavedState.restore(savedStateHandle) ?: engine.newSession(selectedTables),
    )
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()
    private var timerJob: Job? = null
    private var advanceJob: Job? = null

    init {
        QuizSavedState.save(savedStateHandle, _uiState.value)
        if (_uiState.value.interactiveStartedAtMillis != null && !_uiState.value.timeExpired) startTimer()
        if (_uiState.value.phase == QuizPhase.AWAITING_ADVANCE) scheduleAdvance()
    }

    fun questionBecameInteractive() {
        if (_uiState.value.phase != QuizPhase.ANSWERING || _uiState.value.isInteractive) return
        update(engine.dispatch(_uiState.value, QuizAction.QuestionBecameInteractive(clock.nowMillis())).state)
        if (savedStateHandle.get<Long>(QuizSavedState.DEADLINE) == null) {
            savedStateHandle[QuizSavedState.DEADLINE] = clock.nowMillis() + _uiState.value.remainingSeconds * 1_000L
        }
        startTimer()
    }

    fun pressDigit(digit: Int) {
        val next = engine.dispatch(_uiState.value, QuizAction.DigitPressed(digit, clock.nowMillis())).state
        update(next)
        if (next.phase == QuizPhase.AWAITING_ADVANCE) scheduleAdvance()
    }

    fun clear() = update(engine.dispatch(_uiState.value, QuizAction.Clear).state)

    fun advanceAfterCorrectTransition() {
        if (_uiState.value.phase != QuizPhase.AWAITING_ADVANCE) return
        update(engine.dispatch(_uiState.value, QuizAction.Advance).state)
    }

    internal fun synchronizeCountdown() {
        val deadline = savedStateHandle.get<Long>(QuizSavedState.DEADLINE) ?: return
        val target = ceil((deadline - clock.nowMillis()).coerceAtLeast(0) / 1_000.0).toInt()
        val elapsed = (_uiState.value.remainingSeconds - target).coerceAtLeast(0)
        if (elapsed > 0) update(engine.dispatch(_uiState.value, QuizAction.ElapseSeconds(elapsed)).state)
    }

    private fun startTimer() {
        if (!automaticJobsEnabled) return
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (!_uiState.value.timeExpired && _uiState.value.phase != QuizPhase.READY_TO_FINISH) {
                synchronizeCountdown()
                delay(200)
            }
        }
    }

    private fun scheduleAdvance() {
        if (!automaticJobsEnabled) return
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(AppMotion.DefaultDurationMillis.toLong())
            advanceAfterCorrectTransition()
        }
    }

    private fun update(state: QuizState) {
        _uiState.value = state
        QuizSavedState.save(savedStateHandle, state)
    }

    companion object {
        fun factory(selectedTables: Set<Int>) = viewModelFactory {
            initializer { QuizViewModel(createSavedStateHandle(), selectedTables) }
        }
    }
}
