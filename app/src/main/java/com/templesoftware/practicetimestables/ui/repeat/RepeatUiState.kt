package com.templesoftware.practicetimestables.ui.repeat

import com.templesoftware.practicetimestables.data.preferences.PreferenceDefaults

class RepeatUiState private constructor(
    val selectedTables: List<Int>,
    val activeTableIndex: Int,
    val currentItemIndex: Int,
    val answerVisible: Boolean,
    val completed: Boolean,
    val isScrolling: Boolean,
) {
    init {
        require(selectedTables.isNotEmpty())
        require(activeTableIndex in selectedTables.indices)
        require(currentItemIndex in INSTRUCTION_INDEX..LAST_ITEM_INDEX)
        require(!answerVisible || currentItemIndex > INSTRUCTION_INDEX)
        require(!completed || currentItemIndex == LAST_ITEM_INDEX)
        require(!isScrolling || !completed)
    }

    val activeTable: Int get() = selectedTables[activeTableIndex]
    val hasNextTable: Boolean get() = activeTableIndex < selectedTables.lastIndex
    val currentItem: RepeatCarouselItem get() = repeatCarouselItem(currentItemIndex)
    val multiplier: Int? get() = (currentItem as? RepeatCarouselItem.Fact)?.multiplier
    val answer: Int? get() = multiplier?.times(activeTable)

    fun revealAnswer(): RepeatUiState =
        if (completed || isScrolling || currentItemIndex == INSTRUCTION_INDEX || answerVisible) this
        else with(answerVisible = true)

    fun beginScroll(): RepeatUiState =
        if (completed || isScrolling || currentItemIndex >= LAST_ITEM_INDEX) this
        else if (currentItemIndex == INSTRUCTION_INDEX || answerVisible) with(isScrolling = true)
        else this

    fun finishScroll(): RepeatUiState =
        if (!isScrolling) this
        else with(
            currentItemIndex = currentItemIndex + 1,
            answerVisible = false,
            isScrolling = false,
        )

    fun completeTable(): RepeatUiState =
        if (completed || isScrolling || currentItemIndex != LAST_ITEM_INDEX || !answerVisible) this
        else with(completed = true)

    fun advanceTable(): RepeatUiState =
        if (!completed || !hasNextTable) this
        else newSession(selectedTables, activeTableIndex + 1)

    private fun with(
        currentItemIndex: Int = this.currentItemIndex,
        answerVisible: Boolean = this.answerVisible,
        completed: Boolean = this.completed,
        isScrolling: Boolean = this.isScrolling,
    ) = RepeatUiState(
        selectedTables = selectedTables,
        activeTableIndex = activeTableIndex,
        currentItemIndex = currentItemIndex,
        answerVisible = answerVisible,
        completed = completed,
        isScrolling = isScrolling,
    )

    companion object {
        const val INSTRUCTION_INDEX = 0
        const val FIRST_ITEM_INDEX = 1
        const val LAST_ITEM_INDEX = 12

        fun newSession(selectedTables: Set<Int>): RepeatUiState {
            val ordered = PreferenceDefaults.normalizeTables(selectedTables).sorted()
            return newSession(ordered, activeTableIndex = 0)
        }

        fun restore(
            selectedTables: Set<Int>,
            activeTableIndex: Int,
            currentItemIndex: Int,
            answerVisible: Boolean,
            completed: Boolean,
            isScrolling: Boolean,
        ): RepeatUiState {
            val ordered = PreferenceDefaults.normalizeTables(selectedTables).sorted()
            val safeTableIndex = activeTableIndex.coerceIn(ordered.indices)
            val safeItemIndex = currentItemIndex.coerceIn(INSTRUCTION_INDEX, LAST_ITEM_INDEX)
            val safeCompleted = completed && safeItemIndex == LAST_ITEM_INDEX
            return RepeatUiState(
                selectedTables = ordered,
                activeTableIndex = safeTableIndex,
                currentItemIndex = safeItemIndex,
                answerVisible = safeItemIndex > INSTRUCTION_INDEX && (answerVisible || safeCompleted),
                completed = safeCompleted,
                isScrolling = isScrolling && !safeCompleted && safeItemIndex < LAST_ITEM_INDEX,
            )
        }

        private fun newSession(selectedTables: List<Int>, activeTableIndex: Int) = RepeatUiState(
            selectedTables = selectedTables,
            activeTableIndex = activeTableIndex,
            currentItemIndex = INSTRUCTION_INDEX,
            answerVisible = false,
            completed = false,
            isScrolling = false,
        )
    }
}
