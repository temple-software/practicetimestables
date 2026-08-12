package com.example.practicetimestables.domain.quiz

import kotlin.random.Random

interface QuizRandomizer {
    fun shuffle(facts: List<MultiplicationFact>): List<MultiplicationFact>
    fun nextFormatIndex(bound: Int): Int
}

class KotlinQuizRandomizer(private val random: Random = Random.Default) : QuizRandomizer {
    override fun shuffle(facts: List<MultiplicationFact>): List<MultiplicationFact> =
        facts.shuffled(random)

    override fun nextFormatIndex(bound: Int): Int = random.nextInt(bound)
}

data class QuestionPool(
    val eligibleFacts: List<MultiplicationFact>,
    val remainingFacts: List<MultiplicationFact>,
    val lastDrawnFact: MultiplicationFact? = null,
) {
    init {
        require(eligibleFacts.isNotEmpty())
        require(eligibleFacts.distinct().size == eligibleFacts.size)
        require(remainingFacts.all(eligibleFacts::contains))
        require(remainingFacts.distinct().size == remainingFacts.size)
    }

    data class Draw(val fact: MultiplicationFact, val pool: QuestionPool)

    fun draw(randomizer: QuizRandomizer): Draw {
        val available = if (remainingFacts.isNotEmpty()) {
            remainingFacts
        } else {
            avoidCycleBoundaryRepeat(randomizer.shuffle(eligibleFacts), lastDrawnFact)
        }
        val fact = available.first()
        return Draw(
            fact = fact,
            pool = copy(remainingFacts = available.drop(1), lastDrawnFact = fact),
        )
    }

    companion object {
        fun create(selectedTables: List<Int>, randomizer: QuizRandomizer): QuestionPool {
            require(selectedTables.isNotEmpty())
            val facts = selectedTables.flatMap { table ->
                (MIN_MULTIPLIER..MAX_MULTIPLIER).map { multiplier ->
                    MultiplicationFact(multiplier, table)
                }
            }
            return QuestionPool(
                eligibleFacts = facts,
                remainingFacts = randomizer.shuffle(facts),
            )
        }

        private fun avoidCycleBoundaryRepeat(
            shuffled: List<MultiplicationFact>,
            previous: MultiplicationFact?,
        ): List<MultiplicationFact> {
            if (previous == null || shuffled.first() != previous) return shuffled
            val replacementIndex = shuffled.indexOfFirst { it != previous }
            check(replacementIndex > 0) { "A quiz pool must contain more than one distinct fact" }
            return shuffled.toMutableList().apply {
                this[0] = shuffled[replacementIndex]
                this[replacementIndex] = shuffled[0]
            }
        }
    }
}
