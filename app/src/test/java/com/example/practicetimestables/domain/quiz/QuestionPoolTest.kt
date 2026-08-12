package com.example.practicetimestables.domain.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionPoolTest {
    @Test
    fun durationUsesBasePlusTwentyFourSecondsPerExtraTable() {
        assertEquals(120, quizDurationSeconds(1))
        assertEquals(144, quizDurationSeconds(2))
        assertEquals(168, quizDurationSeconds(3))
        assertEquals(192, quizDurationSeconds(4))
        assertThrows(IllegalArgumentException::class.java) { quizDurationSeconds(0) }
    }

    @Test
    fun generatedFactsPreserveSelectedTableAsSecondOperand() {
        val pool = QuestionPool.create(listOf(2, 5), QuizTestRandomizer())

        assertEquals(24, pool.eligibleFacts.size)
        assertEquals(setOf(2, 5), pool.eligibleFacts.map { it.table }.toSet())
        assertEquals((1..12).toSet(), pool.eligibleFacts.filter { it.table == 5 }.map { it.firstOperand }.toSet())
        assertTrue(pool.eligibleFacts.all { it.product == it.firstOperand * it.table })
    }

    @Test
    fun eachFactAppearsExactlyOnceWithinPoolCycle() {
        val randomizer = QuizTestRandomizer(reverseShuffle = true)
        var pool = QuestionPool.create(listOf(2, 7), randomizer)
        val drawn = mutableListOf<MultiplicationFact>()

        repeat(24) {
            val draw = pool.draw(randomizer)
            drawn += draw.fact
            pool = draw.pool
        }

        assertEquals(24, drawn.distinct().size)
        assertEquals(pool.eligibleFacts.toSet(), drawn.toSet())
        assertTrue(pool.remainingFacts.isEmpty())
    }

    @Test
    fun nextCycleNeverRepeatsPreviousCyclesFinalFactAtBoundary() {
        var shuffleCount = 0
        val randomizer = object : QuizRandomizer {
            override fun shuffle(facts: List<MultiplicationFact>): List<MultiplicationFact> =
                if (shuffleCount++ == 0) facts else facts.reversed()

            override fun nextFormatIndex(bound: Int): Int = 0
        }
        var pool = QuestionPool.create(listOf(2), randomizer)
        var last = pool.eligibleFacts.first()
        repeat(12) {
            val draw = pool.draw(randomizer)
            last = draw.fact
            pool = draw.pool
        }
        val nextCycle = pool.draw(randomizer)

        assertNotEquals(last, nextCycle.fact)
        assertEquals(11, nextCycle.pool.remainingFacts.size)
    }

    @Test
    fun quizSessionRejectsEmptyOrInvalidTableSelection() {
        val engine = QuizEngine(QuizTestRandomizer())
        assertThrows(IllegalArgumentException::class.java) { engine.newSession(emptySet()) }
        assertThrows(IllegalArgumentException::class.java) { engine.newSession(setOf(1, 2)) }
    }
}
