package com.templesoftware.practicetimestables.ui.repeat

sealed interface RepeatCarouselItem {
    data object Instruction : RepeatCarouselItem
    data class Fact(val multiplier: Int) : RepeatCarouselItem {
        init { require(multiplier in 1..RepeatUiState.LAST_ITEM_INDEX) }
    }
}

data class RepeatCarouselWindow(
    val previous: RepeatCarouselItem?,
    val current: RepeatCarouselItem,
    val next: RepeatCarouselItem?,
)

fun repeatCarouselItem(index: Int): RepeatCarouselItem = when (index) {
    RepeatUiState.INSTRUCTION_INDEX -> RepeatCarouselItem.Instruction
    in RepeatUiState.FIRST_ITEM_INDEX..RepeatUiState.LAST_ITEM_INDEX ->
        RepeatCarouselItem.Fact(index)
    else -> throw IllegalArgumentException("Invalid Repeat carousel index: $index")
}

fun repeatCarouselWindow(currentIndex: Int): RepeatCarouselWindow {
    val current = repeatCarouselItem(currentIndex)
    return RepeatCarouselWindow(
        previous = if (currentIndex > RepeatUiState.INSTRUCTION_INDEX) {
            repeatCarouselItem(currentIndex - 1)
        } else null,
        current = current,
        next = if (currentIndex < RepeatUiState.LAST_ITEM_INDEX) {
            repeatCarouselItem(currentIndex + 1)
        } else null,
    )
}
