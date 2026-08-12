package com.templesoftware.practicetimestables.domain.quiz

class QuizTestRandomizer(
    private val formatIndices: MutableList<Int> = mutableListOf(0),
    private val reverseShuffle: Boolean = false,
) : QuizRandomizer {
    override fun shuffle(facts: List<MultiplicationFact>): List<MultiplicationFact> =
        if (reverseShuffle) facts.reversed() else facts

    override fun nextFormatIndex(bound: Int): Int {
        val value = if (formatIndices.size > 1) formatIndices.removeAt(0) else formatIndices.first()
        return value.mod(bound)
    }
}
